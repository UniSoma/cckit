---
description: Investigate a topic from multiple perspectives with adaptive evaluation
argument-hint: [topic to investigate]
allowed-tools: Read, Write, Bash, Glob, Grep, Task, AskUserQuestion
---

<objective>
Investigate $ARGUMENTS using dynamically generated perspectives, parallel research, thematic synthesis, and iterative quality evaluation.

This command is the single entry point for multi-perspective investigation. It orchestrates:
1. LLM-native decomposition into perspectives
2. User confirmation of the investigation plan
3. Parallel researcher spawning with adaptive model selection
4. Monitoring with retry and graceful degradation
5. Theme-organized synthesis
6. Evaluation with adaptive re-research (1-3 iterations)
</objective>

<input_handling>

Check for topic in $ARGUMENTS:
- If empty or missing: Use AskUserQuestion to ask "What would you like to investigate?" with header "Topic" and options:
  - "Technology decision" (description: "Compare technologies, frameworks, or tools")
  - "Strategic analysis" (description: "Market, competitive, or business analysis")
  - "Problem investigation" (description: "Deep dive into a specific problem or challenge")
  - "Custom topic" (description: "Describe your own investigation topic")
  Then use the answer combined with any follow-up to form the TOPIC.
- If provided: Use $ARGUMENTS as TOPIC.

</input_handling>

<decomposition>

Analyze the TOPIC and produce a structured investigation plan. This is LLM-native decomposition — no frameworks or templates.

Think through:
1. **Core question**: What does the user actually want to know? Restate as a clear, answerable question.
2. **Key dimensions**: What angles or dimensions matter for answering this question? Consider:
   - Technical feasibility and implementation
   - Market landscape and alternatives
   - Costs, risks, trade-offs
   - User/stakeholder impact
   - Strategic or long-term implications
   - **Current state / existing implementation**: What exists in this codebase that's relevant? (Consider when topic uses words like "our", "my", "existing", "current", "migrate", "refactor", "integrate", or refers to specific project components)
3. **Perspectives**: For each dimension that matters, define a research perspective:
   - **Name**: A clear, descriptive name (e.g., "Technical Feasibility", "Market Landscape", "Cost-Benefit Analysis")
   - **Focus**: What this perspective investigates
   - **Sub-question**: The specific question this perspective answers
   - **Model tier**: `haiku` for straightforward factual research, `sonnet` for complex analysis requiring nuanced judgment (risk assessment, strategic analysis, critical evaluation, cross-domain synthesis)

**Guidelines**:
- Generate 2-5 perspectives (not more — quality over breadth)
- Each perspective should be distinct (minimal overlap)
- At least one perspective should challenge or stress-test the others
- Select `sonnet` tier only for perspectives that genuinely need deeper analysis
- If the topic relates to implementation decisions for this codebase, consider generating a "Current State Analysis" or "Existing Implementation" perspective that uses local file exploration (Glob, Grep, Read) instead of web research
- A codebase-focused perspective should have focus like "Analyze existing patterns, dependencies, and architecture relevant to {topic}"

</decomposition>

<show_and_confirm>

Present the investigation plan to the user for confirmation using AskUserQuestion:

Format the question as:
```
I'll investigate "{TOPIC}" from these perspectives:

1. {Perspective 1 Name} ({model_tier})
   Focus: {focus}
   Question: {sub_question}

2. {Perspective 2 Name} ({model_tier})
   Focus: {focus}
   Question: {sub_question}

[... for each perspective]

Core question: {core_question}

Would you like to proceed with this plan?
```

Options:
- "Proceed" (description: "Start the investigation with these perspectives")
- "Add a perspective" (description: "I want to add another angle to investigate")
- "Remove a perspective" (description: "One of these perspectives isn't needed")
- "Modify" (description: "I want to change the focus or approach")

If the user selects anything other than "Proceed":
- Apply their feedback to modify the perspectives
- Show the updated plan again for confirmation
- Repeat until the user confirms

</show_and_confirm>

<session_setup>

After user confirms, create the session directory:

