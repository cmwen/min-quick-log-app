# Android Dependency Guardian Agent

This repository now includes a custom Copilot coding agent that keeps the Gradle dependency set evergreen and build-ready.

## Agent profile

- Location: `.github/agents/android-dependency-guardian.agent.md`
- Name: `android-dependency-guardian`
- Tools: `read`, `search`, `edit`, `run`, `list`
- Target: GitHub Copilot chat and agents UI (`github-copilot`)

## Intent

The agent specializes in Android dependency hygiene, which means it:

1. Audits `gradle/libs.versions.toml`, module `build.gradle.kts` files, and shared version catalogs.
2. Uses the built-in `search` tool to verify stable releases (AndroidX, Compose, Kotlin, third-party SDKs) before proposing upgrades.
3. Runs Gradle tasks such as `./gradlew build` (or a scoped equivalent) to confirm the project still builds after dependency changes.
4. Documents rationale for every upgrade, including downstream or transitive effects and safety checkpoints.

## How to enable and use the agent

1. Follow the instructions from the GitHub Docs article on [creating custom agents](https://docs.github.com/en/copilot/how-tos/use-copilot-agents/coding-agent/create-custom-agents). When creating the profile in the repository, point the template to `.github/agents/android-dependency-guardian.agent.md`.
2. In the Copilot coding agent dropdown (on GitHub.com or VS Code), select **Android Dependency Guardian** before you start a dependency-related task.
3. Ask the agent to perform tasks such as "Audit Android dependencies for updates", "Check compose BOM versions", or "Ensure Gradle dependencies build cleanly".
4. The agent will reference the instructions described in its profile and respond with the findings, suggested version updates, and verification commands.

## Keeping results evergreen

- When new AndroidX, Kotlin, Compose, or third-party versions first reach stable status, prompt the agent to review them and describe the upgrade path.
- After any dependency change, have the agent rerun the relevant Gradle task that previously signaled success to make sure no regressions were introduced.
- Document the commands (such as `./gradlew build` or `./gradlew :app:dependencies`) used to verify results in your issue or PR summary so reviewers can re-run them easily.