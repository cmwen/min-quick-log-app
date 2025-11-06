# UI Review Prompt Snippets

Use these fragments when guiding an agent to review or implement UI changes in the Android app.

## Layout Quality

- Verify views comply with Material 3 design guidance and use `ConstraintLayout` or `Compose` idioms appropriately.
- Check that resources reference `@dimen` and `@color` entries instead of hard-coded values.
- Confirm UI state survives configuration changes and follows lifecycle-aware patterns.

## Accessibility

- Ensure every interactive element has a content description or label.
- Verify touch targets meet the 48dp minimum.
- Confirm color combinations meet WCAG AA contrast requirements.

## Performance

- Avoid long-running work on the main thread; prefer coroutines or WorkManager.
- Use `RecyclerView` diffing or `LazyColumn` keys to prevent unnecessary recompositions.
- Limit bitmap sizes and leverage image loading libraries for caching.
