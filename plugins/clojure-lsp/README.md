# Clojure LSP Plugin

Integrates [clojure-lsp](https://clojure-lsp.io/) with Claude Code for enhanced Clojure development support.

## Features

- Language server integration for Clojure, ClojureScript, and EDN files
- Automatic installation check on session start
- Verbose logging for debugging

## Supported File Extensions

| Extension | Language |
|-----------|----------|
| `.clj` | Clojure |
| `.cljs` | ClojureScript |
| `.cljc` | Clojure (common) |
| `.edn` | EDN |

## Prerequisites

You need `clojure-lsp` installed on your system. The plugin will check for it on session start and provide installation instructions if not found.

### Installation Methods

**Homebrew (macOS/Linux):**
```bash
brew install clojure-lsp/brew/clojure-lsp-native
```

**Nix:**
```bash
nix-env -i clojure-lsp
```

**Manual:**
Download from [GitHub releases](https://github.com/clojure-lsp/clojure-lsp/releases)

See the [official installation guide](https://clojure-lsp.io/installation/) for more options.

## Plugin Installation

Add this plugin to your Claude Code configuration:

```json
{
  "plugins": ["cckit:clojure-lsp"]
}
```

## License

MIT

## Credits

Inspired by [claude-code-lsps](https://github.com/boostvolt/claude-code-lsps) by Jan Kott.