```bash
TOPIC_SLUG=$(echo "{TOPIC}" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9]/-/g' | sed 's/--*/-/g' | sed 's/^-//' | sed 's/-$//' | head -c 50)
SESSION_ID="$(date +%Y%m%d-%H%M)-${TOPIC_SLUG}"
SESSION_DIR="$(pwd)/artifacts/investigate/${SESSION_ID}"
mkdir -p "$SESSION_DIR"
```

Save SESSION_ID, SESSION_DIR, and the confirmed perspective list.

Announce:
```
Starting investigation: {TOPIC}
Session: {SESSION_ID}
Directory: {SESSION_DIR}
Perspectives: {N} researchers ({count haiku} haiku, {count sonnet} sonnet)
```

</session_setup>

<parallel_spawning>

Spawn all researchers using synchronous Task calls. Multiple Task calls in a single message run in parallel automatically.

For each perspective, choose the agent based on model tier:
- `haiku` tier → use agent `investigate:researcher`
- `sonnet` tier → use agent `investigate:researcher-deep`

Generate the perspective slug from the name: lowercase, replace spaces/special chars with hyphens.

**IMPORTANT**: Spawn ALL researchers in a SINGLE message with multiple Task tool calls for true parallelism.

**CRITICAL**: Do NOT use `run_in_background: true`. Synchronous Task calls return only the agent's final message (`DONE: {path}`), not the full transcript. This keeps orchestrator context minimal.

For each perspective, use this prompt format:

```
PERSPECTIVE: {perspective_name}
FOCUS: {focus}
SUB_QUESTION: {sub_question}
TOPIC: {topic}
OUTPUT: {SESSION_DIR}/{perspective-slug}-raw.md
SESSION: {SESSION_ID}

Execute research from the {perspective_name} perspective and write results to the OUTPUT path.
```

Task tool parameters:
- `subagent_type`: `"investigate:researcher"` (haiku) or `"investigate:researcher-deep"` (sonnet)
- `description`: `"Research {perspective_name} perspective"`
- Do NOT set `run_in_background` (defaults to false)

All researchers run in parallel. Task blocks until all complete.
**No polling.** No background agents. No TaskOutput loops.

</parallel_spawning>

<monitoring>

Synchronous Task calls block and return the agent's final message directly. No TaskOutput needed.

**Result handling**:

Each Task call returns one of:
- `DONE: {path}` — research completed successfully
- Error message — research failed

Track results for each perspective:
- **completed**: Task returned `DONE: {path}`
- **failed**: Task returned error or unexpected output

**Retry logic**: For each failed perspective:

1. First, check if output file exists with Glob: `{SESSION_DIR}/{perspective-slug}-raw.md`
   - If file exists: mark as **completed** (agent wrote file but returned unexpected message)
   - If file missing: proceed with retry

2. If retry needed:
   - Spawn the same perspective again with the same parameters (synchronous Task)
   - If retry also fails AND file still missing after Glob check, mark as **degraded** permanently

**Minimum requirement**: At least 1 perspective must complete. If ALL fail after retries:
- Report failure with session path
- Suggest retrying with simpler topic or fewer perspectives
- Stop execution (do not proceed to synthesis)

After all research completes, report status:
```
Research complete:
- {Perspective 1}: completed
- {Perspective 2}: completed
- {Perspective 3}: degraded (failed after retry)
```

</monitoring>

<synthesis>

Invoke the synthesizer synchronously (run_in_background: false).

Build the PERSPECTIVES JSON from monitoring results:
```json
[
  {"name": "Database Performance", "slug": "database-performance", "status": "completed"},
  {"name": "Cost Analysis", "slug": "cost-analysis", "status": "degraded", "reason": "timeout after retry"}
]
```

```
Task tool:
  subagent_type: "investigate:synthesizer"
  description: "Synthesize investigation findings"
  prompt: |
    SESSION_DIR: {SESSION_DIR}
    TOPIC: {TOPIC}
    CORE_QUESTION: {core_question}
    PERSPECTIVES: {JSON array}

    Read all *-raw.md files in the session directory.
    Synthesize findings into a theme-organized REPORT.md.
    Handle degraded perspectives by documenting gaps.
  run_in_background: false
```

Wait for synthesis to complete.

</synthesis>

<evaluation_loop>

After synthesis, enter the evaluation-iteration loop.

