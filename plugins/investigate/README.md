# Investigate Plugin

Adaptive multi-perspective research with iterative evaluation. Decomposes questions into dynamic perspectives, researches them in parallel, synthesizes findings by theme, and evaluates quality with optional re-research.

## Usage

### Quick Start
```
/investigate:run best database for a new SaaS product
/investigate:run security implications of migrating to microservices
/investigate:run build vs buy for authentication
```

If no topic is provided, the command will ask interactively.

### With Topic Refinement
```
/investigate:ask
/investigate:ask databases
```

Use `ask` when you have a vague idea that needs clarification before investigating.

### Commands

| Command | Purpose |
|---------|---------|
| `/investigate:run [topic]` | Run full investigation (asks interactively if no topic) |
| `/investigate:ask [idea]` | Refine vague topics through questioning before investigating |

## How It Works

1. **Decompose** — analyzes your topic and generates 2-5 research perspectives (no fixed personas, dynamically chosen from the query)
2. **Confirm** — shows you the plan and lets you add, remove, or modify perspectives before starting
3. **Research** — spawns parallel researcher agents, each investigating from their assigned perspective. Uses haiku for straightforward research, sonnet for complex analysis
4. **Synthesize** — combines findings into a theme-organized REPORT.md (organized by theme, not by perspective)
5. **Evaluate** — assesses quality across three dimensions (groundedness, coverage, synthesis quality) and issues ACCEPT or RE_RESEARCH verdict
6. **Iterate** — if gaps are found, spawns additional researchers to fill them, re-synthesizes, and re-evaluates (up to 3 iterations)

## Output

All artifacts are written to `artifacts/investigate/{session-id}/`:

| File | Description |
|------|-------------|
| `REPORT.md` | Theme-organized synthesis (main output) |
| `EVALUATION.md` | Quality assessment with scores |
| `*-raw.md` | Individual perspective raw findings |

## Architecture

```
/investigate:ask (command) ──> /investigate:run (command)
                                    |
                                    +-- researcher (agent, haiku)
                                    +-- researcher-deep (agent, sonnet)
                                    +-- synthesizer (agent, sonnet)
                                    +-- evaluator (agent, sonnet)
                                    |
                                    +-- source-evaluation (skill)
                                    +-- output-standards (skill)
```

- **2 commands** — `ask` for topic refinement, `run` for full orchestration
- **4 agents** — 2 researchers (haiku/sonnet), 1 synthesizer, 1 evaluator
- **2 skills** — source evaluation criteria and output format standards

## Key Design Decisions

| Decision | Choice |
|----------|--------|
| Perspectives | Dynamic from query context (not fixed personas) |
| Decomposition | LLM-native (no frameworks) |
| User control | Show and confirm perspectives before researching |
| Iteration | Adaptive 1-3 passes based on evaluator verdict |
| Evaluator | Actionable — ACCEPT or RE_RESEARCH with specific directives |
| Model selection | haiku for factual research, sonnet for complex analysis |
| Report organization | By theme (not by perspective) |
| Failure handling | Retry once, then graceful degradation |
