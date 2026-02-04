# Research Plugin

Commands for common research tasks: comparing options, checking feasibility, surveying a domain, and more. Each command runs a web search and returns a structured analysis inline.

## Commands

| Command | Description |
|---------|-------------|
| `competitive` | Research competitors — who else does this, how, strengths/weaknesses |
| `deep-dive` | Investigate a topic thoroughly with sources |
| `feasibility` | Reality check — can we actually do this with our constraints? |
| `history` | What's been tried before — past attempts, lessons learned |
| `landscape` | Map the space — tools, players, trends, gaps in a domain |
| `open-source` | Find libraries, tools, and projects that solve this |
| `options` | Compare multiple options side-by-side with a recommendation |
| `technical` | How to implement something — approaches, libraries, tradeoffs |

## Examples

```
/research:options Prisma vs Drizzle vs TypeORM
/research:feasibility real-time collaboration in our current stack
/research:landscape Clojure build tools
```

## Attribution

This plugin is vendored from [glittercowboy/taches-cc-resources](https://github.com/glittercowboy/taches-cc-resources/tree/main/commands/research).

Original author: **glittercowboy**

## License

See original repository for license terms.
