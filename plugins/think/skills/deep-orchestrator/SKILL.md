---
name: deep-orchestrator
description: Orchestrate multi-model deep thinking analysis through parallel lens agents and tension synthesis
user-invocable: false
---

<objective>
Analyze $ARGUMENTS through multiple mental model lenses, prioritizing cross-model tensions.

This skill orchestrates:
1. Topic validation (ensure genuine tradeoffs exist)
2. Adaptive model selection (3-5 models, always including a contrarian lens)
3. Parallel lens execution (each model applied by a separate agent)
4. Cross-model tension synthesis (a dedicated agent finds where models disagree and why)
5. Inline presentation (key tensions + bottom line returned to conversation)
</objective>

<model_reference>
Available mental models for selection:

| Model | Core Question It Answers | Contrarian? |
|-------|-------------------------|-------------|
| `10-10-10` | How will this decision look across three time horizons? | No |
| `5-whys` | What is the actual root cause? | No |
| `eisenhower-matrix` | What should I do first / delegate / drop? | No |
| `first-principles` | What's actually true vs. assumed? | No |
| `inversion` | What would guarantee failure? (Then avoid that.) | **Yes** |
| `occams-razor` | What's the simplest explanation that fits? | No |
| `one-thing` | What single action has the most leverage? | No |
| `opportunity-cost` | What am I giving up by choosing this? | No |
| `pareto` | Which 20% of effort yields 80% of value? | No |
| `second-order` | What are the consequences of the consequences? | **Yes** |
| `swot` | What are the internal/external strengths & threats? | No |
| `via-negativa` | What should I remove rather than add? | **Yes** |

Use the "Core Question" column to judge relevance to the topic. There are no fixed mappings — reason about which models genuinely illuminate the problem.
</model_reference>

<topic_validation>
Before model selection, validate the topic from $ARGUMENTS:

**If empty or missing**: Use AskUserQuestion to ask "What would you like to think through?" with header "Topic" and options:
- "A decision" (description: "Choosing between options or approaches")
- "A strategy" (description: "Planning direction or priorities")
- "A problem" (description: "Understanding root causes or failure modes")

**If too vague** (single word, "this codebase", no clear problem): Use AskUserQuestion — "What specifically would you like to think through? A decision, a tradeoff, a problem?" with header "Refine" and options:
- "Help me narrow it down" (description: "I'll ask clarifying questions")
- "Let me rephrase" (description: "I'll provide a more specific question")

**If statement without tension** (e.g., "We're building a REST API" — no tradeoff or decision implied): Use AskUserQuestion — "What's the tradeoff or decision you're working through?" with header "Tradeoff" and options:
- "There's a choice to make" (description: "I need to decide between approaches")
- "I want to stress-test this" (description: "Challenge assumptions in this plan")

**If clear with tradeoffs**: Proceed to model selection.
</topic_validation>

<model_selection>
Analyze the validated topic and select models:

1. **Classify the problem**:
   - Primary type: decision, strategy, prioritization, diagnosis, design
   - Secondary type (if applicable)
   - Complexity: simple (clear question) / moderate / complex (ambiguous, high-stakes)

2. **Select 3-5 models** using the reference table's "Core Question" column:
   - Simple problems → 3 models
   - Moderate problems → 3-4 models
   - Complex/ambiguous/high-stakes → 4-5 models
   - Each model must address a distinct dimension of the problem

3. **Verify contrarian lens rule**: At least one selected model must be contrarian (`inversion`, `via-negativa`, or `second-order`).
   - If no contrarian was selected, add `inversion` (most universally applicable contrarian)

4. **Record rationale**: For each selected model, note a 1-line reason why it fits this topic. This will be passed to the synthesizer.
</model_selection>

<adaptive_gate>
Decide whether to confirm model selection with the user:

**Skip confirmation when ALL of these are true:**
- Clear, well-formed question with obvious tradeoffs
- Unambiguous mapping to 3 models
- No high-stakes language ("critical", "irreversible", "bet the company")

**Show confirmation when ANY of these are true:**
- Broad or ambiguous topic
- 5+ models could plausibly apply (needed to narrow)
- High-stakes language present
- 4+ models selected

When confirming, use AskUserQuestion with header "Models":

Format the question as:
```
I'll analyze this with {N} mental models:

1. {Model Name} — {1-line rationale}
2. {Model Name} — {1-line rationale} [contrarian]
...

Does this selection look right?
```

Options:
- "Looks good" (description: "Proceed with these models")
- "Add a model" (description: "Include an additional mental model")
- "Remove/swap" (description: "Change one of the selected models")

If the user selects anything other than "Looks good":
- Apply their feedback to modify the selection
- Re-verify the contrarian lens rule
- Show the updated selection again
- Repeat until confirmed
</adaptive_gate>

<session_setup>
After model selection is finalized (confirmed or skipped), create the session directory:

```bash
TOPIC_SLUG=$(echo "{TOPIC}" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9]/-/g' | sed 's/--*/-/g' | sed 's/^-//' | sed 's/-$//' | head -c 50)
SESSION_ID="$(date +%Y%m%d-%H%M)-${TOPIC_SLUG}"
SESSION_DIR="$(pwd)/artifacts/think/${SESSION_ID}"
mkdir -p "$SESSION_DIR"
```

Save SESSION_ID, SESSION_DIR, and the confirmed model list.

Announce:
```
Starting deep analysis: {TOPIC}
Session: {SESSION_ID}
Models: {N} ({list of model names}, contrarian: {contrarian model names})
```
</session_setup>

