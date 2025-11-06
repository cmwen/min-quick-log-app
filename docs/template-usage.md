# Template Usage Guide

This document explains how to work with the AI-aware Android template, including local development options, automation pipelines, and the release process.

## Local Development

- **Prerequisites** — Install Android Studio Giraffe (or newer), Java 17, and the Android 14 (API 34) SDK. To avoid local setup drift, use the supplied Development Container (`.devcontainer/devcontainer.json`) with VS Code or `devcontainer` CLI. The container installs Java 17, Android SDK components, and useful extensions automatically.
- **Gradle wrapper** — The repository includes a configured Gradle wrapper; run any `./gradlew` command (for example `./gradlew tasks`) after cloning to download the required distribution.
- **Common commands**
  - `./gradlew assembleDebug` — builds a debug APK.
  - `./gradlew lint` — runs Android lint checks.
  - `./gradlew testDebugUnitTest` — executes unit tests.
- **Agent context** — When editing features, update `agents/context.yaml`, `docs/agents-context.md`, and `AGENTS.md` as needed so automated agents stay aligned.

## Automation Pipelines

The repository ships with three GitHub Actions workflows located in `.github/workflows/`:

| Workflow | Trigger | Purpose |
| --- | --- | --- |
| `ci.yml` | `push` to `main`, all pull requests | Runs lint, unit tests, and assembles the debug APK to ensure baseline quality. |
| `codeql.yml` | `push` to `main`, all pull requests, weekly schedule | Performs GitHub CodeQL static analysis for Kotlin/Java sources to surface security issues. |
| `release.yml` | `workflow_dispatch`, `push` tags matching `v*` | Builds signed release artifacts, uploads them as workflow artifacts, and publishes a GitHub Release. |

Dependabot (`.github/dependabot.yml`) keeps Gradle dependencies and GitHub Actions versions current via weekly update PRs.

## Release Process

1. **Prepare version metadata** — Decide on the semantic `version_name` (e.g., `1.2.0`) and monotonically increasing `version_code` (e.g., `5`).
2. **Tagging strategy** — The release workflow can create a `v<version_name>` tag automatically when launched manually, or you can push the tag yourself (`git tag v1.2.0 && git push origin v1.2.0`).
3. **Trigger the workflow**
   - Manual run: Navigate to **Actions → Android Release** and start a run with the desired version values.
   - Tag push: Pushing a `v*` tag automatically starts the workflow.
4. **Artifacts and publish**
   - The workflow assembles both the signed APK and App Bundle (`*.aab`), then uploads them as artifacts.
   - When a tag is present, the workflow uses `softprops/action-gh-release` to publish a GitHub Release with the artifacts attached.
5. **Post-release** — Update documentation and changelog (if any), then communicate the release to stakeholders or continue distribution steps (e.g., upload the AAB to Play Console).

## GitHub App-Managed Secrets

The CI and release workflows expect signing credentials and optional API keys at runtime. Instead of storing these as repository secrets, configure an organization-level GitHub App (or GitHub Actions app) with permission to inject secrets into workflows:

1. **Create or configure the App**
   - Permissions: `Actions: read/write`, `Secrets: read`, `Metadata: read`, `Contents: read`.
   - Installation: Install the app on this repository (or organization) and enable access to Actions.
2. **Provision secrets with the App**
   - Store the signing material (see `docs/secrets-management.md`) in the App's secret store.
   - Map each secret name to the workflow expectations:
     - `ANDROID_KEYSTORE_BASE64`
     - `ANDROID_KEYSTORE_PASSWORD`
     - `ANDROID_KEY_ALIAS`
     - `ANDROID_KEY_PASSWORD`
     - Optional: `OPENAI_API_KEY`
3. **Inject secrets at runtime**
   - Use the GitHub App's configuration UI or automation to provide environment variables to Actions runs. Most secret-management apps support temporary environment variables or token exchange.
   - Verify an Actions run includes the injected secrets before triggering a release.
4. **Audit and rotation**
   - Limit App access to trusted administrators.
   - Rotate secrets when team membership or signing keys change and update the App's stored values.

Refer to `docs/secrets-management.md` for detailed instructions on generating and encoding the signing keystore.

## Contributor Experience

- Pull Request template (`.github/pull_request_template.md`) lists checks for tests, docs, and agent context updates.
- Issue templates guide contributors to triage bugs and request enhancements consistently.
- `AGENTS.md` summarizes how AI automation should interact with this repository; link it from any internal agent configurations.
- `agents/context.yaml` ships with starter roles (`android-builder`, `ui-reviewer`, `release-shepherd`) that you can adapt for your automation stack.

Keep this document updated whenever automation or release flows evolve.
