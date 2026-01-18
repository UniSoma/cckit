# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CCKit is a Claude Code plugin marketplace maintained by UniSoma. It contains original and curated third-party plugins (commands, agents, skills, hooks, LSP/MCP servers).

## Plugin Development

### Creating a New Plugin

1. Create directory: `plugins/your-plugin/`
2. Required files:
   - `.claude-plugin/plugin.json` - Plugin manifest
   - `README.md` - User documentation

3. Optional directories based on plugin type:
   - `commands/` - Slash commands (markdown with YAML frontmatter)
   - `hooks/` - Event handlers (hooks.json + shell scripts)
   - `scripts/` - Supporting scripts (e.g., MCP servers)

4. Register in `.claude-plugin/marketplace.json`

### Plugin Manifest (plugin.json)

```json
{
  "name": "plugin-name",
  "version": "1.0.0",
  "description": "What this plugin does",
  "author": { "name": "Author Name" }
}
```

**Auto-discovered paths** (do NOT include in manifest - causes duplicate loading errors):
- `commands/` - Slash commands directory
- `hooks/hooks.json` - Hook configuration
- `.mcp.json` - MCP server configuration
- `.lsp.json` - LSP server configuration

Only specify these in the manifest if using non-standard locations.

### Command Format

```yaml
---
description: One-line description
argument-hint: [optional usage pattern]
allowed-tools:
  - Read
  - Edit
  - Bash(git:*)
---

Instructions for the command...
```

### Hook Configuration

```json
{
  "hooks": {
    "SessionStart": [{
      "hooks": [{
        "type": "command",
        "command": "${CLAUDE_PLUGIN_ROOT}/hooks/script.sh",
        "timeout": 30
      }]
    }]
  }
}
```

### Third-Party Attribution

When vendoring external plugins:
- Credit original author in plugin.json and README.md
- Add `homepage` field in marketplace.json pointing to source repo
- Include "Attribution" section in plugin README

## Marketplace Registry

Update `.claude-plugin/marketplace.json` when adding plugins:

```json
{
  "name": "plugin-name",
  "description": "...",
  "version": "1.0.0",
  "author": { "name": "..." },
  "source": "./plugins/plugin-name",
  "category": "development|productivity|learning|security"
}
```

## Commit Guidelines

- Clear, concise commit messages
- No AI attribution in commits
