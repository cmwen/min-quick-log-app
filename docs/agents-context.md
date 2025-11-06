# Agent Context Customization

This template ships with machine-readable context to help AI tooling understand the project's goals, constraints, and coding conventions. Update these files regularly so that AI assistants produce accurate, safe suggestions.

## Files

| Path | Purpose |
| --- | --- |
| `agents/context.yaml` | High-level product charter, goals, non-goals, and the agent catalog. |
| `agents/prompts/coding.md` | Prompt snippets describing coding standards, review expectations, and security guidelines. |
| `agents/prompts/ui.md` | Prompt snippets that help agents evaluate UI quality and accessibility. |
| `docs/architecture.md` | Human-readable architecture overview referenced by `context.yaml`. |

## Editing Guidelines

- Keep statements concise and actionable.
- Use bullet points for goals, anti-goals, and constraints.
- Include links to canonical documentation where relevant.
- Update the context whenever new features or restrictions are introduced.

## Example Workflow

1. **Plan** — Capture the product or feature goals in `agents/context.yaml`.
2. **Design** — Update `docs/architecture.md` or add new documents in `docs/`.
3. **Implement** — Leverage AI tooling with the updated context files to generate code or documentation.
4. **Review** — Ensure changes stay aligned with security and compliance rules defined in the context.

## Recommended Agents

The `agents_catalog` section of `agents/context.yaml` lists starting roles you can enable in your preferred automation platform:

- **android-builder** — Keeps Gradle tasks, tests, and lint checks passing locally and in CI.
- **ui-reviewer** — Audits layouts, accessibility, and Material patterns using `agents/prompts/ui.md`.
- **release-shepherd** — Coordinates version bumps, signing validation, and release workflow execution.

## Version Control

Commit agent context updates alongside the feature work they describe. This creates an audit trail and avoids stale instructions.

## Automation Tips

- Reference `agents/context.yaml` in custom chat prompts for GitHub Copilot Chat or other assistants.
- Consider mirroring the context into your issue templates so contributors are reminded of agent-specific constraints.
- Use `scripts/sync-agent-context.sh` (add this script per project needs) if you maintain external context stores.
