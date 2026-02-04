# Think Plugin

Twelve mental model commands plus `/think:deep` for multi-model analysis with tension synthesis.

## Commands

### `/think:deep` — Multi-Model Analysis

Analyze a topic through 3-5 mental model lenses with cross-model tension synthesis.

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

**Use `/think:deep`** for problems with genuine tradeoffs — decisions between options, competing priorities, or plans worth stress-testing. The contrarian lens finds failure modes; the synthesis finds where frameworks disagree and why.

**Use an individual model** when you already know which lens fits, or when you need one focused perspective mid-task.

## Examples

### Choosing between options

```
/think:deep Should we use a monorepo or polyrepo?
```
Models: opportunity-cost, 10-10-10, first-principles, inversion

```
/think:deep Microservices vs modular monolith for our 4-person team
```
Models: first-principles, inversion, pareto, second-order

### Prioritizing work

```
/think:deep We have 15 features requested and can only build 3 this quarter
```
Models: eisenhower-matrix, pareto, one-thing, via-negativa

### Stress-testing strategy

```
/think:deep Our startup is deciding whether to pivot from B2B to B2C
```
Models: swot, opportunity-cost, 10-10-10, second-order, inversion

### Diagnosing problems

```
/think:deep Why do our deployments keep failing on Fridays?
```
Models: 5-whys, pareto, inversion

### Improving processes

```
/think:deep Our code review process is slowing us down but catching real bugs
```
Models: pareto, via-negativa, opportunity-cost, inversion

### Individual model examples

Each model asks one question. Pick the question that matches your problem.

```
/think:inversion We're launching a new payment system
```
"What would guarantee this fails?" Surfaces risks that optimistic planning misses.

```
/think:first-principles We need a caching layer
```
"Do we actually need a cache, or are we solving the wrong problem?"

```
/think:5-whys Users are abandoning checkout at the payment step
```
"Why?" — asked five times, drilling past symptoms to root cause.

```
/think:via-negativa Our onboarding flow has 12 steps
```
"What can we remove?" Often stronger than adding more.

```
/think:pareto We're spending too much time on bug fixes
```
"Which 20% of causes produce 80% of bugs?"

```
/think:opportunity-cost We're considering rewriting the frontend in React
```
"What could we build instead with the same effort?"

```
/think:second-order We're adding a mandatory code review policy
```
"What happens after the immediate effect?" Catches unintended consequences.

```
/think:10-10-10 Should we take on this technical debt to ship faster?
```
"How will this look in 10 days, 10 months, 10 years?"

## Output

`/think:deep` saves all artifacts to a session directory:

```
artifacts/think/20260204-1430-monorepo-vs-polyrepo/
  opportunity-cost.md    # Individual lens analysis
  first-principles.md
  10-10-10.md
  inversion.md
  ANALYSIS.md            # Cross-model synthesis with tensions
```

The inline response shows key tensions and a bottom line. Open the individual files for the full reasoning behind each lens.

Individual `/think:*` commands respond directly in the conversation and save no files.

## Attribution

The individual model commands originate from [glittercowboy/taches-cc-resources](https://github.com/glittercowboy/taches-cc-resources/tree/main/commands/consider). The `/think:deep` orchestrator and its supporting agents are original additions by UniSoma.

## License

See original repository for license terms.
