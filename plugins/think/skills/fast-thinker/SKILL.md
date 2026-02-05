---
name: fast-thinker
description: Fast multi-model analysis — no confirmation, no files, inline result
user-invocable: false
---

<objective>
Analyze $ARGUMENTS through multiple mental model lenses in a single pass, returning consolidated analysis with tensions inline. No files written, no user confirmation — just fast structured thinking.

This skill orchestrates:
1. Topic validation (ensure genuine tradeoffs exist)
2. Adaptive model selection (3-5 models, always including a contrarian lens)
3. Framework loading (reuses existing framework definitions)
4. Single agent dispatch (one agent applies all lenses and synthesizes)
5. Inline result return (agent response returned directly)
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

4. **Record rationale**: For each selected model, note a 1-line reason why it fits this topic.
</model_selection>

<framework_loading>
For each selected model, load its framework definition from the co-located deep-orchestrator's `frameworks/` directory.

Available framework files (relative to this skill's parent directory):
- `deep-orchestrator/frameworks/10-10-10.md`
- `deep-orchestrator/frameworks/5-whys.md`
- `deep-orchestrator/frameworks/eisenhower-matrix.md`
- `deep-orchestrator/frameworks/first-principles.md`
- `deep-orchestrator/frameworks/inversion.md`
- `deep-orchestrator/frameworks/occams-razor.md`
- `deep-orchestrator/frameworks/one-thing.md`
- `deep-orchestrator/frameworks/opportunity-cost.md`
- `deep-orchestrator/frameworks/pareto.md`
- `deep-orchestrator/frameworks/second-order.md`
- `deep-orchestrator/frameworks/swot.md`
- `deep-orchestrator/frameworks/via-negativa.md`

**Loading procedure:**

1. For each selected model slug, Read `deep-orchestrator/frameworks/{model-slug}.md` relative to the skills directory. Issue ALL Read calls in a single parallel batch.

2. Each file contains the framework body (objective, process, output_format, success_criteria sections) with no frontmatter.

3. Store the full file content as part of the FRAMEWORKS parameter to pass to the agent.

**IMPORTANT**: Read ALL framework files before spawning the agent. Framework loading must complete before dispatch.

**IMPORTANT**: Do NOT skip framework loading or inline frameworks from memory. The framework files contain the canonical framework definitions. If a Read call fails for any framework file, report the error to the user rather than proceeding with improvised frameworks.
</framework_loading>

<dispatch>
Spawn a SINGLE think-fast agent with all frameworks and models:

```
Task tool:
  subagent_type: "think:think-fast"
  model: sonnet
  description: "Fast multi-model analysis"
  prompt: |
    TOPIC: {topic}

    MODELS:
    1. {Model Name} — {rationale} {[contrarian] if applicable}
    2. {Model Name} — {rationale} {[contrarian] if applicable}
    ...

    FRAMEWORKS:

    === {model-slug} ===
    {full framework file content}

    === {model-slug} ===
    {full framework file content}

    ...

    Apply each mental model framework to the topic and produce a consolidated analysis with tensions.
```

Do NOT use `run_in_background: true`. Wait for the agent to complete.

The agent's response IS the final output. Return it directly to the calling context with no modification. Do not summarize, truncate, or reformat the agent's analysis.
</dispatch>

<error_handling>
**Agent fails or returns empty**: Report to the user:

```
Fast analysis could not complete.
Try again with a more specific topic, or use /think:deep for the full multi-agent analysis.
```

**Framework file read fails**: Report the error and do not proceed with improvised frameworks. Tell the user which framework could not be loaded.
</error_handling>

<success_criteria>
- Topic validated before model selection
- 3-5 models selected with at least 1 contrarian
- All framework files loaded from disk (not improvised)
- Single agent dispatched with all frameworks concatenated
- No files written to disk (no artifacts/ directory)
- No user confirmation for model selection (skip adaptive gate)
- Agent's analysis returned inline to calling context
</success_criteria>
