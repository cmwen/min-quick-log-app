# Architecture Overview

**Note:** This file provides a high-level overview. For detailed architecture documentation, see [ARCHITECTURE.md](ARCHITECTURE.md).

Quick Log is a tag-first Android application built with modern Android architecture components following the MVVM pattern.

## Modules

- **app** — Single Android application module that produces the `debug` and `release` build variants. Configured for Android 14 (API level 34, minSdk 24) with Kotlin 1.9+.
- **agent assets** — The `agents/` directory contains machine-readable context for AI assistants including project goals, coding conventions, and architecture guidelines.

The app currently uses a single module structure. Feature modules may be considered in the future for better separation and build performance.

## Key Technologies

- **Kotlin** with Jetpack libraries (core-ktx, appcompat, material)
- **AndroidX** navigation and lifecycle base dependencies
- **Gradle Kotlin DSL** for build scripts
- **GitHub Actions** for CI/CD pipelines
- **Dependabot** for automated dependency updates

### Feature-specific additions

- **Room** provides persistent storage for tags, entries, and their relationships. The schema seeds a small tag graph so the app can surface sensible suggestions on first launch.
- **ViewModel + Kotlin Flows** drive UI state (`QuickLogViewModel` combines recent tags, highly-connected “popular” tags derived from the tag graph, contextual suggestions, entry history, and entry drafts into a single observable stream).
- **Google Play Services Location** exposes the user's current position, which is reverse geocoded (when available) to enrich logs with a place label.
- **Material 3 components** (chips, buttons, cards, toolbar) deliver a responsive light/dark UI without custom styling, and the entries overview uses `TabLayout` + `ViewPager2` for quick pivots.
- **Branding assets** live in `design/` (SVG source) and `app/src/main/res/drawable/` (VectorDrawable) so the same iconography can be reused for launchers or marketing material.
- **Data exploration**: the `EntriesOverviewActivity` provides grouped RecyclerView sections for dates, locations, and tags plus a stats panel, while `TagManagerActivity` now renders the user-defined tag graph, lets users connect tags bidirectionally, import/export CSV vocabularies, and bulk-manage tags with multi-select delete + on-device creation.

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

## Additional Documentation

For comprehensive architecture details and development guidelines, see:

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Detailed architecture documentation with actual implementation patterns
- **[UI_PATTERNS.md](UI_PATTERNS.md)** - UI component guidelines and consistency standards
- **[CODING_GUIDELINES.md](CODING_GUIDELINES.md)** - Kotlin coding standards and best practices
- **[FEATURE_DEVELOPMENT.md](FEATURE_DEVELOPMENT.md)** - Step-by-step guide for adding new features

These documents provide the accurate, current information about the codebase structure and development standards.
