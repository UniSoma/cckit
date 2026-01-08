#!/bin/bash

# Check if clojure-lsp is installed
if command -v clojure-lsp &> /dev/null; then
    echo "[clojure-lsp] clojure-lsp is installed and available"
    exit 0
fi

# Try to install via Homebrew if available
if command -v brew &> /dev/null; then
    echo "[clojure-lsp] clojure-lsp not found, attempting to install via Homebrew..."
    brew install clojure-lsp/brew/clojure-lsp-native

    if command -v clojure-lsp &> /dev/null; then
        echo "[clojure-lsp] Successfully installed clojure-lsp"
        exit 0
    else
        echo "[clojure-lsp] Installation completed but clojure-lsp not found in PATH"
        echo "[clojure-lsp] You may need to restart your terminal or add it to your PATH"
        exit 0
    fi
fi

# Provide manual installation instructions
echo "[clojure-lsp] clojure-lsp is not installed"
echo "[clojure-lsp] Please install it manually: https://clojure-lsp.io/installation/"
echo "[clojure-lsp] Common installation methods:"
echo "[clojure-lsp]   - Homebrew: brew install clojure-lsp/brew/clojure-lsp-native"
echo "[clojure-lsp]   - Nix: nix-env -i clojure-lsp"
echo "[clojure-lsp]   - Manual: Download from GitHub releases"
exit 0
