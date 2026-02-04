# Clojure LSP Plugin

Connects Claude Code to [clojure-lsp](https://clojure-lsp.io/) for code navigation, diagnostics, and refactoring in Clojure projects.

## Features

- Language server integration for `.clj`, `.cljs`, `.cljc`, and `.edn` files
- Automatic installation check on session start

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

## Attribution

This plugin is vendored from [boostvolt/claude-code-lsps](https://github.com/boostvolt/claude-code-lsps).

Original author: **boostvolt** (Jan Kott)

## License

See original repository for license terms.
