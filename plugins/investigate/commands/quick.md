---
description: Quick investigation - single researcher, smart model selection, direct output
argument-hint: <topic to investigate>
allowed-tools: Read, Write, Bash, Glob, Grep, Task, AskUserQuestion
---

<objective>
Perform a fast, single-pass investigation of $ARGUMENTS with minimal overhead.

Unlike the full `/investigate:run` which decomposes into multiple perspectives, confirms with the user, and iterates with evaluation, this command:
- Researches directly (no decomposition into perspectives)
- Asks for clarification only if topic is vague
- Single researcher pass with smart model selection
- No evaluation or re-research loop
- Outputs a single markdown file + inline summary
</objective>

<input_handling>

Check for topic in $ARGUMENTS:
- If empty or missing: Use AskUserQuestion to ask "What would you like to quickly investigate?" with header "Topic" and options:
  - "How-to question" (description: "How to implement or accomplish something")
  - "Comparison" (description: "Compare options, tools, or approaches")
  - "Explanation" (description: "Understand a concept, technology, or pattern")
  - "Custom topic" (description: "Describe your own investigation topic")
  Then use the answer combined with any follow-up to form the TOPIC.
- If provided: Use $ARGUMENTS as TOPIC.

</input_handling>

<smart_clarification>

After obtaining TOPIC, check if it's underspecified:

**Triggers for clarification** (ask if ANY apply):
- Topic is 1-2 words only (e.g., "databases", "React")
- Topic is a bare noun without context (e.g., "authentication", "caching")
- Topic lacks a clear question or goal

**Skip clarification if**:
- Topic is a clear question ("How does X work?", "What's the difference between X and Y?")
- Topic has specific context ("best database for time-series IoT data")
- Topic mentions specific technologies with a goal ("migrate from Redux to Zustand")

**If clarification needed**, use AskUserQuestion:
- header: "Focus"
- question: "What specifically about '{TOPIC}' do you want to know?"
- options (generate 3-4 contextually relevant options based on the topic):
  - For technologies: "How it works", "When to use it", "Compare alternatives", "Implementation guide"
  - For concepts: "Explain the concept", "Best practices", "Common pitfalls", "Real-world examples"
  - For decisions: "Pros and cons", "When to choose this", "Alternatives", "Migration path"

Update TOPIC with the clarification.

</smart_clarification>

<model_selection>

Determine which model to use based on topic characteristics:

**Use sonnet if topic involves**:
- Comparisons with tradeoffs ("X vs Y", "compare", "which is better")
- Architectural or strategic decisions ("should we", "best approach for")
- Risk or security analysis ("security implications", "risks of")
- Complex tradeoff analysis ("pros and cons", "tradeoffs")
- Abstract concepts requiring synthesis ("how does X relate to Y")

**Use haiku for**:
- Factual how-to questions ("how to", "steps to", "guide for")
- Simple explanations ("what is", "explain", "define")
- Lookup-style queries ("syntax for", "API for", "example of")
- Well-defined technical questions with clear answers

Default to haiku if uncertain — it handles most queries well.

Save the selected model as MODEL_TIER ("haiku" or "sonnet").

</model_selection>

<session_setup>

Create a lightweight session:

```bash
TOPIC_SLUG=$(echo "{TOPIC}" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9]/-/g' | sed 's/--*/-/g' | sed 's/^-//' | sed 's/-$//' | head -c 50)
SESSION_ID="$(date +%Y%m%d-%H%M)-${TOPIC_SLUG}"
OUTPUT_DIR="$(pwd)/artifacts/investigate"
OUTPUT_FILE="${OUTPUT_DIR}/${SESSION_ID}.md"
mkdir -p "$OUTPUT_DIR"
```

Announce briefly:
```
Quick investigation: {TOPIC}
Session: {SESSION_ID}
Model: {haiku or sonnet based on selection}
```

</session_setup>

<research>

Spawn the researcher agent with the selected model:

```
Task tool:
  subagent_type: "investigate:quick-researcher"
  model: {MODEL_TIER}  # "haiku" or "sonnet"
  description: "Quick research: {short topic summary}"
  prompt: |
    TOPIC: {TOPIC}
    OUTPUT: {OUTPUT_FILE}
    SESSION: {SESSION_ID}

    Research this topic and write findings to the OUTPUT path.
  run_in_background: false
```

The agent will:
1. Research the topic directly
2. Write findings to OUTPUT_FILE
3. Return: `DONE: {path}` followed by a 2-3 sentence summary

</research>

<final_report>

Parse the agent's response to extract:
- The output path (after `DONE:`)
- The summary (lines after the DONE line)

Present to user:

```
Quick investigation complete!

Output: {OUTPUT_FILE}

{Summary from agent response}

For deeper analysis with multiple perspectives, use:
/investigate:run {TOPIC}
```

</final_report>

<error_handling>

**If agent fails or returns unexpected output:**
1. Check if OUTPUT_FILE was written using Glob
2. If file exists: report success with path, note summary unavailable
3. If file missing: report failure, suggest `/investigate:run` or topic refinement

**If topic remains too vague after clarification:**
- Proceed anyway — agent will do best-effort research
- Note in output that results may be general

</error_handling>

<success_criteria>
- Topic received or requested via AskUserQuestion
- Vague topics trigger smart clarification (1-2 words, bare nouns)
- Model selected based on topic complexity (sonnet for comparisons/tradeoffs, haiku for factual)
- Session directory created
- Researcher agent spawned and completes
- Summary displayed inline, full findings written to disk
- Suggestion for deeper investigation included
</success_criteria>
