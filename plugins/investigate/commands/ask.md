---
description: Clarify investigation needs through adaptive questioning before running
argument-hint: [rough idea or leave blank]
allowed-tools: Read, Task, AskUserQuestion, Skill
---

<objective>
Gather and clarify investigation needs through adaptive questioning, then decide whether to proceed with `/investigate:run`.

This command is a pre-investigation intake process that:
1. Accepts vague or rough ideas and refines them
2. Explores the problem space through multi-step discovery
3. Identifies scope, assumptions, stakeholders, and constraints
4. Evaluates whether investigation is the right approach
5. Offers a decision gate with clear options
</objective>

<input_handling>

Check for input in $ARGUMENTS:

**If empty or very short (1-2 words)**:
Use AskUserQuestion to gather initial direction:
- Question: "What would you like to explore or investigate?"
- Header: "Topic"
- Options:
  - "I have a rough idea" (description: "I'll describe what I'm thinking about")
  - "Technology decision" (description: "Comparing options or evaluating a tool/approach")
  - "Understanding something" (description: "I want to deeply understand a concept or system")
  - "Problem analysis" (description: "I'm trying to figure out why something isn't working or what to do")

Based on their answer, ask a follow-up to get the actual topic.

**If provided but potentially vague**:
Proceed to context analysis to determine what clarification is needed.

Store the raw input as INITIAL_INPUT for reference.

</input_handling>

<context_analysis>

Analyze INITIAL_INPUT to extract what's known and identify gaps.

**What's known** — extract any information about:
- **What**: The subject matter or problem area
- **Who**: Stakeholders, users, or decision-makers involved
- **Why**: The motivation or goal behind investigating
- **Constraints**: Timeline, technical context, resources, existing commitments
- **Existing code**: Relevant code in this codebase (if applicable)

**Vagueness indicators** — flag the input as needing clarification if:
- Missing scope: "databases" (which aspect? performance? cost? migration?)
- Ambiguous context: could mean multiple things depending on situation
- Multiple interpretations: reasonable people would interpret differently
- No clear goal: investigating for its own sake vs. to make a decision
- Too broad: would require weeks of research to cover thoroughly
- Implementation context missing: topic involves "our" system but no codebase context provided

**Assessment output** (internal, not shown to user):
```
WHAT: [extracted or "unclear"]
WHO: [extracted or "unclear"]
WHY: [extracted or "unclear"]
CONSTRAINTS: [extracted or "none identified"]
VAGUENESS_LEVEL: low | medium | high
GAPS: [list of specific missing information]
```

</context_analysis>

<adaptive_questioning>

Based on the gaps identified, ask clarifying questions using AskUserQuestion. Ask 1-2 questions at a time, not all at once.

**Question priority** (ask in this order, skip if already clear):

### 1. Scope (if WHAT is unclear or too broad)
Question: "What specific aspect of {topic} are you most interested in?"
Provide 3-4 contextual options based on the topic, plus "Other".

Example for "databases":
- "Performance optimization" (description: "Making queries faster, scaling")
- "Migration planning" (description: "Moving from one database to another")
- "Technology selection" (description: "Choosing the right database for a use case")

### 2. Goal/Decision (if WHY is unclear)
Question: "What will this investigation help you decide or understand?"
Options:
- "Make a specific decision" (description: "I need to choose between options")
- "Build understanding" (description: "I want to deeply understand the landscape")
- "Validate an assumption" (description: "I believe X and want to verify")
- "Solve a problem" (description: "Something isn't working and I need to figure out why")

### 3. Assumptions (probe existing beliefs)
Question: "What do you already believe or assume about {refined topic}?"
Options:
- "I have a hypothesis" (description: "I think I know the answer but want validation")
- "I'm starting fresh" (description: "I don't have strong opinions yet")
- "I've tried things" (description: "I've already explored some approaches")

If they have a hypothesis or have tried things, ask a follow-up to capture specifics.

### 4. Stakeholders (if WHO is unclear and relevant)
Only ask if the investigation seems to involve organizational decisions:
Question: "Who will use the findings from this investigation?"
Options:
- "Just me" (description: "Personal learning or individual decision")
- "My team" (description: "Team decision or shared understanding")
- "Leadership/stakeholders" (description: "Needs to inform broader decisions")

### 5. Constraints (if not yet identified)
Question: "Are there constraints I should know about?"
Options:
- "Technical constraints" (description: "Existing stack, integrations, requirements")
- "Timeline pressure" (description: "Need findings by a specific date")
- "No major constraints" (description: "Relatively open exploration")

### 6. Existing Implementation (if topic relates to this codebase)
Only ask if the topic involves implementation decisions (migrations, refactoring, technology choices for this project):