**Iteration 1**:

Invoke the evaluator synchronously:

```
Task tool:
  subagent_type: "investigate:evaluator"
  description: "Evaluate investigation quality (iteration 1)"
  prompt: |
    SESSION_DIR: {SESSION_DIR}
    TOPIC: {TOPIC}
    CORE_QUESTION: {core_question}
    PERSPECTIVES: {JSON array of all perspectives so far}
    ITERATION: 1

    Evaluate the quality of REPORT.md and raw outputs.
    Write EVALUATION.md with verdict (ACCEPT or RE_RESEARCH).
  run_in_background: false
```

After evaluator completes, read EVALUATION.md:
- Use Grep to find the verdict line: search for `## Verdict:` in `{SESSION_DIR}/EVALUATION.md`

**If ACCEPT**: Proceed to final report.

**If RE_RESEARCH and iteration < 3**:
1. Parse RE_RESEARCH directives from EVALUATION.md (perspective definitions)
2. Spawn new researchers for the gap-filling perspectives (same spawning pattern: synchronous parallel Task calls)
3. Track results from Task returns (same pattern: check for `DONE:`, Glob check before retry)
4. Add new perspectives to the PERSPECTIVES list
5. Re-invoke synthesizer with updated PERSPECTIVES (include both original and new)
6. Re-invoke evaluator with incremented ITERATION number
7. Check verdict again

**Iteration 2-3**: Same pattern. If iteration reaches 3, evaluator will ACCEPT regardless.

</evaluation_loop>

<final_report>

After ACCEPT verdict (or iteration 3 forced accept), present results:

Read the verdict and quality scores from EVALUATION.md using Grep:
- Search for `**Aggregate:**` to get overall score
- Search for `| Groundedness |` to get individual scores

Present to user:

```
Investigation complete!

Session: {SESSION_ID}
Directory: {SESSION_DIR}

Quality: {Aggregate score}/1.0
- Groundedness: {PASS/PARTIAL/FAIL}
- Coverage: {PASS/PARTIAL/FAIL}
- Synthesis Quality: {PASS/PARTIAL/FAIL}
Iterations: {iteration_count}

Files:
- REPORT.md (main findings)
- EVALUATION.md (quality assessment)
- {perspective-1-slug}-raw.md
- {perspective-2-slug}-raw.md
- ...

{If any degraded perspectives:}
Degraded perspectives:
- {name}: {reason}
```

</final_report>

<error_handling>

**All researchers fail (0 outputs after retries)**:
```
Investigation failed: No perspectives completed.

Session: {SESSION_ID}
Core question: {core_question}
Perspectives attempted: {list}

Suggested next steps:
1. Try with a simpler or more focused topic
2. Try with fewer perspectives (2-3 instead of {N})
3. Check for connectivity issues
```
Stop execution. Do not proceed to synthesis.

**Synthesizer fails**:
```
Synthesis failed, but raw research is available.

Session: {SESSION_DIR}
Raw outputs:
- {list of completed *-raw.md files}

You can read the raw files directly for research findings.
```
Stop execution. Do not proceed to evaluation.

**Evaluator fails**:
Report synthesis results without quality metrics:
```
Investigation complete (quality evaluation unavailable).

Session: {SESSION_ID}
REPORT.md: {SESSION_DIR}/REPORT.md

Note: Quality evaluation could not be completed.
Raw outputs and synthesis are available in the session directory.
```

**Retry failure**: After a single retry failure, mark perspective as degraded and continue with remaining perspectives.

</error_handling>

<success_criteria>
- Topic decomposed into 2-5 distinct perspectives using LLM-native reasoning
- User confirmed perspectives before research begins
- All researchers spawned in parallel (single message with multiple Task calls)
- Failed researchers retried once, then gracefully degraded
- Synthesis produces theme-organized REPORT.md (not per-perspective)
- Evaluator issues ACCEPT or RE_RESEARCH with specific gap-filling directives
- RE_RESEARCH triggers new researcher spawning + re-synthesis + re-evaluation
- Maximum 3 iterations before forced acceptance
- Final report shows session path, quality metrics, and file listing
- All errors handled gracefully with useful information to the user
</success_criteria>
