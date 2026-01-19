(ns mcp-nrepl
  (:require [babashka.fs :as fs]
    [babashka.nrepl.server :as nrepl-server]
    [bencode.core :as bencode]
    [cheshire.core :as json]
    [clojure.string :as str]
    [clojure.tools.cli :as cli]))

;; MCP Protocol constants
(def MCP-VERSION "2024-11-05")
(def SERVER-INFO {:name "mcp-nrepl" :version "0.3.0"})
(def MAX-REQUEST-SIZE 65536) ;; 64 KB - maximum JSON-RPC request size

;; nREPL response collection limits
(def max-response-wait-ms 30000) ;; 30 seconds total timeout
(def max-responses 1000)         ;; Maximum responses before aborting

;; JSON-RPC 2.0 standard error codes
(def error-codes
  {:parse-error      -32700
   :invalid-request  -32600
   :method-not-found -32601
   :invalid-params   -32602
   :internal-error   -32603
   :server-not-init  -32002})

;; Thread-safe stdout for concurrent response writing
(def stdout-lock (Object.))

(defn send-response!
  "Thread-safe response writer. Prevents interleaved JSON output."
  [response]
  (locking stdout-lock
    (println (json/generate-string response))
    (flush)))

;; Global state
(def state (atom {:nrepl-input-stream nil
                  :nrepl-output-stream nil
                  :session-id nil
                  :initialized false
                  :nrepl-port nil
                  :embedded-server nil
                  ;; Track active evaluations: {mcp-request-id -> {:nrepl-id uuid, :cancelled? atom}}
                  :active-evals {}}))

;; Utility functions
(defn log-error [msg & args]
  (binding [*out* *err*]
    (println (str "[ERROR] " (apply format msg args)))))

(defn decode-bytes
  "Convert bytes to UTF-8 string"
  [v]
  (String. v "UTF-8"))