Question: "Is there existing code related to {topic} that I should understand?"
Options:
- "Yes, explore relevant code" (description: "Look at current implementation before refining the topic")
- "I'll describe what exists" (description: "I'll tell you about the current state")
- "Starting fresh" (description: "No existing implementation to consider")

If "Yes, explore relevant code":
- Use the Task tool with `subagent_type: "Explore"` to search the codebase
- Prompt: "Find code related to {topic}. Look for relevant patterns, dependencies, and architecture. Summarize what exists and how it's structured."
- The Explore agent returns a summary without polluting the main context
- Incorporate the agent's findings into the refined topic context

**Adaptive flow**:
- If VAGUENESS_LEVEL is low: Ask 0-1 questions, move to gate evaluation
- If VAGUENESS_LEVEL is medium: Ask 2-3 questions
- If VAGUENESS_LEVEL is high: Ask 3-4 questions across categories
- Stop asking once you have enough to form a clear, focused investigation topic

</adaptive_questioning>

<topic_refinement>

After questioning, synthesize everything into a refined investigation topic.

**Refined topic format**:
```
REFINED_TOPIC: [Clear, focused statement of what to investigate]
CORE_QUESTION: [The specific question the investigation should answer]
CONTEXT: [Relevant constraints, stakeholders, and assumptions]
```

Example transformation:
- Input: "databases"
- After questioning: "PostgreSQL vs. MongoDB for our real-time analytics pipeline, considering our existing Python stack and need to scale to 10M events/day"
- Core question: "Which database better fits our specific requirements for real-time analytics?"

</topic_refinement>

<gate_evaluation>

Before offering the decision gate, evaluate whether investigation is warranted.

**Investigation IS warranted when**:
- Topic requires gathering external information
- Multiple perspectives would add value
- The question can't be answered with a quick search
- Stakes are high enough to justify the effort
- User needs synthesis across sources, not just facts

**Investigation may NOT be warranted when**:
- Question has a definitive, easily-found answer
- User seems to want validation rather than exploration
- Topic is too narrow for multi-perspective research
- A simpler approach would suffice (quick search, reading docs)
- User's actual need is different from what they asked

**Gate recommendation** (internal assessment):
```
RECOMMEND_INVESTIGATION: yes | maybe | no
REASON: [brief explanation]
ALTERNATIVE: [if no/maybe, what simpler approach might work]
```

</gate_evaluation>

<decision_gate>

Present the final decision to the user based on your assessment.

**If RECOMMEND_INVESTIGATION is "yes"**:

Use AskUserQuestion:
- Question: "I've refined your topic to: '{REFINED_TOPIC}'. Core question: '{CORE_QUESTION}'. How would you like to proceed?"
- Header: "Action"
- Options:
  - "Run investigation now" (description: "Start multi-perspective investigation with this refined topic")
  - "Give me the refined topic" (description: "I'll run the investigation manually later")
  - "Let me adjust" (description: "I want to modify the topic or add context")

**If RECOMMEND_INVESTIGATION is "maybe" or "no"**:

Use AskUserQuestion:
- Question: "Based on our discussion, {REASON}. Would you still like to investigate, or try a simpler approach?"
- Header: "Approach"
- Options:
  - "Investigate anyway" (description: "Run full multi-perspective investigation")
  - "Try the simpler approach" (description: "{ALTERNATIVE}")
  - "Give me the refined topic" (description: "I'll decide what to do with it")
  - "Let me reconsider" (description: "I want to think about this differently")

</decision_gate>

<execution>

Based on user's decision:

**"Run investigation now" or "Investigate anyway"**:
Invoke the investigation using the Skill tool:
```
skill: "investigate:run"
args: "{REFINED_TOPIC}. Context: {CONTEXT}"
```

**"Give me the refined topic"**:
Output the refined topic clearly:
```
Refined topic for investigation:

**Topic**: {REFINED_TOPIC}

**Core question**: {CORE_QUESTION}

**Context**: {CONTEXT}

You can run this later with:
/investigate:run {REFINED_TOPIC}
```

**"Try the simpler approach"**:
Provide the recommended alternative directly. For example:
- If the alternative is "quick web search": Perform the search and summarize
- If the alternative is "read the docs": Point to the specific documentation
- If the alternative is "direct answer": Answer the question directly

**"Let me adjust" or "Let me reconsider"**:
Ask what they'd like to change and return to adaptive questioning with the new information.

</execution>

<success_criteria>
- Vague topics are refined into clear, focused investigation questions
- User understands what will be investigated before committing
- Unnecessary investigations are avoided through gate evaluation
- All 3 decision options work correctly
- Handoff to /investigate:run is seamless when chosen
- User can get the refined topic without running investigation
- Alternative approaches are offered when investigation isn't warranted
</success_criteria>
