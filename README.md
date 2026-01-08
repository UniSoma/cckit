# CCKit - Claude Code Plugin Marketplace

A Claude Code plugin marketplace created and maintained by UniSoma.

## What is CCKit?

CCKit is a GitHub-based repository by UniSoma where developers can publish and discover Claude Code plugins. All plugins are hosted directly in this repository, making it easy to browse, contribute, and install.

## Available Plugins

| Plugin | Category | Description |
|--------|----------|-------------|
| [clojure-lsp](./plugins/clojure-lsp) | Development | Clojure language server integration |

## Plugin Types

This marketplace supports all Claude Code plugin types:

- **Commands** - Custom slash commands (e.g., `/hello`, `/deploy`)
- **Agents** - Specialized AI agents for specific tasks
- **Skills** - Reusable capabilities that agents can invoke
- **Hooks** - Event handlers (SessionStart, PreToolUse, PostToolUse, Stop)
- **LSP Servers** - Language server integrations for enhanced code intelligence

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
