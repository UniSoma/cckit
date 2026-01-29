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

### Quick Mode
```
/investigate:quick how does React Server Components work
/investigate:quick compare Prisma vs Drizzle
```

Use `quick` for fast, single-pass research when you don't need multiple perspectives or iterative refinement.

### Answer Mode
```
/investigate:answer how does React Server Components work
/investigate:answer compare Prisma vs Drizzle for type safety
```

Use `answer` for direct inline answers with no files written to disk. Adapts response length to question complexity.

### With Topic Refinement
```
/investigate:ask
/investigate:ask databases
```

Use `ask` when you have a vague idea that needs clarification before investigating.

### Commands

| Command | Purpose |
|---------|---------|
| `/investigate:run [topic]` | Full investigation with multiple perspectives and quality evaluation |
| `/investigate:quick [topic]` | Fast single-pass research with file output |
| `/investigate:answer [question]` | Direct inline answer — adaptive detail, no files written |
| `/investigate:ask [idea]` | Refine vague topics through questioning before investigating |

## How It Works

### Full Mode (`/investigate:run`)

1. **Decompose** — analyzes your topic and generates 2-5 research perspectives (no fixed personas, dynamically chosen from the query)
2. **Confirm** — shows you the plan and lets you add, remove, or modify perspectives before starting
3. **Research** — spawns parallel researcher agents, each investigating from their assigned perspective. Uses haiku for straightforward research, sonnet for complex analysis
4. **Synthesize** — combines findings into a theme-organized REPORT.md (organized by theme, not by perspective)
5. **Evaluate** — assesses quality across three dimensions (groundedness, coverage, synthesis quality) and issues ACCEPT or RE_RESEARCH verdict
6. **Iterate** — if gaps are found, spawns additional researchers to fill them, re-synthesizes, and re-evaluates (up to 3 iterations)

### Quick Mode (`/investigate:quick`)

1. **Clarify** (if needed) — asks for focus only if topic is vague (1-2 words, bare nouns)
2. **Select model** — haiku for factual queries, sonnet for comparisons/tradeoffs
3. **Research** — single researcher agent investigates directly
4. **Output** — summary displayed inline, full FINDINGS.md written to disk

### Answer Mode (`/investigate:answer`)

1. **Clarify** (if needed) — asks only for extremely vague topics (single words)
2. **Select model** — haiku for factual queries, sonnet for comparisons/tradeoffs
3. **Research** — lightweight agent investigates directly
4. **Output** — full answer returned inline, no files written

| Aspect | Full Mode | Quick Mode | Answer Mode |
|--------|-----------|------------|-------------|
| Perspectives | 2-5 parallel | Single pass | Single pass |
| User confirmation | Yes | Only if vague | Only if extremely vague |
| Model selection | Per-perspective | Smart (haiku/sonnet) | Smart (haiku/sonnet) |
| Researchers | Parallel agents | Single agent | Single agent |
| Evaluation loop | Up to 3 iterations | None | None |
| Output | Multiple files | Single file + inline summary | Inline only (no files) |
| Use case | Deep analysis | Fast answers | Direct answers |

## Output

All artifacts are written to `artifacts/investigate/{session-id}/`:

### Full Mode (`/investigate:run`)

| File | Description |
|------|-------------|
| `REPORT.md` | Theme-organized synthesis (main output) |
| `EVALUATION.md` | Quality assessment with scores |
| `*-raw.md` | Individual perspective raw findings |

### Quick Mode (`/investigate:quick`)

Output: `artifacts/investigate/{session-id}.md` (single file, no subdirectory)

Quick mode also displays a 2-3 sentence summary inline after completion.

### Answer Mode (`/investigate:answer`)

No files written. The full answer is returned directly in the conversation.

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

/investigate:quick (command)
         |
         +-- quick-researcher (agent, model selected by orchestrator)
         |
         +-- source-evaluation (skill)

/investigate:answer (command)
         |
         +-- answer-researcher (agent, model selected by orchestrator)
         |
         +-- source-evaluation (skill)
```

- **4 commands** — `ask` for topic refinement, `run` for full orchestration, `quick` for fast research, `answer` for direct inline answers
- **6 agents** — 4 for full mode (2 researchers, synthesizer, evaluator), 1 for quick mode, 1 for answer mode (both model selected dynamically)
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
| Quick mode | Smart clarification (vague topics only) + smart model selection |
| Answer mode | No disk output, adaptive response length, minimal clarification |
