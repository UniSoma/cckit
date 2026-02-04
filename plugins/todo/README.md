# Todo Plugin

Capture ideas mid-conversation without derailing your current work. Use `/todo:add` to save a thought with full context, then `/todo:check` later to resume where you left off.

## Commands

### `/todo:add [optional description]`

Captures the current conversation context as a structured todo item in `TO-DOS.md`.

**What it captures:**
- Specific problem or task from conversation
- File paths with line numbers
- Technical details (error messages, root causes, constraints)
- Timestamp and context title
- Solution hints, if available

**Usage:**
- `/todo:add` - Infers todo from current conversation
- `/todo:add fix authentication bug` - Uses provided description as focus

### `/todo:check`

Lists todos, lets you select one, loads full context, and removes it from the list.

**What it does:**
1. Shows compact numbered list of todos with dates
2. Waits for selection
3. Loads full todo context (Problem/Files/Solution)
4. Checks for project workflows (CLAUDE.md, skills)
5. Suggests relevant workflow if found
6. Removes todo from list
7. Ready to start work

## Example Workflow

**Mid-conversation capture:**
```
You: "Fix the login redirect bug"
Claude: [investigating auth.ts, finds the issue]
You: "Actually, I notice the error handling here is messy too.
      Let's just fix the redirect for now."
You: /todo:add refactor error handling

[stays focused on login redirect, doesn't derail]
```

**Later that week:**
```
You: /todo:check

Outstanding Todos:

1. Refactor error handling in auth flow (2025-11-15 14:23)
2. Add user preference caching (2025-11-14 09:30)

Reply with number: 1

[Removes from list, starts work with full context]
```

## File Structure

**Per-project (created automatically in working directory):**
```
/your/project/
  TO-DOS.md              # Project-specific todos
```

Each project maintains its own todo list. The commands are global, the todos are local.

## Attribution

This plugin is based on the todo management system from [TACHES](https://github.com/glittercowboy/taches-cc-resources).

Watch the full explanation: [YouTube](https://youtu.be/SAhOHNpdDa8)
