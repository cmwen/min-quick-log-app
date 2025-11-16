# Coding Prompt Snippets

Use the snippets below to prime AI coding assistants when working on this project. Combine the fragments relevant to the task at hand.

## Quality Gates

- Follow Kotlin official code style; keep functions focused and well-named.
- Add or update unit tests for new logic.
- Ensure the app builds for `debug` and `release` variants.

## Security & Privacy

- Do not log or persist secrets, API keys, or personally identifiable information.
- Store runtime secrets in environment variables or secure storage.
- Ensure AI-powered features display transparency to users and allow opt-out.

## AI Integrations

- Wrap external AI API calls in a repository/service layer for easier testing.
- Enforce request timeouts and error handling to avoid blocking UI threads.
- Cache prompt templates in resources to enable localization.

## Documentation

- Update `agents/context.yaml`, `docs/ARCHITECTURE.md`, and relevant README sections when introducing new capabilities.
- Note any new GitHub secrets or infrastructure requirements in `docs/secrets-management.md`.
- Follow `docs/FEATURE_DEVELOPMENT.md` guide when adding new features.
- Provide reproducible steps for demoing AI-powered features.
