---
name: source-evaluation
description: Four-tier source quality classification, verification checklist, confidence language, and citation standards for research agents.
user-invocable: false
---

# Source Evaluation and Citation Standards

Criteria for evaluating research source quality and formatting citations consistently across all investigation outputs.

## Four-Tier Source Quality Classification

### Tier 1: Primary Authoritative Sources

**Definition**: Official documentation, specifications, and primary research

**Examples**:
- Official documentation (React docs, PostgreSQL manual, AWS docs)
- Technical specifications (RFCs, W3C standards, ISO standards)
- Peer-reviewed academic papers
- Official repository source code (GitHub canonical repos)
- Government/regulatory documentation (GDPR official text, NIST guidelines)

**When to use**:
- Definitive statements ("X requires Y")
- Technical specifications and APIs
- Security/compliance requirements
- Standards and best practices

**Citation format**: `[Official Docs: React](https://react.dev)`

**Confidence language**: "According to the official documentation...", "The specification states...", "Research demonstrates..."

---

### Tier 2: Expert Community Sources

**Definition**: Reputable technical writing, established community resources

**Examples**:
- MDN Web Docs
- Stack Overflow accepted answers (high vote count)
- Well-maintained blogs by recognized experts (e.g., Martin Fowler, Kent C. Dodds)
- Major tech company engineering blogs (Netflix, Stripe, Uber)
- Established tutorial sites (freeCodeCamp, DigitalOcean tutorials)
- Conference talks from major events (JSConf, PyCon, AWS re:Invent)

**When to use**:
- Implementation patterns and examples
- Community best practices
- Comparative analyses
- Real-world use cases

**Citation format**: `[MDN: Promises](https://developer.mozilla.org)`, `[Stack Overflow: JWT best practices](https://stackoverflow.com/...)`

**Confidence language**: "Common practice is...", "Community consensus suggests...", "Widely recommended approach..."

---

### Tier 3: Observational/Comparative Sources

**Definition**: Comparison sites, surveys, general tech media

**Examples**:
- GitHub repository statistics (stars, contributors, activity)
- npm/PyPI package statistics and trends
- Stack Overflow Trends, Google Trends
- Tech survey reports (Stack Overflow Survey, State of JS)
- Comparison sites (G2, Capterra for vendor comparison)
- General tech news (TechCrunch, Ars Technica)
- Product landing pages (vendor claims)

**When to use**:
- Popularity trends
- Feature comparisons
- Market landscape
- Adoption indicators

**Citation format**: `[GitHub: express](https://github.com/expressjs/express) (65k stars)`, `[npm trends: react vs vue](https://npmtrends.com/...)`

**Confidence language**: "Based on current trends...", "Popularity metrics suggest...", "X appears to be more widely adopted..."

---

### Tier 4: Anecdotal/Limited Sources

**Definition**: Individual opinions, unverified claims, limited sample sources

**Examples**:
- Single blog posts (non-expert authors)
- Reddit/forum discussions (multiple opinions)
- Social media posts
- Personal project repos (not widely used)
- Outdated articles (>3 years for fast-moving tech)
- Marketing content without substantiation

**When to use**:
- Illustrative examples
- Emerging/experimental approaches
- Niche use cases
- When better sources unavailable

**Citation format**: `[Blog: Author Name](URL)`, `[Reddit discussion: r/webdev](URL)`

**Confidence language**: "Some developers report...", "Anecdotal evidence suggests...", "One approach could be...", "Limited information available..."

---

## Source Verification Checklist

Before citing any source, verify:

### Authority
- [ ] **Author credentials**: Who wrote this? Are they recognized in the field?
- [ ] **Organization reputation**: Is the publisher reputable?
- [ ] **Primary vs secondary**: Is this original research or reporting on others' work?

### Currency
- [ ] **Publication date**: When was this written?
- [ ] **Still relevant**: Has the technology/approach changed since publication?
- [ ] **Update frequency**: Is this maintained/updated documentation or stale content?

### Accuracy
- [ ] **Can you verify claims**: Are assertions backed by evidence?
- [ ] **Cross-reference**: Do other reputable sources confirm this information?
- [ ] **Technical correctness**: Does the code/approach actually work?

