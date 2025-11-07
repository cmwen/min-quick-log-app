# Quick Log

Quick Log is a lightweight Android 14+ companion for capturing what just happened without friction. Tap a few tags, let the app stamp the current time and location, optionally jot a short note, and save. Entries stay editable, organised, and export cleanly into Logseq-friendly text so you can remix the data for journaling, retros, or analytics.

## Key capabilities

- Tag-first capture: recent and context-aware suggestions keep the most relevant people, actions, and moods one tap away.
- Smart context: automatic timestamping, quick location lookup, and optional notes enrich each entry without slowing you down.
- Fast recall: edit any entry from the history list, re-tag it, or update the note after the fact.
- Simple exports: produce Logseq-compatible text with a single tap so your moments travel to whatever knowledge base you prefer; rich-text share targets keep formatting intact.
- Dark theme ready: Material 3 styling honours system light/dark settings out of the box.
- Personal vocab: create custom tags on the fly whenever you need a new label and relate them on the Tag Map screen.
- Sense-making views: switch to the Entries Overview to slice logs by date, location, or tag, review stats, run tag searches, and share CSV snapshots.

## Getting started

```bash
./gradlew lint testDebugUnitTest
./gradlew assembleDebug
```

- Launch the debug build on an Android 14+ device or emulator.
- Grant location access when prompted to enrich entries with a place label (falling back to coordinates if no reverse geocode is available).
- Tap an existing entry to edit tags or notes; hold onto the Export action in the top bar to share your log as Markdown-style bullets.

## Project layout

```
app/                     # Android application module (Room, ViewModel, UI)
agents/                  # Agent context, prompts, and presets
docs/                    # Extended documentation (architecture, workflows)
scripts/                 # Hooks for automation and tooling
```

See `docs/architecture.md` for module-level decisions and `docs/template-usage.md` for CI/CD and release workflows. Keep documentation and automation updated as the product evolves.
