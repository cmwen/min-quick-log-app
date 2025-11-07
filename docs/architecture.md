# Architecture Overview

This template provides a modular foundation for Android projects that integrate AI-driven features. The initial setup intentionally keeps the implementation lightweight while documenting the extension points for production systems.

## Modules

- **app** — Primary Android application module that produces the `debug` and `release` build variants. It is configured for Android 14 (API level 34) with Kotlin, uses modern build tooling, and demonstrates dependency injection and AI-aware configuration hooks.
- **agent assets** — The `agents/` directory contains machine-readable context that can be consumed by AI assistants or tooling to understand the project's purpose, coding conventions, and security posture.

Additional modules (e.g., `core`, `feature-*`, `data`) can be added as needed. Update `settings.gradle.kts` and add module-specific `build.gradle.kts` files to extend the template.

## Key Technologies

- **Kotlin** with Jetpack libraries (core-ktx, appcompat, material)
- **AndroidX** navigation and lifecycle base dependencies
- **Gradle Kotlin DSL** for build scripts
- **GitHub Actions** for CI/CD pipelines
- **Dependabot** for automated dependency updates

### Feature-specific additions

- **Room** provides persistent storage for tags, entries, and their relationships. The schema seeds a small tag graph so the app can surface sensible suggestions on first launch.
- **ViewModel + Kotlin Flows** drive UI state (`QuickLogViewModel` combines recent tags, suggestions, entry history, and entry drafts into a single observable stream).
- **Google Play Services Location** exposes the user's current position, which is reverse geocoded (when available) to enrich logs with a place label.
- **Material 3 components** (chips, buttons, cards, toolbar) deliver a responsive light/dark UI without custom styling, and the entries overview uses `TabLayout` + `ViewPager2` for quick pivots.
- **Branding assets** live in `design/` (SVG source) and `app/src/main/res/drawable/` (VectorDrawable) so the same iconography can be reused for launchers or marketing material.
- **Data exploration**: the `EntriesOverviewActivity` provides grouped RecyclerView sections for dates, locations, and tags plus a stats panel, while `TagManagerActivity` renders the user-defined tag graph and lets users connect tags bidirectionally.

## Build Configuration

- `compileSdk` / `targetSdk` set to 34 for Android 14 compatibility
- `minSdk` set to 24 for broad device coverage
- Java 17 toolchain via Gradle configuration
- Unit testing using JUnit 4 with placeholder tests

## Agent Integration Hooks

- `agents/context.yaml` summarises product goals and operating constraints for AI agents.
- `agents/prompts/` contains reusable prompt fragments for tooling or LLM-backed automations.
- `docs/agents-context.md` explains how to customize the agent metadata and keep roles aligned.

## Release Flow

1. Developers merge changes into `main`.
2. CI workflow runs lint, unit tests, and builds artifacts.
3. Release workflow triggered manually or via Git tags:
   - Builds signed `release` APK using the GitHub secrets.
   - Publishes artifacts to the GitHub release page.
4. Optional: extend release pipeline to push to Play Console using Play Developer API.

## Next Steps

- Configure feature modules, DI framework (e.g., Hilt), and data layer based on project needs.
- Integrate concrete AI services (OpenAI, Vertex AI, Azure OpenAI) using secrets managed through GitHub.
- Update documentation as the architecture evolves to keep the agent context synchronized.
