# MCP nREPL Plugin

An MCP server that lets Claude Code evaluate Clojure code through an nREPL connection.

## Features

- **eval-clojure tool** — Evaluate Clojure code directly from Claude Code
- **Auto-reconnection** on dropped connections
- **Multiple modes** — Connect to an external nREPL or start an embedded server
- **Cancellation support** for long-running evaluations
- **Built-in guide** — `clojure-eval-guide` prompt with common expressions and workflow tips

## Prerequisites

### Babashka

This plugin requires [Babashka](https://babashka.org/) (`bb`) to be installed. The plugin will check for it on session start and provide installation instructions if not found.

**Installation:**

```bash
# Homebrew (macOS/Linux)
brew install borkdude/brew/babashka

# Nix
nix-env -i babashka

# curl installer
bash < <(curl -s https://raw.githubusercontent.com/babashka/babashka/master/install)
```

### nREPL Server

You need an nREPL server running for your Clojure project. Common ways to start one:

**Leiningen:**
```bash
lein repl :headless :port 7888
```

**Clojure CLI (deps.edn):**
```bash
clj -Sdeps '{:deps {nrepl/nrepl {:mvn/version "1.0.0"}}}' -M -m nrepl.cmdline --port 7888
```

**Shadow-cljs:**
```bash
npx shadow-cljs server
# Uses .shadow-cljs/nrepl.port for port
```

## Plugin Installation

Add this plugin to your Claude Code configuration:

```json
{
  "plugins": ["cckit:mcp-nrepl"]
}
```

## Configuration

### Port Resolution

The plugin determines which nREPL port to connect to in this order:

1. `--nrepl-port` flag (if configured in MCP args)
2. `NREPL_PORT` environment variable
3. `.nrepl-port` file in current directory (auto-created by most REPL tools)

### Default (reads .nrepl-port)

The default configuration reads the nREPL port from a `.nrepl-port` file in your project directory (automatically created by most REPL tools).

### Environment Variable

Set the `NREPL_PORT` environment variable before starting Claude Code:

```bash
export NREPL_PORT=7888
claude
```

Or for a single session:

```bash
NREPL_PORT=7888 claude
```

### Custom Port (MCP Override)

To connect to a specific port, override the MCP configuration in your project's `.mcp.json`:

```json
{
  "mcp-nrepl": {
    "type": "stdio",
    "command": "bb",
    "args": ["/path/to/plugin/scripts/mcp-nrepl.bb", "--nrepl-port", "7888"]
  }
}
```

### Embedded Server (No External nREPL)

For quick experimentation without an external nREPL:

```json
{
  "mcp-nrepl": {
    "type": "stdio",
    "command": "bb",
    "args": ["/path/to/plugin/scripts/mcp-nrepl.bb", "--server"]
  }
}
```

Note: The embedded server uses Babashka's nREPL, which lacks some JVM Clojure features (e.g., Java interop, full `clojure.core` macros). See [Babashka differences](https://book.babashka.org/#differences-with-clojure).

## Usage

Once installed and configured, Claude Code will have access to the `eval-clojure` tool.

### Common Expressions

| Task | Expression |
|------|------------|
| Get documentation | `(clojure.repl/doc symbol)` |
| Get source code | `(clojure.repl/source symbol)` |
| Search symbols | `(clojure.repl/apropos "pattern")` |
| Load file | `(load-file "path/to/file.clj")` |
| Switch namespace | `(in-ns 'namespace.name)` |
| Current namespace | `(str *ns*)` |
| List ns vars | `(keys (ns-publics *ns*))` |

### Example Workflow

1. Start your nREPL server: `lein repl :headless :port 7888`
2. Ask Claude to evaluate code: "Can you check what namespace we're in?"
3. Claude will use the eval-clojure tool: `(str *ns*)`
4. Explore your codebase: "Show me the source for `my-fn`"

### Accessing the Guide

Ask Claude to use the `clojure-eval-guide` prompt for a comprehensive reference of common expressions and workflow tips.

## Troubleshooting

### "No nREPL port available"

- Ensure your nREPL server is running
- Check that `.nrepl-port` file exists in your project root
- Or set the `NREPL_PORT` environment variable
- Or specify the port explicitly in your MCP configuration

### "Failed to connect to nREPL"

- Verify the nREPL server is running on the expected port
- Check firewall settings if connecting to a remote server
- Ensure no other process is using the port

### "Babashka not found"

- Install babashka using one of the methods above
- Restart Claude Code after installation

## Technical Details

- **MCP Protocol Version**: 2024-11-05
- **Server Version**: 0.3.0
- **Transport**: stdio (JSON-RPC over stdin/stdout)
- **Timeout**: 30 seconds per evaluation
- **Maximum request size**: 64 KB

## License

MIT
