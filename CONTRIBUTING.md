# Contributing

Thanks for taking the time to contribute! This repository is intended to be used as a GitHub template. Follow the guidelines below to keep contributions consistent and secure.

## Development Workflow

1. Fork or create a repository using this template.
2. Create a feature branch from `main`.
3. Apply changes and add or update tests where relevant.
4. Run `./gradlew lint test` before opening a pull request.
5. Submit a pull request describing the change and referencing related issues.

## Commit Style

- Use present-tense, imperative commit messages (e.g., "Add CI workflow").
- Group related changes into a single commit when possible.
- Reference issues using `Fixes #123` or `Refs #123`.

## Code Style

- Kotlin code adheres to the official Kotlin style guide (`ktlint` compatible).
- XML layout files should use 4-space indentation.
- Prefer dependency injection for shared resources to keep AI-related services testable.

## Security and Agent Guidelines

- Do not commit API keys, client secrets, or keystore files.
- Update `agents/context.yaml` and related documents when introducing new agent-powered capabilities or constraints.
- Review generated code for compliance with privacy and safety requirements.

## Issues & Discussions

- Use descriptive titles.
- Include reproduction steps, expected behaviour, and actual results when reporting bugs.
- For feature ideas, explain the motivation and success criteria.

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