<framework_loading>
For each selected model, load its framework definition from the co-located `frameworks/` directory.

Available framework files (relative to this skill's directory):
- `frameworks/10-10-10.md`
- `frameworks/5-whys.md`
- `frameworks/eisenhower-matrix.md`
- `frameworks/first-principles.md`
- `frameworks/inversion.md`
- `frameworks/occams-razor.md`
- `frameworks/one-thing.md`
- `frameworks/opportunity-cost.md`
- `frameworks/pareto.md`
- `frameworks/second-order.md`
- `frameworks/swot.md`
- `frameworks/via-negativa.md`

**Loading procedure:**

1. For each selected model slug, Read `frameworks/{model-slug}.md` relative to the skill directory. Issue ALL Read calls in a single parallel batch.

2. Each file contains the framework body (objective, process, output_format, success_criteria sections) with no frontmatter.

3. Store the full file content as the FRAMEWORK parameter to pass to the lens agent.

**IMPORTANT**: Read ALL framework files before spawning any agents. Framework loading must complete before parallel spawning begins.

**IMPORTANT**: Do NOT skip framework loading or inline frameworks from memory. The framework files contain the canonical framework definitions. If a Read call fails for any framework file, report the error to the user rather than proceeding with improvised frameworks.
</framework_loading>

<parallel_spawning>
Spawn ALL think-lens agents in a SINGLE message with multiple Task calls. This is critical for parallel execution.

For each selected model, create a Task call:

```
Task tool:
  subagent_type: "think:think-lens"
  model: sonnet
  description: "Apply {model-name} lens"
  prompt: |
    MODEL: {model-name}
    TOPIC: {topic}
    OUTPUT: {SESSION_DIR}/{model-slug}.md
    FRAMEWORK:
    {framework content extracted from framework file}

    Apply this mental model framework to the topic.
    Write your analysis to the OUTPUT path.
```

**CRITICAL**: Do NOT use `run_in_background: true`. Synchronous Task calls in a single message run in parallel automatically and return only the agent's final message, keeping orchestrator context minimal.

All lens agents run in parallel. Task blocks until all complete.
</parallel_spawning>

<monitoring>
Track results from each Task return:

- `DONE: {path}` → mark as **completed**
- Error or unexpected output → check with Glob for `{SESSION_DIR}/{model-slug}.md`
  - File exists → mark as **completed** (agent wrote file but returned unexpected message)
  - File missing → retry once (synchronous single Task call)
  - Retry fails AND file still missing after Glob check → mark as **degraded**

**Minimum requirement**: At least 2 lenses must complete (cross-model tension analysis requires 2+ perspectives). If fewer than 2 complete:

```
Deep analysis could not complete — fewer than 2 lenses produced output.

Session: {SESSION_DIR}
{List any raw output files that exist}

Try again with a more specific topic or fewer models.
```

Stop execution. Do not proceed to synthesis.

After all monitoring completes, report status:
```
Lens analysis complete:
- {Model 1}: completed
- {Model 2}: completed
- {Model 3}: degraded (failed after retry)
```
</monitoring>

<synthesis>
Spawn the think-synthesizer synchronously:

Build the MODELS JSON from monitoring results:
```json
[
  {"name": "opportunity-cost", "slug": "opportunity-cost", "status": "completed", "contrarian": false},
  {"name": "inversion", "slug": "inversion", "status": "completed", "contrarian": true}
]
```

```
Task tool:
  subagent_type: "think:think-synthesizer"
  description: "Synthesize multi-model analysis"
  prompt: |
    SESSION_DIR: {SESSION_DIR}
    TOPIC: {topic}
    MODELS: {JSON array with name, slug, status, contrarian flag for each model}

    Read all lens outputs and synthesize into ANALYSIS.md.
```

Do NOT use `run_in_background`. Wait for synthesis to complete.
</synthesis>

<presentation>
After synthesizer returns `DONE: {path}`:

1. Read ANALYSIS.md from the session directory
2. Extract the "## Key Tensions" section content
3. Extract the "## Bottom Line" section content

Present inline:

```
Deep analysis complete ({N} models applied, {M} contrarian).

Key tensions:
{extracted key tension summaries — keep each to 1-2 sentences}

Bottom line:
{extracted bottom line section}

Full analysis: {SESSION_DIR}/ANALYSIS.md
Individual lenses: {comma-separated list of model-slug.md files}
```
</presentation>

<error_handling>
**Fewer than 2 lenses complete**: Stop. Report available raw files and session path.

**Synthesizer fails**: Check Glob for ANALYSIS.md in session directory.
- If ANALYSIS.md exists: proceed to presentation (agent wrote file but returned unexpected message)
- If missing: Stop. Point user to individual lens files:

```
Synthesis failed, but individual lens analyses are available.

Session: {SESSION_DIR}
Individual lenses:
- {list of completed model-slug.md files}

You can read the individual analyses directly.
```

**All lenses complete but synthesizer returns unexpected output**: Check Glob for ANALYSIS.md. If exists, proceed to presentation.
</error_handling>

<success_criteria>
- Topic validated before model selection
- 3-5 models selected with at least 1 contrarian
- All lens agents spawned in parallel (single message with multiple Task calls)
- Failed lenses retried once, then degraded gracefully
- Minimum 2 lenses complete before synthesis
- ANALYSIS.md produced with tensions-first structure
- Inline summary returns Key Tensions + Bottom Line
- Full artifacts available in session directory
</success_criteria>