### Objectivity
- [ ] **Conflicts of interest**: Is this vendor marketing or neutral analysis?
- [ ] **Balanced perspective**: Does it acknowledge tradeoffs and limitations?
- [ ] **Evidence-based**: Are claims supported by data or just opinions?

### Coverage
- [ ] **Scope appropriate**: Does the source cover the specific topic you need?
- [ ] **Depth sufficient**: Is there enough detail for your use case?
- [ ] **Context provided**: Does it explain when/why/how to apply the information?

## Perspective-Specific Source Priorities

Match source tier priority to the nature of the research perspective:

- **Implementation-focused perspectives**: Prioritize Tier 1 (official docs) + Tier 2 (community examples). Implementation accuracy is critical.
- **Landscape/ecosystem perspectives**: Prioritize Tier 3 (trends, comparisons) + Tier 2 (expert overviews). Mapping the space requires adoption data.
- **Historical perspectives**: Prioritize Tier 1 (original papers, announcements) + Tier 2 (retrospectives). Historical accuracy requires primary sources.
- **Comparative perspectives**: Prioritize Tier 1 (official docs for specs) + Tier 3 (comparisons, reviews). Need vendor claims plus market perception.
- **Feasibility/reality-check perspectives**: Prioritize Tier 2 (expert experience) + Tier 3 (adoption data). Real-world experience matters most.
- **Risk/security perspectives**: Higher bar. Use Tier 1 (official/authoritative) whenever possible. Security claims require multiple authoritative sources.
- **Financial/strategic perspectives**: Prioritize Tier 1 (regulatory/official data) + Tier 2 (analyst reports). Financial claims need verifiable data.
- **Emerging/novel perspectives**: Accept lower tier sources (Tier 3-4) when better sources don't exist yet. Acknowledge limited availability.

## Confidence Language Summary

| Source Quality | Language |
|---------------|----------|
| High (Tier 1) | "The specification requires...", "Official documentation states...", "Research demonstrates..." |
| Moderate (Tier 2) | "Best practices recommend...", "Community consensus suggests...", "Widely accepted approach..." |
| Limited (Tier 3) | "Current trends show...", "Popular options include...", "Based on adoption metrics..." |
| Low (Tier 4) | "Some developers report...", "Anecdotal evidence suggests...", "Limited information indicates..." |
| None available | "Information not readily available...", "Would require further investigation..." |

**Critical**: Never overstate confidence. "Possibly" is better than wrong certainty.

## Red Flags: When NOT to Use a Source

### Outdated Information
- Technology docs >3 years old (unless foundational/stable)
- Security advice >1 year old
- Framework guides from previous major version

### Unverifiable Claims
- "Fastest framework" without benchmarks
- "Most secure" without evidence
- "Industry standard" without citation

### Obvious Bias
- Vendor marketing without acknowledging bias
- Single-vendor comparisons
- Paid placements without disclosure

### Poor Quality Indicators
- Excessive grammatical errors
- No author attribution
- No publication date
- Broken code examples
- Contradicts authoritative sources without explanation

## When Sources Conflict

1. **Tier trumps**: Higher-tier source wins unless outdated
2. **Currency matters**: More recent source preferred for fast-moving topics
3. **Present both**: If reputable sources conflict, show both perspectives with context
4. **Context dependency**: Explain when each source's advice applies

## Citation Format Standards

### Basic Pattern
```
[Source Type: Specific Page/Section](URL)
```

### In-Context Citation
Integrate citations naturally into findings:

**Good**:
> According to the [Official React Documentation](https://react.dev), hooks were introduced in React 16.8. The community widely adopted this pattern, with [Stack Overflow data](URL) showing a 400% increase in hooks-related questions.

**Avoid**:
> Hooks are good. [1][2][3] They make code cleaner. [4][5]

### Sources Section Format
```markdown
## Sources

**Primary Sources** (Tier 1):
- [Source Title](URL)

**Expert Analysis** (Tier 2):
- [Source Title](URL)

**Metrics and Trends** (Tier 3):
- [Source Title](URL)
```

## Quality Over Quantity

**Prefer**: 5 high-quality Tier 1-2 sources over 20 low-quality Tier 4 sources

**Avoid**:
- Citing the same source repeatedly
- Over-citing obvious information
- Citation dumping (list of sources without synthesis)
