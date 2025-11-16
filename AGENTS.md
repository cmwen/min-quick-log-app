# AI Agent Guidance

This repository includes machine-readable and human-readable context to help AI coding assistants produce high-quality changes. Share this document with every automation tool or agent that can modify the codebase.

## Mission

- Build and maintain an Android 14+ application that showcases AI-first features.
- Keep automation, documentation, and release workflows in sync with the source code.
- Protect sensitive information such as signing keys, API credentials, and user data.

## Source of Truth

- `agents/context.yaml` — canonical goals, non-goals, and security constraints.
- `docs/ARCHITECTURE.md` — comprehensive architecture documentation with actual implementation patterns.
- `docs/CODING_GUIDELINES.md` — Kotlin coding standards and best practices.
- `docs/UI_PATTERNS.md` — UI component guidelines and consistency standards.
- `docs/FEATURE_DEVELOPMENT.md` — step-by-step guide for adding new features.
- `docs/secrets-management.md` — secrets handling policy and GitHub App integration notes.

## Included Agents

- **Android Builder** — Uses `agents/prompts/coding.md` to run checks, update Gradle files, and ensure builds complete locally and in CI.
- **UI Reviewer** — Focuses on layouts, accessibility, and Material guidelines; pairs with `agents/prompts/ui.md` (add more prompts as your design system evolves).
- **Release Shepherd** — Verifies signing configuration, updates version metadata, and walks through `.github/workflows/release.yml`.

## Expectations for AI Agents

1. Prefer incremental, reviewable pull requests with automated tests.
2. Leave existing workflows in a working state; update CI/CD files when modifying build or release logic.
3. Assume secrets are injected at runtime by GitHub Actions via organization-level GitHub Apps—never hardcode secrets.
4. Follow Kotlin and Android best practices, using Kotlin DSL for Gradle configuration.
5. Update relevant documentation alongside code changes.

## Safety and Compliance

- Never commit files from `build/`, `*.keystore`, or other sensitive artifacts.
- Use the provided scripts (`scripts/`) for generating or rotating signing material.
- When unsure about requirements, prefer asking for clarification by opening an issue or drafting a PR with questions.
