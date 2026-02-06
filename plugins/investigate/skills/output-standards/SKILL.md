---
name: output-standards
description: Format requirements and quality standards for investigation outputs including raw findings and synthesized reports.
user-invocable: false
---

# Investigation Output Standards

Format requirements and quality standards for all investigation outputs, ensuring consistency across dynamic perspectives and modes.

## Output File Types

### Raw Findings (from researcher agents)
**Location**: `artifacts/investigate/{session-id}/{perspective-slug}-raw.md`
**Producer**: Researcher agents
**Consumer**: Synthesizer agent + user (review)

### Final Report (from synthesizer)
**Location**: `artifacts/investigate/{session-id}/REPORT.md`
**Producer**: Synthesizer agent
**Consumer**: User (primary output)

## Required Sections: Raw Findings

Every raw research output MUST include these sections:

### 1. Title (H1)
```markdown
# {Perspective Name} Research: {Topic}
```

### 2. Research Parameters
```markdown
## Research Parameters

**Topic**: {research question}
**Perspective**: {perspective name}
**Focus**: {what this perspective investigates}
**Date**: {YYYY-MM-DD}
**Session**: {session-id}
```

### 3. Key Findings
```markdown
## Key Findings

- {Finding 1 with confidence language}
- {Finding 2 with confidence language}
- {Finding 3 with confidence language}
```

**Requirements**:
- 2-5 bullet points (force prioritization)
- Each uses appropriate confidence language from source-evaluation skill
- Specific and actionable, not vague
- No preamble text before bullets

### 4. Analysis (Free-Form Body)

Structure the analysis section based on the perspective's focus area. No fixed template — organize content logically for the investigation angle:

- Use H3 (###) for subsections
- Include citations inline
- Apply confidence language throughout
- Organize by theme, comparison, chronology, or whatever structure best serves the perspective

### 5. Sources
```markdown
## Sources

**Primary Sources** (Tier 1):
- [Source Title](URL)

**Expert Analysis** (Tier 2):
- [Source Title](URL)

**Metrics and Trends** (Tier 3):
- [Source Title](URL)
```

**Requirements**:
- Group by tier
- Minimum 3 sources
- Descriptive titles, not bare URLs
- Tier mix appropriate to perspective focus (see source-evaluation skill)

### 6. Confidence Assessment
```markdown
## Confidence Assessment

**Overall Confidence**: {High | Moderate | Limited | Low}

**Factors**:
- {Factor affecting confidence}
- {Factor affecting confidence}

**Gaps**: {What's missing or uncertain}
```

## REPORT.md Format

The synthesized report is organized by **theme**, not by perspective:

```markdown
# Investigation Report: {Topic}

**Session:** {session-id}
**Date:** {YYYY-MM-DD}
**Perspectives:** {N} completed, {M} degraded

## Executive Summary

{2-3 paragraphs: key findings, consensus view, main recommendation, notable gaps}

## Key Findings

{3-7 thematic findings, each citing supporting perspectives}

- **{Finding}** — supported by {Perspective A}, {Perspective B}. {Brief evidence}.

## Detailed Analysis

### {Theme 1}

{Cross-perspective analysis organized by theme, not by perspective.
Draw connections between what different perspectives found.
Cite perspectives inline: "The technical assessment found X, while
the market analysis suggests Y."}

### {Theme 2}

{Continue thematic organization...}

## Tensions & Trade-offs

{Where perspectives disagree or reveal trade-offs}

- **{Tension}**: {Perspective A} finds {X}, but {Perspective B} raises {Y}
  - **Context**: {Why both are valid}
  - **Resolution path**: {Suggested next step}

## Gaps & Limitations

{What's missing from the investigation}

- **{Gap}**: {What's missing and why it matters}
- **Degraded perspectives**: {List any that failed, with context}

## Recommendations

{Actionable recommendations drawing from multiple perspectives}

1. {Recommendation with supporting evidence from perspectives}
2. {Recommendation}

## Sources

{Combined from all perspectives, using [Perspective: Source](URL) format}

## Confidence Assessment

**Overall Confidence**: {High | Moderate | Limited | Low}
**Strongest areas**: {Where evidence converges}
**Weakest areas**: {Where gaps remain}
```

## File Naming Conventions

### Session Directory
`artifacts/investigate/{timestamp}-{topic-slug}/`

**Timestamp format**: `YYYYMMDD-HHMM`
**Topic slug**: kebab-case, 2-4 words, concise

### Files Within Session
- `{perspective-slug}-raw.md` — raw researcher output
- `REPORT.md` — synthesized report

**Perspective slug**: kebab-case of perspective name (e.g., "market-analysis", "technical-feasibility")

## Quality Checklist

Before finalizing any output:

### Structure
- [ ] Title follows format
- [ ] All required sections present
- [ ] Headings use correct hierarchy (H1 > H2 > H3)

### Content Quality
- [ ] Key Findings are specific and actionable (2-5 bullets)
- [ ] Confidence language matches source quality
- [ ] Citations are inline, not footnotes
- [ ] No unsupported claims
- [ ] Logically organized

### Sources
- [ ] Minimum 3 sources
- [ ] Grouped by tier
- [ ] Descriptive titles with URLs
- [ ] Consistent citation format

### Confidence
- [ ] Overall confidence rating assigned
- [ ] Factors listed
- [ ] Gaps acknowledged

## Anti-Patterns

### Structure
- **Wall of text**: Break into sections with headings
- **Footnotes/endnotes**: Use inline citations
- **Orphan sections**: Every section should have substance

### Content
- **Preamble fluff**: "In this research, I will discuss..." — just start
- **Hedging overload**: "It seems like maybe X might possibly..." — be clearer
- **Citation dumping**: Lists of sources without synthesis
- **Unsupported opinions**: "X is the best" without evidence
- **Tutorial regurgitation**: Copying docs instead of synthesizing

### Language
- **Overconfidence**: "X is definitely the best" based on a blog post
- **Underconfidence**: "According to official docs, it appears that possibly..." — docs are authoritative
- **Corporate speak**: "Leverage synergies..." — use plain language

### Sources
- **Single source dependence**: All findings from one source
- **Outdated sources**: Old content for fast-moving tech
- **Tier mismatch**: High confidence language with Tier 4 sources
