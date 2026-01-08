# Contributing to CCKit

Thank you for your interest in contributing to UniSoma's CCKit! This guide explains how to submit plugins to the marketplace.

## Before You Start

1. **Review existing plugins** - Browse the `plugins/` directory to understand the expected structure
2. **Check the official docs** - Familiarize yourself with [Claude Code plugin documentation](https://docs.anthropic.com/en/docs/claude-code/plugins)
3. **Plan your plugin** - Decide what type of plugin you're creating (command, agent, skill, or hook)

## Plugin Requirements

### Required Files

Every plugin must include:

1. **`.claude-plugin/plugin.json`** - Plugin metadata
2. **`README.md`** - Documentation for users

### plugin.json Format

```json
{
  "name": "your-plugin-name",
  "version": "1.0.0",
  "description": "Brief description of what your plugin does",
  "author": {
    "name": "Your Name"
  }
}
```

### Directory Structure

```
plugins/your-plugin/
├── .claude-plugin/
│   └── plugin.json       # Required
├── commands/             # For slash commands
│   └── command-name.md
├── agents/               # For specialized agents
│   └── agent-name.md
├── skills/               # For agent skills
│   └── skill-name.md
├── hooks/                # For event handlers
│   └── hook-name.md
└── README.md             # Required
```

## Component Formats

### Commands

Commands use markdown with an allowed-tools table:

```markdown
| allowed-tools | description |
|---|---|
| Bash(git:*), Read | Description of command |

## Context

- Variable: !`command`

## Your task

Instructions for the command...
```

### Agents

Agents use markdown with a metadata table:

```markdown
| name | description | tools | model | color |
|------|-------------|-------|-------|-------|
| agent-name | What the agent does | Tool1, Tool2 | sonnet | blue |

Instructions for the agent...
```

**Available models:** `opus`, `sonnet`, `haiku`

**Available colors:** `blue`, `green`, `yellow`, `red`, `purple`, `cyan`

### Skills

Skills follow a similar format to agents but are invoked by other agents rather than directly by users.

### Hooks

Hooks intercept specific events:

```markdown
| hook | description |
|------|-------------|
| SessionStart | Runs when a session begins |

Instructions for the hook...
```

**Hook types:** `SessionStart`, `PreToolUse`, `PostToolUse`, `Stop`

## Submission Process

### 1. Fork and Clone

```bash
git clone https://github.com/UniSoma/cckit.git
cd cckit
```

### 2. Create Your Plugin

```bash
mkdir -p plugins/your-plugin/.claude-plugin
mkdir -p plugins/your-plugin/commands  # or agents/, skills/, hooks/
```

### 3. Add Plugin Files

Create your plugin following the formats above.

### 4. Update Marketplace Index

Add your plugin to `.claude-plugin/marketplace.json`:

```json
{
  "name": "your-plugin",
  "description": "What your plugin does",
  "version": "1.0.0",
  "author": {
    "name": "Your Name"
  },
  "source": "./plugins/your-plugin",
  "category": "development"
}
```

**Categories:** `development`, `productivity`, `learning`, `security`

### 5. Submit Pull Request

1. Commit your changes with a clear message
2. Push to your fork
3. Open a pull request with:
   - Plugin name and description
   - What problem it solves
   - Example usage

## Review Criteria

Pull requests are reviewed for:

- **Functionality** - Does the plugin work as described?
- **Quality** - Is the code/markdown well-written?
- **Documentation** - Is usage clearly explained?
- **Safety** - Does it avoid harmful operations?
- **Uniqueness** - Does it add value beyond existing plugins?

## Guidelines

### Do

- Keep plugins focused on a single purpose
- Write clear, concise documentation
- Include usage examples
- Test your plugin before submitting
- Use semantic versioning

### Don't

- Include sensitive data or credentials
- Create plugins that could cause harm
- Duplicate existing plugins without improvement
- Use overly generic names

## Getting Help

- Open an issue for questions
- Check existing plugins for examples
- Review the official Claude Code documentation

## License

By contributing, you agree that your plugin will be released under the MIT license.
