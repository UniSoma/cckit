# Think Plugin

Twelve mental model commands plus two multi-model analysis modes: `/think:fast` for inline results and `/think:deep` for full artifact-based analysis.

## Commands

### `/think:fast` — Fast Multi-Model Analysis

Apply 3-5 mental models in a single pass. No confirmation, no files — returns a consolidated analysis directly.

```
/think:fast Should we use a monorepo or polyrepo?
```

How it works:
1. Classifies the problem and selects 3-5 models (always including a contrarian lens)
2. Loads framework definitions and dispatches a single agent
3. The agent applies each model, identifies tensions between them, and synthesizes
4. Returns the full analysis inline — no files written to disk

Use `/think:fast` when you want structured multi-model thinking without leaving the conversation. Output depth adapts to the topic: models that surface critical findings get more space; models that confirm others stay brief.

### `/think:deep` — Deep Multi-Model Analysis

Analyze a topic through 3-5 mental model lenses with cross-model tension synthesis. Saves all artifacts to disk.

```
/think:deep Should we use a monorepo or polyrepo?
```

How it works:
1. Classifies the problem and selects 3-5 relevant models (always including a contrarian lens)
2. Runs all models in parallel as separate agents
3. A synthesizer identifies where models **disagree** and **agree**
4. Returns key tensions and a bottom line inline; saves full analysis to `artifacts/think/`

### Individual Models

| Command | Description |
|---------|-------------|
| `10-10-10` | Evaluate decisions across three time horizons |
| `5-whys` | Drill to root cause by asking why repeatedly |
| `eisenhower-matrix` | Apply urgent/important matrix to prioritize |
| `first-principles` | Break down to fundamentals and rebuild |
| `inversion` | Solve problems backwards — what would guarantee failure? |
| `occams-razor` | Find simplest explanation that fits all facts |
| `one-thing` | Identify the single highest-leverage action |
| `opportunity-cost` | Analyze what you give up by choosing |
| `pareto` | Apply 80/20 rule to focus on what matters |
| `second-order` | Think through consequences of consequences |
| `swot` | Map strengths, weaknesses, opportunities, threats |
| `via-negativa` | Improve by removing rather than adding |

## When to Use What

**Use `/think:fast`** for inline multi-model analysis during a task. It runs in a single agent, writes no files, and returns the result directly. Good for decisions where you want structured thinking without switching context.

**Use `/think:deep`** for problems worth a thorough written record. It runs models in parallel agents, saves individual lens analyses and a full synthesis to `artifacts/think/`. Good for high-stakes decisions you want to revisit later.

**Use an individual model** when you already know which lens fits, or when you need one focused perspective mid-task.

### Which model for my problem?

| If you're... | Try these models |
|--------------|-----------------|
| Choosing between options | `opportunity-cost`, `10-10-10` |
| Diagnosing a problem | `5-whys`, `first-principles` |
| Prioritizing work | `eisenhower-matrix`, `pareto`, `one-thing` |
| Stress-testing a plan | `inversion`, `second-order` |
| Simplifying something | `via-negativa`, `occams-razor` |
| Evaluating position | `swot` |

When in doubt, use `/think:fast` or `/think:deep` — they select models automatically.

## Examples

### Fast inline analysis

```
/think:fast Should we cache at the application layer or use a CDN?
```

### Deep analysis with artifacts

```
/think:deep Microservices vs modular monolith for our 4-person team
```

### Individual model mid-task

```
/think:inversion We're launching a new payment system
```

```
/think:5-whys Users are abandoning checkout at the payment step
```

## Output

### `/think:fast`

Returns everything inline — model analyses, tensions, convergences, and a bottom line. No files created. Output depth adapts: models with critical findings expand; models that confirm others stay brief.

### `/think:deep`

Saves all artifacts to a session directory:

```
artifacts/think/20260204-1430-monorepo-vs-polyrepo/
  opportunity-cost.md    # Individual lens analysis
  first-principles.md
  10-10-10.md
  inversion.md
  ANALYSIS.md            # Cross-model synthesis with tensions
```

The inline response shows key tensions and a bottom line. Open the individual files for the full reasoning behind each lens.

### Individual commands

Respond directly in the conversation. No files saved.

## Attribution

The individual model commands originate from [glittercowboy/taches-cc-resources](https://github.com/glittercowboy/taches-cc-resources/tree/main/commands/consider). The `/think:deep` and `/think:fast` orchestrators and their supporting agents are original additions by UniSoma.

## License

See original repository for license terms.
