# Android Template Starter

> TODO: Replace this block with a short summary of your application once you create a repository from the template.

This repository is a template you can clone or fork to spin up an Android project with CI/CD, dependency automation, and agent-friendly documentation already wired in. Use the steps below to copy the template and tailor it to your product.

## Use This Template

1. **Create a repository**
   ```bash
   gh repo create <your-org>/<your-app> --template <original-repo>
   git clone git@github.com:<your-org>/<your-app>.git
   cd <your-app>
   ```
2. **Run initial checks**
   ```bash
   ./gradlew lint testDebugUnitTest
   ```
3. **Review automation**
   - `.github/workflows/ci.yml` runs linting, unit tests, and debug builds on every PR.
   - `.github/workflows/release.yml` signs and publishes release artifacts when triggered.
   - `.github/dependabot.yml` keeps Gradle dependencies and GitHub Actions current.
4. **Configure secrets**
   - Generate signing material with `keytool` (see `docs/secrets-management.md` for the recommended command).
   - Upload values listed in `docs/secrets-management.md` as repository secrets or via your GitHub App.
5. **Wire up agents**
   - The `agents/` directory provides context, prompts, and recommended roles (`android-builder`, `ui-reviewer`, `release-shepherd`) to help AI coding assistants reason about the project.
   - Update `agents/context.yaml`, `docs/agents-context.md`, and `AGENTS.md` with app-specific goals.
6. **Customize documentation**
   - `docs/architecture.md` for module design decisions.
   - `docs/template-usage.md` for workflow and release instructions.
   - `CONTRIBUTING.md` for team processes.

When you are ready, replace the placeholder text in this README with product-specific goals, features, and screenshots.

## Project Layout

```
.
├── app/                     # Android application module
├── agents/                  # Agent context, prompts, and presets
├── docs/                    # Extended documentation
├── scripts/                 # Placeholder for custom automation scripts
└── .github/workflows/       # GitHub Actions pipelines
```

Additional guidance lives in `docs/template-usage.md`. Keep the documentation in sync with your code and automation as the project evolves.
