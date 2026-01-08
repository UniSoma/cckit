#!/bin/bash

# Check if babashka (bb) is installed
if command -v bb &> /dev/null; then
    BB_VERSION=$(bb --version 2>/dev/null | head -n1)
    echo "[mcp-nrepl] Babashka is installed: $BB_VERSION"
    exit 0
fi

# Try to install via Homebrew if available (macOS/Linux)
if command -v brew &> /dev/null; then
    echo "[mcp-nrepl] Babashka not found, attempting to install via Homebrew..."
    brew install borkdude/brew/babashka

    if command -v bb &> /dev/null; then
        echo "[mcp-nrepl] Successfully installed babashka"
        exit 0
    else
        echo "[mcp-nrepl] Installation completed but bb not found in PATH"
        echo "[mcp-nrepl] You may need to restart your terminal"
        exit 0
    fi
fi

# Try to install via nix if available
if command -v nix-env &> /dev/null; then
    echo "[mcp-nrepl] Babashka not found, attempting to install via Nix..."
    nix-env -i babashka

    if command -v bb &> /dev/null; then
        echo "[mcp-nrepl] Successfully installed babashka"
        exit 0
    fi
fi

# Provide manual installation instructions
echo "[mcp-nrepl] WARNING: Babashka (bb) is not installed"
echo "[mcp-nrepl] The mcp-nrepl plugin requires babashka to function."
echo "[mcp-nrepl] "
echo "[mcp-nrepl] Installation options:"
echo "[mcp-nrepl]   Homebrew: brew install borkdude/brew/babashka"
echo "[mcp-nrepl]   Nix:      nix-env -i babashka"
echo "[mcp-nrepl]   curl:     bash < <(curl -s https://raw.githubusercontent.com/babashka/babashka/master/install)"
echo "[mcp-nrepl]   Manual:   https://github.com/babashka/babashka#installation"
echo "[mcp-nrepl] "
echo "[mcp-nrepl] After installation, restart Claude Code to enable the mcp-nrepl MCP server."
exit 0
