# CCKit - Claude Code Plugin Marketplace

A curated collection of Claude Code plugins maintained by UniSoma. This repository contains:

- **Original** — Built by UniSoma
- **Curated** — Third-party plugins we use and maintain

All plugins are vendored directly in this repository. Third-party plugins include attribution to their original authors and source repositories.

## Available Plugins

| Plugin | Category | Origin | Description |
|--------|----------|--------|-------------|
| [clojure-lsp](./plugins/clojure-lsp) | Development | [boostvolt](https://github.com/boostvolt/claude-code-lsps) | Clojure language server integration |
| [investigate](./plugins/investigate) | Productivity | UniSoma | Adaptive multi-perspective research with iterative evaluation |
| [mcp-nrepl](./plugins/mcp-nrepl) | Development | UniSoma | MCP server for Clojure nREPL integration |
| [research](./plugins/research) | Productivity | [TACHES](https://github.com/glittercowboy/taches-cc-resources) | Structured research frameworks for systematic investigation |
| [think](./plugins/think) | Productivity | UniSoma | Mental models and multi-model analysis with tension synthesis |
| [todo](./plugins/todo) | Productivity | [TACHES](https://github.com/glittercowboy/taches-cc-resources) | Capture ideas mid-conversation and resume later with full context |

## Plugin Types

This marketplace supports these Claude Code plugin types:

- **Commands** — Custom slash commands (e.g., `/hello`, `/deploy`)
- **Agents** — Specialized AI agents for specific tasks
- **Skills** — Reusable capabilities that agents can invoke
- **Hooks** — Event handlers (SessionStart, PreToolUse, PostToolUse, Stop)
- **MCP Servers** — Model Context Protocol integrations for external tools
- **LSP Servers** — Language server integrations for code intelligence

## Installation

### Adding the Marketplace

Add this marketplace to your Claude Code settings:

```json
{
  "marketplaces": [
    "https://github.com/UniSoma/cckit"
  ]
}
```

### Installing Individual Plugins

Use the `/plugin` command in Claude Code:

```
/plugin install cckit:clojure-lsp
```

Or add directly to your settings:

```json
{
  "plugins": [
    "cckit:clojure-lsp"
  ]
}
```

## Contributing

We welcome contributions! See [CONTRIBUTING.md](./CONTRIBUTING.md) for guidelines on:

- Submitting new plugins
- Plugin structure requirements
- Review process

## Plugin Structure

Each plugin follows this structure:

```
plugins/your-plugin/
├── .claude-plugin/
│   └── plugin.json       # Plugin metadata (required)
├── commands/             # Slash commands (optional)
│   └── command-name.md
├── agents/               # Specialized agents (optional)
│   └── agent-name.md
├── skills/               # Agent skills (optional)
│   └── skill-name.md
├── hooks/                # Event handlers (optional)
│   └── hook-name.md
└── README.md             # Plugin documentation (required)
```

## Resources

- [Claude Code Documentation](https://docs.anthropic.com/en/docs/claude-code)
- [Official Plugins Repository](https://github.com/anthropics/claude-code/tree/main/plugins)
- [Plugin Development Guide](https://docs.anthropic.com/en/docs/claude-code/plugins)

## License

MIT - See [LICENSE](./LICENSE)