(defn decode-response
  "Decode all byte arrays in a response map to UTF-8 strings.
   Handles nested values in sequences (e.g., status field)."
  [response]
  (into {}
    (map (fn [[k v]]
           [k (cond
                (bytes? v) (decode-bytes v)
                (sequential? v) (mapv #(if (bytes? %) (decode-bytes %) %) v)
                :else v)])
      response)))

(defn read-bounded-line
  "Read a line from reader with a maximum size limit.
   Returns nil on EOF, throws on size exceeded."
  [^java.io.Reader reader max-size]
  (let [sb (StringBuilder.)
        limit (inc max-size)]
    (loop [char-count 0]
      (let [ch (.read reader)]
        (cond
          (= -1 ch) (when (pos? char-count) (str sb))
          (= ch (int \newline)) (str sb)
          (>= char-count limit)
          (throw (ex-info "Request too large"
                   {:type :invalid-request :limit max-size}))
          :else (do (.append sb (char ch))
                  (recur (inc char-count))))))))

(defn parse-port
  "Pure function: parse port string to integer, returns nil if invalid"
  [port-str]
  (when port-str
    (parse-long (str/trim port-str))))

(defn read-nrepl-port [& [provided-port]]
  (cond
    provided-port
    (or (parse-port provided-port)
      (do (log-error "Invalid port number: %s" provided-port)
        nil))

    (System/getenv "NREPL_PORT")
    (let [port-str (System/getenv "NREPL_PORT")
          port (parse-port port-str)]
      (or port
        (do (log-error "Invalid NREPL_PORT environment variable: %s" port-str)
          nil)))

    (fs/exists? ".nrepl-port")
    (try
      (let [port-str (slurp ".nrepl-port")
            port (parse-port port-str)]
        (or port
          (do (log-error "Invalid port number in .nrepl-port: %s" port-str)
            nil)))
      (catch Exception e
        (log-error "Failed to read .nrepl-port: %s" (.getMessage e))
        nil))

    :else
    (do
      (log-error "No nREPL port specified. Use --nrepl-port <port>, set NREPL_PORT env var, or create .nrepl-port file")
      nil)))

(defn connect-to-nrepl [port]
  (try
    (let [socket (java.net.Socket. "localhost" port)
          input-stream (java.io.PushbackInputStream. (.getInputStream socket))
          output-stream (.getOutputStream socket)]
      (.setSoTimeout socket 500)
      {:input-stream input-stream
       :output-stream output-stream})
    (catch Exception e
      (log-error "Failed to connect to nREPL on port %d: %s" port (.getMessage e))
      nil)))

(defn send-nrepl-message [msg]
  (try
    (when-let [out (:nrepl-output-stream @state)]
      (bencode/write-bencode out msg)
      (.flush out)
      true)
    (catch Exception e
      (log-error "Failed to send nREPL message: %s" (.getMessage e))
      false)))

(defn read-nrepl-response []
  (when-let [in (:nrepl-input-stream @state)]
    (let [response (bencode/read-bencode in)]
      (if (map? response)
        response
        (throw (ex-info "Invalid nREPL response (not a map)"
                 {:type :protocol-error :response response}))))))

(defn read-nrepl-response-with-cancel
  "Read nREPL response with cancellation support.
   Polls for responses, checking cancelled? atom between socket timeouts.
   Returns nil if cancelled, response map otherwise.
   Throws on overall timeout."
  [cancelled?-atom timeout-ms]
  (let [deadline (+ (System/currentTimeMillis) timeout-ms)]
    (loop []
      (cond
        ;; Check cancellation first
        (and cancelled?-atom @cancelled?-atom)
        nil

        ;; Check overall timeout
        (> (System/currentTimeMillis) deadline)
        (throw (ex-info "Timeout waiting for nREPL response"
                 {:type :timeout-error :timeout-ms timeout-ms}))

        :else
        (let [result (try
                       {:response (read-nrepl-response)}
                       (catch java.net.SocketTimeoutException _
                         {:timeout true}))]
          (if (:timeout result)
            (recur)
            (:response result)))))))

(defn create-session []
  (let [clone-msg {"op" "clone" "id" (str (java.util.UUID/randomUUID))}]
    (when (send-nrepl-message clone-msg)
      (some-> (read-nrepl-response)
        (get "new-session")))))

(defn interrupt-eval!
  "Send interrupt op to nREPL for a specific evaluation."
  [nrepl-request-id]
  (let [{:keys [session-id]} @state]
    (when session-id
      (send-nrepl-message {"op" "interrupt"
                           "session" session-id
                           "interrupt-id" nrepl-request-id
                           "id" (str (java.util.UUID/randomUUID))}))))

(defn connection-alive?
  "Check if the nREPL connection is still alive by sending a describe op."
  []
  (try
    (when (:nrepl-output-stream @state)
      (send-nrepl-message {"op" "describe" "id" (str (java.util.UUID/randomUUID))})
      (some? (read-nrepl-response)))
    (catch Exception _ false)))

(defn reset-connection!
  "Close existing connection and clear state for reconnection."
  []
  (when-let [in (:nrepl-input-stream @state)]
    (try (.close in) (catch Exception _)))
  (when-let [out (:nrepl-output-stream @state)]
    (try (.close out) (catch Exception _)))
  (swap! state assoc
    :nrepl-input-stream nil
    :nrepl-output-stream nil
    :session-id nil))

(defn shutdown!
  "Gracefully shutdown the server, closing all connections and stopping embedded server."
  []
  (log-error "Shutting down mcp-nrepl...")
  (reset-connection!)
  (when-let [srv (:embedded-server @state)]
    (try
      (nrepl-server/stop-server! srv)
      (catch Exception e
        (log-error "Error stopping embedded server: %s" (.getMessage e)))))
  (swap! state assoc :initialized false :embedded-server nil))

(defn connect-nrepl!
  "Establish a new nREPL connection. Throws on failure."
  []
  (let [port (or (:nrepl-port @state) (read-nrepl-port))]
    (when-not port
      (throw (ex-info "No nREPL port available. Use --nrepl-port or create .nrepl-port file."
               {:type :connection-error})))
    (let [conn (connect-to-nrepl port)]
      (when-not conn
        (throw (ex-info (str "Failed to connect to nREPL on port " port)
                 {:type :connection-error :port port})))
      (swap! state assoc
        :nrepl-input-stream (:input-stream conn)
        :nrepl-output-stream (:output-stream conn))
      (let [session-id (create-session)]
        (when-not session-id
          (throw (ex-info "Failed to create nREPL session"
                   {:type :connection-error :port port})))
        (swap! state assoc :session-id session-id)))))

(defn ensure-nrepl-connection []
  (when-not (:nrepl-input-stream @state)
    (connect-nrepl!)))

(defn collect-nrepl-responses
  "Collect nREPL responses until 'done' status or cancellation.
   Decodes byte arrays to strings once when reading.
   Only collects responses matching the given nrepl-id.
   Returns {:responses [...] :cancelled? bool}.
   Throws if timeout exceeded or too many responses collected."
  [nrepl-id cancelled?-atom]
  (let [start-time (System/currentTimeMillis)]
    (loop [responses []]
      (let [elapsed (- (System/currentTimeMillis) start-time)
            remaining-ms (- max-response-wait-ms elapsed)]
        (cond
          ;; Check cancellation flag first
          (and cancelled?-atom @cancelled?-atom)
          {:responses responses :cancelled? true}

          (> elapsed max-response-wait-ms)
          (throw (ex-info "Timeout collecting nREPL responses"
                   {:type :timeout-error :collected (count responses) :elapsed-ms elapsed}))

          (> (count responses) max-responses)
          (throw (ex-info "Too many nREPL responses"
                   {:type :limit-error :limit max-responses}))

          :else
          ;; Use cancellation-aware reader with remaining time
          (if-let [raw-response (read-nrepl-response-with-cancel cancelled?-atom remaining-ms)]
            (let [response (decode-response raw-response)
                  response-id (get response "id")
                  status (get response "status")]
              ;; Only process responses matching our request ID
              (if (= response-id nrepl-id)
                (if (and status (some #(= "done" %) status))
                  {:responses (conj responses response) :cancelled? false}
                  (recur (conj responses response)))
                ;; Skip responses from other requests (stale messages)
                (recur responses)))
            ;; nil response means cancelled
            {:responses responses :cancelled? true}))))))

(defn eval-nrepl-code*
  "Internal: Evaluate code via nREPL and return all responses.
   mcp-request-id is used for cancellation tracking (optional)."
  [code mcp-request-id]
  (let [{:keys [session-id]} @state
        nrepl-id (str (java.util.UUID/randomUUID))
        cancelled? (atom false)
        msg {"op" "eval"
             "code" code
             "session" session-id
             "id" nrepl-id}]
    ;; Register active eval for cancellation support
    (when mcp-request-id
      (swap! state assoc-in [:active-evals mcp-request-id]
        {:nrepl-id nrepl-id :cancelled? cancelled?}))
    (try
      (when (send-nrepl-message msg)
        (collect-nrepl-responses nrepl-id cancelled?))
      (finally
        ;; Unregister on completion
        (when mcp-request-id
          (swap! state update :active-evals dissoc mcp-request-id))))))

(defn eval-nrepl-code
  "Evaluate code via nREPL and return result map {:responses [...] :cancelled? bool}.
   Automatically reconnects once if the connection is stale.
   mcp-request-id enables cancellation tracking (optional)."
  [code & [mcp-request-id]]
  (ensure-nrepl-connection)
  (try
    (eval-nrepl-code* code mcp-request-id)
    (catch Exception e
      ;; Connection may be stale - reset and retry once
      (log-error "nREPL eval failed, attempting reconnect: %s" (.getMessage e))
      (reset-connection!)
      (connect-nrepl!)
      (eval-nrepl-code* code mcp-request-id))))

(defn eval-clojure-code
  "Evaluate Clojure code and return result map {:responses [...] :cancelled? bool}.
   Throws exception on failure. mcp-request-id enables cancellation tracking."
  [code & [mcp-request-id]]
  (if-let [result (eval-nrepl-code code mcp-request-id)]
    result
    (throw (ex-info "No nREPL connection available" {:type :internal-error}))))

;; Tool response formatting
(defn extract-field-from-responses
  "Extract a field from nREPL responses (already decoded)"
  [responses field]
  (keep #(get % field) responses))

(defn format-tool-result
  "Format nREPL responses into a tool result.
   Accepts :cancelled? to indicate evaluation was interrupted."
  [responses & {:keys [default-message cancelled?]}]
  (let [values (extract-field-from-responses responses "value")
        output (str/join "\n" (extract-field-from-responses responses "out"))
        errors (str/join "\n" (extract-field-from-responses responses "err"))
        cancel-msg (when cancelled? "[Evaluation cancelled]")
        result-text (str/join "\n"
                      (concat
                        (when cancel-msg [cancel-msg])
                        (when-not (str/blank? output) [output])
                        (when-not (str/blank? errors) [errors])
                        values))]
    {"content" [{"type" "text"
                 "text" (if (str/blank? result-text)
                          (or default-message "nil")
                          result-text)}]}))

(defn format-tool-error
  "Format an error message as a tool response"
  [error-msg]
  {"isError" true
   "content" [{"type" "text"
               "text" error-msg}]})

;; MCP Protocol handlers

(defn handle-initialize [_params]
  (swap! state assoc :initialized true)
  {"protocolVersion" MCP-VERSION
   "capabilities" {"tools" {"listChanged" false}
                   "prompts" {"listChanged" false}}
   "serverInfo" SERVER-INFO})

(defn handle-tools-list []
  {"tools"
   [{"name" "eval-clojure"
     "description" "Evaluate Clojure code using nREPL. Use Clojure expressions for common tasks: (clojure.repl/doc sym), (clojure.repl/source sym), (clojure.repl/apropos \"query\"), (load-file \"path\"), (in-ns 'ns), (str *ns*)"
     "inputSchema"
     {"type" "object"
      "properties"
      {"code" {"type" "string"
               "description" "The Clojure code to evaluate"}}
      "required" ["code"]}}]})

(defn handle-tools-call
  "Handle tools/call requests. mcp-request-id enables cancellation tracking."
  [params mcp-request-id]
  (let [tool-name (get params "name")
        arguments (get params "arguments" {})]
    (case tool-name
      "eval-clojure"
      (let [code (get arguments "code")]
        (if (str/blank? code)
          (format-tool-error "Error: code parameter is required and cannot be empty")
          (try
            (let [result (eval-clojure-code code mcp-request-id)]
              (format-tool-result (:responses result) :cancelled? (:cancelled? result)))
            (catch Exception e
              (format-tool-error (str "Error evaluating Clojure code: " (.getMessage e)))))))

      (format-tool-error (str "Unknown tool: " tool-name)))))

(defn handle-resources-list []
  {"resources" []})

(defn handle-resources-read [_params]
  (throw (ex-info "Resources are not supported. Use eval-clojure tool instead."
           {:type :method-not-found})))

(defn handle-prompts-list []
  {"prompts"
   [{"name" "clojure-eval-guide"
     "description" "Guide for evaluating Clojure code via nREPL - common expressions and workflow tips"
     "arguments" []}]})

(defn handle-prompts-get [params]
  (let [prompt-name (get params "name")]
    (case prompt-name
      "clojure-eval-guide"
      {"description" "Guide for evaluating Clojure code via nREPL"
       "messages" [{"role" "user"
                    "content" {"type" "text"
                               "text" "# Clojure Evaluation Guide

Use `eval-clojure` with these common expressions:

## Common Expressions

| Task | Expression |
|------|------------|
| Get documentation | `(clojure.repl/doc symbol)` |
| Get source code | `(clojure.repl/source symbol)` |
| Search symbols | `(clojure.repl/apropos \"pattern\")` |
| Load file | `(load-file \"path/to/file.clj\")` |
| Switch namespace | `(in-ns 'namespace.name)` |
| Current namespace | `(str *ns*)` |
| List ns vars | `(keys (ns-publics *ns*))` |
| List all namespaces | `(map str (all-ns))` |

## Workflow

1. Verify connection: `(+ 1 1)`
2. If it fails, ensure nREPL is running on port 7888

## Tips

- Always use `:reload` when requiring: `(require '[my.ns :reload])`
- Check namespace before evaluating: `(str *ns*)`
- Use `doc` before unfamiliar functions
- Use `apropos` to discover related functions

## Alternative: clj-nrepl-eval CLI

For nREPL on other ports (e.g., nbb on 7889):
```bash
clj-nrepl-eval --port 7889 \"(+ 1 2 3)\"
```"}}]}

      (throw (ex-info (str "Unknown prompt: " prompt-name)
               {:type :invalid-params :prompt-name prompt-name})))))

(defn handle-cancelled
  "Handle notifications/cancelled - interrupt a running evaluation."
  [params]
  (let [request-id (get params "requestId")]
    (when-let [eval-info (get-in @state [:active-evals request-id])]
      ;; Set the cancelled flag so collect-nrepl-responses will exit
      (reset! (:cancelled? eval-info) true)
      ;; Send interrupt to nREPL to stop the running evaluation
      (interrupt-eval! (:nrepl-id eval-info))))
  ::notification)

(defn handle-request [request]
  (let [method (get request "method")
        params (get request "params")
        id (get request "id")]

    (when-not (:initialized @state)
      (when-not (= "initialize" method)
        (throw (ex-info "Server not initialized" {:type :server-not-init}))))

    (let [result
          (case method
            "initialize" (handle-initialize params)
            "notifications/initialized" ::notification  ;; Notification, no response needed
            "notifications/cancelled" (handle-cancelled params)
            "ping" {}  ;; Empty object response for health checks
            "tools/list" (handle-tools-list)
            "tools/call" (handle-tools-call params id)  ;; Pass ID for cancellation tracking
            "resources/list" (handle-resources-list)
            "resources/read" (handle-resources-read params)
            "prompts/list" (handle-prompts-list)
            "prompts/get" (handle-prompts-get params)
            (throw (ex-info (str "Unknown method: " method)
                     {:type :method-not-found :method method})))]

      ;; Notifications don't get a response
      (when-not (= ::notification result)
        {"jsonrpc" "2.0"
         "id" id
         "result" result}))))

(defn handle-error
  "Create a JSON-RPC 2.0 error response with standard error codes."
  ([id code error-msg]
    (handle-error id code error-msg nil))
  ([id code error-msg data]
    (let [error-code (if (keyword? code) (get error-codes code) code)]
      {"jsonrpc" "2.0"
       "id" id
       "error" (cond-> {"code" error-code
                        "message" error-msg}
                 data (assoc "data" data))})))

(defn json-parse-error?
  "Check if exception is a JSON parse error (by class name, for Babashka compatibility)"
  [e]
  (some-> e class .getName (.startsWith "com.fasterxml.jackson")))

(defn process-message [line]
  (try
    (let [request (json/parse-string line)
          id (get request "id")]
      (if (get request "method")
        (try
          (handle-request request)
          (catch clojure.lang.ExceptionInfo e
            (let [data (ex-data e)
                  error-type (:type data)]
              (handle-error id (or error-type :internal-error) (.getMessage e) data)))
          (catch Exception e
            (handle-error id :internal-error (.getMessage e))))
        (handle-error id :invalid-request "Invalid request: missing method")))
    (catch Exception e
      (if (json-parse-error? e)
        (handle-error nil :parse-error "Parse error: Invalid JSON")
        (handle-error nil :internal-error (.getMessage e))))))

;; Async dispatch for tools/call to enable cancellation
(defn dispatch-async?
  "Returns true if request should be handled asynchronously (in worker thread)."
  [request]
  (and (= "tools/call" (get request "method"))
    (some? (get request "id"))))

(defn handle-async!
  "Process request in worker thread, send response when done.
   Allows main thread to continue reading stdin for cancellation notifications."
  [request]
  (future
    (try
      (let [response (handle-request request)]
        (when response
          (send-response! response)))
      (catch Exception e
        (send-response!
          (handle-error (get request "id") :internal-error (.getMessage e)))))))

(def cli-options
  [["-p" "--nrepl-port PORT" "Connect to nREPL server on specified port"
    :parse-fn parse-port
    :validate [integer? "Must be a valid port number"]]
   ["-s" "--server" "Start embedded nREPL server (no external server needed)"]
   ["-e" "--eval CODE" "Evaluate Clojure code and print result (connectionless eval mode)"]
   ["-h" "--help" "Show this help message"]])

(defn usage [options-summary]
  (->> ["mcp-nrepl - MCP server bridge to nREPL"
        ""
        "Usage: mcp-nrepl.bb [OPTIONS]"
        "       mcp-nrepl.bb --server          # Start with embedded nREPL server"
        "       mcp-nrepl.bb --eval CODE       # Evaluate code"
        ""
        "Options:"
        options-summary
        ""
        "Modes:"
        "  MCP Server Mode (default): Reads MCP JSON-RPC messages from stdin"
        "  Connectionless Eval Mode (--eval): Evaluates code and prints result"
        ""
        "Server Options:"
        "  --server: Start an embedded nREPL server (no external server needed)"
        "  --nrepl-port PORT: Connect to external nREPL server on specified port"
        ""
        "Port Resolution (in order of priority):"
        "  1. --nrepl-port flag"
        "  2. NREPL_PORT environment variable"
        "  3. .nrepl-port file in current directory"]
    (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
    (str/join \newline errors)))

(defn validate-args [args]
  (let [{:keys [options _arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      {:exit-message (usage summary) :ok? true}

      errors
      {:exit-message (error-msg errors)}

      :else
      {:options options})))

(defn run-eval-mode
  "Connectionless evaluation mode - evaluate code and print result to stdout"
  [code]
  (try
    (let [eval-result (eval-clojure-code code)
          result (format-tool-result (:responses eval-result))
          text (get-in result ["content" 0 "text"])]
      (println text)
      (System/exit 0))
    (catch Exception e
      (binding [*out* *err*]
        (println (str "Error: " (.getMessage e))))
      (System/exit 1))))

(defn main [& args]
  (let [{:keys [options exit-message ok?]} (validate-args args)]
    (if exit-message
      (do
        (println exit-message)
        (System/exit (if ok? 0 1)))
      (do
        ;; Register shutdown hook early for graceful termination
        (.addShutdownHook (Runtime/getRuntime)
          (Thread. shutdown!))

        (try
          ;; Start embedded nREPL server if --server option is provided
          (when (:server options)
            (let [server (nrepl-server/start-server! {:host "localhost" :port 0 :quiet true})
                  port (.getLocalPort (:socket server))]
              (swap! state assoc :embedded-server server :nrepl-port port)))

          ;; Set nREPL port from options if provided (overrides embedded server port)
          (when-let [port (:nrepl-port options)]
            (when-not (:server options)  ; Don't override if using embedded server
              (swap! state assoc :nrepl-port port)))

          ;; Check if we're in eval mode or MCP server mode
          (if-let [code (:eval options)]
            ;; Connectionless eval mode - evaluate code and exit
            (run-eval-mode code)

            ;; MCP server mode - read JSON-RPC messages from stdin
            ;; Uses async dispatch for tools/call to allow cancellation
            (let [stdin-reader (java.io.BufferedReader. (java.io.InputStreamReader. System/in "UTF-8"))]
              (loop []
                (when-let [line (try
                                  (read-bounded-line stdin-reader MAX-REQUEST-SIZE)
                                  (catch clojure.lang.ExceptionInfo _
                                    (send-response!
                                      (handle-error nil :invalid-request
                                        "Request too large. Maximum size: 64 KB."
                                        {:max-size MAX-REQUEST-SIZE}))
                                    nil))]
                  (try
                    (let [request (json/parse-string line)]
                      (if (dispatch-async? request)
                        ;; Async dispatch for tools/call - worker thread handles response
                        (handle-async! request)
                        ;; Sync dispatch for all other requests
                        (when-let [response (process-message line)]
                          (send-response! response))))
                    (catch Exception e
                      (send-response!
                        (if (json-parse-error? e)
                          (handle-error nil :parse-error "Parse error: Invalid JSON")
                          (handle-error nil :internal-error (.getMessage e))))))
                  (recur)))))

          (catch Exception e
            (log-error "Fatal error: %s" (.getMessage e))
            (System/exit 1)))))))

;; Entry point
(when (= *file* (System/getProperty "babashka.file"))
  (apply main *command-line-args*))
