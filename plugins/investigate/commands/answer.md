---
description: Answer a question directly - adaptive detail, no files written
argument-hint: <question to answer>
allowed-tools: Read, Glob, Grep, Task, AskUserQuestion
---

<objective>
Answer $ARGUMENTS directly with adaptive detail. No files are written to disk — the research findings are returned inline.

Unlike `/investigate:quick` which writes findings to a file and returns a brief summary, this command:
- Returns the full answer directly (no file output)
- Adapts response length to question complexity
- Minimal overhead — no session directories, no artifacts
</objective>

<input_handling>

Check for topic in $ARGUMENTS:
- If empty or missing: Use AskUserQuestion to ask "What do you want to know?" with header "Question" and options:
  - "How-to question" (description: "How to implement or accomplish something")
  - "Comparison" (description: "Compare options, tools, or approaches")
  - "Explanation" (description: "Understand a concept, technology, or pattern")
  - "Custom question" (description: "Ask your own question")
  Then use the answer combined with any follow-up to form the TOPIC.
- If provided: Use $ARGUMENTS as TOPIC.

</input_handling>

<smart_clarification>

After obtaining TOPIC, check if it's underspecified:

**Triggers for clarification** (ask ONLY if topic is extremely vague):
- Topic is a single word (e.g., "databases", "React")
- Topic is a bare noun with no context or implied question

**Skip clarification if** (default — most inputs skip):
- Topic is a question ("How does X work?")
- Topic has any context ("best database for time-series data")
- Topic is 3+ words with a clear subject
- Topic mentions specific technologies with implied goal

**If clarification needed**, use AskUserQuestion:
- header: "Focus"
- question: "What specifically about '{TOPIC}' do you want to know?"
- options (generate 3-4 contextually relevant options based on the topic)

Update TOPIC with the clarification.

</smart_clarification>

<model_selection>

Determine which model to use based on topic characteristics:

**Use sonnet if topic involves**:
- Comparisons with tradeoffs ("X vs Y", "compare", "which is better")
- Architectural or strategic decisions ("should we", "best approach for")
- Risk or security analysis ("security implications", "risks of")
- Complex tradeoff analysis ("pros and cons", "tradeoffs")
- Abstract concepts requiring synthesis

**Use haiku for**:
- Factual how-to questions ("how to", "steps to", "guide for")
- Simple explanations ("what is", "explain", "define")
- Lookup-style queries ("syntax for", "API for", "example of")
- Well-defined technical questions with clear answers

Default to haiku if uncertain.

Save the selected model as MODEL_TIER ("haiku" or "sonnet").

</model_selection>

<research>

Spawn the researcher agent with the selected model:

```
Task tool:
  subagent_type: "investigate:answer-researcher"
  model: {MODEL_TIER}  # "haiku" or "sonnet"
  description: "Answer: {short topic summary}"
  prompt: |
    TOPIC: {TOPIC}

    Research this topic and return your findings directly. Do not write any files.
  run_in_background: false
```

The agent will:
1. Research the topic using web search and/or codebase tools
2. Return findings directly as its response (no files written)

</research>

<final_report>

The agent's response IS the final output. Display it directly to the user.

After the agent's response, append:

```
---
For deeper analysis, use:
- /investigate:quick {TOPIC} — single-pass research with file output
- /investigate:run {TOPIC} — multi-perspective investigation
```

</final_report>

<error_handling>

**If agent fails or returns empty:**
1. Report the failure
2. Suggest `/investigate:quick` or `/investigate:run` as alternatives

**If topic remains too vague after clarification:**
- Proceed anyway — agent will do best-effort research

</error_handling>

<success_criteria>
- Topic received or requested via AskUserQuestion
- Only extremely vague topics (single words) trigger clarification
- Model selected based on topic complexity
- NO files written to disk — no session directories, no artifacts
- Agent response displayed directly to user
- Suggestion for deeper investigation appended
</success_criteria>
