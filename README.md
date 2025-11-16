# Quick Log

Quick Log is a lightweight Android 14+ companion for capturing what just happened without friction. Tap a few tags, let the app stamp the current time and location, optionally jot a short note, and save. Entries stay editable, organised, and export cleanly into Logseq-friendly text so you can remix the data for journaling, retros, or analytics.

## Key capabilities

- Tag-first capture: recent, popular, and context-aware suggestions keep the most relevant people, actions, and moods one tap away.
- Smart context: automatic timestamping, quick location lookup, and optional notes enrich each entry without slowing you down.
- Fast recall: edit any entry from the history list, re-tag it, or update the note after the fact.
- Simple exports: produce Logseq-compatible text with a single tap so your moments travel to whatever knowledge base you prefer; rich-text share targets keep formatting intact.
- Dark theme ready: Material 3 styling honours system light/dark settings out of the box.
- Personal vocab: create custom tags on the fly, manage them from the Tag Map screen, import/export CSVs, or bulk-delete labels when vocabularies change.
- Sense-making views: switch to the Entries Overview to slice logs by date, location, or tag, review stats, run tag searches, and share CSV snapshots.
- Location map: visualize where you've been on an interactive OpenStreetMap, filter by date range, view chronological timelines, and export location data in LLM-friendly JSON or CSV formats for advanced analysis.
- Localised UI: ship-ready English, Spanish, and French strings keep workflows familiar for multilingual teams.

## Getting started

```bash
./gradlew lint testDebugUnitTest
./gradlew assembleDebug
```

- Launch the debug build on an Android 14+ device or emulator.
- Grant location access when prompted to enrich entries with a place label (falling back to coordinates if no reverse geocode is available).
- Tap an existing entry to edit tags or notes; hold onto the Export action in the top bar to share your log as Markdown-style bullets.
- Access the Location Map from the toolbar to see your logged locations on an interactive map, filter by date, and export data for LLM analysis.

For detailed information about the Location Map feature, see `docs/location-map-feature.md`.

## Project layout

```
app/                     # Android application module (Room, ViewModel, UI)
agents/                  # Agent context, prompts, and presets
docs/                    # Extended documentation (architecture, workflows)
scripts/                 # Hooks for automation and tooling
```

See `docs/ARCHITECTURE.md` for comprehensive architecture documentation and `REFACTORING_ANALYSIS.md` for analysis and improvement roadmap. Keep documentation and automation updated as the product evolves.
