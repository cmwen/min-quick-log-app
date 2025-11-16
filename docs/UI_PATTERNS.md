# UI Patterns & Component Guidelines

## Overview

This document describes the UI patterns, components, and conventions used across the Quick Log app. Following these patterns ensures consistency and makes it easier for developers (including LLMs) to add new features.

## Layout Patterns

### Activity Layout Structure

**Standard Pattern (Recommended):**
```xml
<androidx.constraintlayout.widget.ConstraintLayout>
    
    <!-- Toolbar -->
    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        app:layout_constraintTop_toTopOf="parent" />
    
    <!-- Main Content -->
    <ScrollView/RecyclerView/Content
        app:layout_constraintTop_toBottomOf="@id/toolbar"
        app:layout_constraintBottom_toTopOf="@id/bottomNav" />
    
    <!-- Bottom Navigation -->
    <BottomNavigationView
        android:id="@+id/bottomNav"
        app:layout_constraintBottom_toBottomOf="parent" />
        
</androidx.constraintlayout.widget.ConstraintLayout>
```

### Current Inconsistencies

| Screen | Root Layout | Toolbar Wrapper | Issue |
|--------|-------------|-----------------|-------|
| MainActivity | ConstraintLayout | Direct | ✅ Standard |
| EntriesOverview | ConstraintLayout | AppBarLayout | ❌ Inconsistent |
| TagManager | ConstraintLayout | Direct | ✅ Standard |
| LocationMap | CoordinatorLayout | AppBarLayout | ❌ Different root |

**Fix Required:**
- Standardize on ConstraintLayout for all activities
- Remove AppBarLayout unless collapsing toolbar is needed
- Use MaterialToolbar directly

## Component Patterns

### 1. Bottom Navigation

**Current Issue:** Duplicated in every activity

**Standard Pattern:**
```kotlin
// In Activity onCreate()
private fun setupBottomNav() {
    binding.bottomNav.selectedItemId = R.id.nav_current_screen
    binding.bottomNav.setOnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_record -> {
                navigateToMain()
                true
            }
            R.id.nav_entries -> {
                navigateToEntries()
                true
            }
            R.id.nav_tags -> {
                navigateToTags()
                true
            }
            R.id.nav_locations -> {
                navigateToMap()
                true
            }
            else -> false
        }
    }
}

private fun navigateToMain() {
    if (this !is MainActivity) {
        startActivity(Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
    }
}
```

**Recommended Improvement:**
Create a base activity:
```kotlin
abstract class BaseNavigationActivity : AppCompatActivity() {
    abstract val currentNavItem: Int
    
    protected fun setupBottomNav(binding: ViewBinding) {
        // Shared implementation
    }
}
```

### 2. Toolbar Setup

**Standard Pattern:**
```kotlin
// In Activity onCreate()
setSupportActionBar(binding.toolbar)
binding.toolbar.title = getString(R.string.screen_title)
binding.toolbar.setNavigationOnClickListener {
    finish()
}

// For options menu
override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_name, menu)
    return true
}

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.action_something -> {
            doSomething()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
```

### 3. ViewBinding Usage

**Standard Pattern:**
```kotlin
class MyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Use binding.viewId
    }
}
```

**Never use:**
- `findViewById()` - Use ViewBinding instead
- Kotlin synthetic imports - Deprecated

### 4. ViewModel Integration

**Current Pattern (to be improved):**
```kotlin
private val viewModel: MyViewModel by viewModels {
    val database = LogDatabase.getInstance(applicationContext)
    MyViewModel.Factory(MyRepository(database))
}
```

**Future Pattern (with Hilt):**
```kotlin
@AndroidEntryPoint
class MyActivity : AppCompatActivity() {
    private val viewModel: MyViewModel by viewModels()
}
```

### 5. State Observation

**Standard Pattern:**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ... setup ...
    
    observeViewModel()
}

private fun observeViewModel() {
    // Observe UI state
    lifecycleScope.launch {
        viewModel.uiState.collect { state ->
            updateUI(state)
        }
    }
    
    // Observe one-time events
    lifecycleScope.launch {
        viewModel.events.collect { event ->
            handleEvent(event)
        }
    }
}

private fun updateUI(state: MyUiState) {
    // Update all UI based on state
}

private fun handleEvent(event: MyEvent) {
    when (event) {
        is MyEvent.ShowMessage -> showSnackbar(event.message)
        is MyEvent.NavigateAway -> navigateTo(event.destination)
    }
}
```

### 6. Dialog Creation

**Standard Pattern:**
```kotlin
private fun showInputDialog() {
    val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
    val input = dialogView.findViewById<TextInputEditText>(R.id.input)
    
    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.dialog_title)
        .setView(dialogView)
        .setPositiveButton(R.string.confirm) { _, _ ->
            val value = input?.text?.toString().orEmpty()
            viewModel.handleInput(value)
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
        
    input?.post { input.requestFocus() }
}
```

### 7. Snackbar Messages

**Standard Pattern:**
```kotlin
private fun showMessage(message: String) {
    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
}

private fun showMessage(@StringRes messageRes: Int) {
    showMessage(getString(messageRes))
}
```

### 8. RecyclerView Setup

**Standard Pattern:**
```kotlin
private fun setupRecyclerView() {
    val adapter = MyAdapter(
        onClick = { item -> viewModel.handleClick(item) },
        onLongClick = { item -> viewModel.handleLongClick(item) }
    )
    
    binding.recyclerView.apply {
        layoutManager = LinearLayoutManager(context)
        this.adapter = adapter
        // Optional: dividers, item decoration
    }
    
    // Observe data
    lifecycleScope.launch {
        viewModel.items.collect { items ->
            adapter.submitList(items)
        }
    }
}
```

### 9. FAB Usage

**Standard Pattern:**
```kotlin
// In layout
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/fab"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:contentDescription="@string/fab_description"
    app:srcCompat="@drawable/ic_add"
    app:layout_constraint... />

// In Activity
binding.fab.setOnClickListener {
    viewModel.handleFabClick()
}
```

**Guidelines:**
- Always provide contentDescription
- Use Material icons
- Position consistently (bottom-right, 16dp margin)

### 10. Loading States

**Standard Pattern:**
```kotlin
private fun updateUI(state: MyUiState) {
    binding.progressBar.isVisible = state.isLoading
    binding.content.isVisible = !state.isLoading && !state.hasError
    binding.errorView.isVisible = state.hasError
    
    if (state.hasError) {
        binding.errorText.text = state.errorMessage
    }
}
```

## Material 3 Components

### Buttons

**Primary Action:**
```xml
<com.google.android.material.button.MaterialButton
    style="@style/Widget.Material3.Button"
    android:text="@string/action" />
```

**Secondary Action:**
```xml
<com.google.android.material.button.MaterialButton
    style="@style/Widget.Material3.Button.OutlinedButton"
    android:text="@string/action" />
```

**Tertiary Action:**
```xml
<com.google.android.material.button.MaterialButton
    style="@style/Widget.Material3.Button.TextButton"
    android:text="@string/action" />
```

### Text Input

**Standard:**
```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@string/hint">

    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/input"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
        
</com.google.android.material.textfield.TextInputLayout>
```

### Chips

**Filter Chips (Checkable):**
```kotlin
val chip = inflater.inflate(R.layout.view_tag_chip, group, false) as Chip
chip.text = tag.label
chip.isCheckable = true
chip.isChecked = isSelected
chip.setOnClickListener { viewModel.toggleSelection(tag) }
group.addView(chip)
```

**Action Chips (with close icon):**
```kotlin
chip.isCloseIconVisible = true
chip.setOnCloseIconClickListener { viewModel.remove(tag) }
```

### Cards

**Standard:**
```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="12dp"
    app:cardElevation="4dp"
    app:cardCornerRadius="8dp">
    
    <!-- Card content -->
    
</com.google.android.material.card.MaterialCardView>
```

## Spacing & Dimensions

### Standard Spacing
```xml
<!-- Margins -->
<dimen name="spacing_tiny">4dp</dimen>
<dimen name="spacing_small">8dp</dimen>
<dimen name="spacing_medium">12dp</dimen>
<dimen name="spacing_normal">16dp</dimen>
<dimen name="spacing_large">24dp</dimen>
<dimen name="spacing_xlarge">32dp</dimen>

<!-- Common usages -->
Screen edges: 16dp
Between sections: 24dp
Between elements: 8dp
Card margin: 12dp
Card padding: 16dp
```

### Touch Targets
- Minimum: 48x48dp
- FAB: 56x56dp (regular) or 40x40dp (mini)
- Icons: 24x24dp

## Color Usage

### When to Use Which Color

**Primary Color:**
- Selected state
- Active elements
- Primary buttons
- Important icons

**On Surface:**
- Body text
- Icons
- Most UI elements

**Surface Variant:**
- Dividers
- Disabled states
- Subtle backgrounds

**Error:**
- Error messages
- Destructive actions
- Validation failures

## String Resources

### Naming Conventions

```xml
<!-- Screen titles -->
<string name="screen_name_title">Title</string>

<!-- Actions -->
<string name="action_verb">Verb</string>
<string name="action_verb_noun">Verb Noun</string>

<!-- Messages -->
<string name="message_context">Message text</string>
<string name="error_context">Error message</string>

<!-- Labels -->
<string name="label_field">Field name</string>

<!-- Content descriptions -->
<string name="cd_description">Description for accessibility</string>
```

### Placeholders

```xml
<!-- Use placeholders for dynamic content -->
<string name="entry_count">%d entries</string>
<string name="greeting">Hello, %s!</string>

<!-- In code -->
getString(R.string.entry_count, count)
```

## Accessibility

### Content Descriptions

**Required for:**
- All ImageButtons
- All ImageViews that aren't decorative
- All FABs
- All icon-only buttons

```xml
android:contentDescription="@string/cd_add_entry"
```

### Text Contrast

- Ensure WCAG AA compliance (4.5:1 for normal text)
- Use Material color system
- Test with both light and dark themes

### Touch Targets

- Minimum 48x48dp
- More for critical actions
- Adequate spacing between tappable elements

## Common Anti-Patterns to Avoid

### ❌ findViewById
```kotlin
// Don't do this
val button = findViewById<Button>(R.id.button)
```

```kotlin
// Do this instead
binding.button.setOnClickListener { }
```

### ❌ Direct Database Access in Activity
```kotlin
// Don't do this
lifecycleScope.launch {
    val entries = database.entryDao().getAll()
}
```

```kotlin
// Do this instead
lifecycleScope.launch {
    viewModel.entries.collect { entries ->
        // Update UI
    }
}
```

### ❌ Business Logic in Activity
```kotlin
// Don't do this
fun saveEntry() {
    if (selectedTags.isEmpty()) {
        showError()
        return
    }
    // ... complex logic ...
}
```

```kotlin
// Do this instead
fun saveEntry() {
    viewModel.saveEntry()
}
```

### ❌ Android Framework in ViewModel
```kotlin
// Don't do this in ViewModel
fun showMessage(message: String) {
    Toast.makeText(context, message, LENGTH_LONG).show()
}
```

```kotlin
// Do this instead in ViewModel
_events.emit(ShowMessageEvent(message))

// Handle in Activity
viewModel.events.collect { event ->
    when (event) {
        is ShowMessageEvent -> showSnackbar(event.message)
    }
}
```

## Testing Considerations

### Testable UI Code

**Good:**
```kotlin
// All state in ViewModel, easy to test
fun updateUI(state: UiState) {
    binding.title.text = state.title
    binding.subtitle.text = state.subtitle
    binding.button.isEnabled = state.isValid
}
```

**Bad:**
```kotlin
// Logic in UI, hard to test
fun updateUI() {
    if (selectedItems.size > 0 && hasPermission()) {
        binding.button.isEnabled = true
    }
}
```

## Migration Checklist

When updating an existing activity:

- [ ] Switch to ConstraintLayout if needed
- [ ] Add ViewBinding
- [ ] Extract duplicate bottom nav code
- [ ] Move business logic to ViewModel
- [ ] Use StateFlow for UI state
- [ ] Use SharedFlow for events
- [ ] Add content descriptions
- [ ] Follow spacing guidelines
- [ ] Use Material 3 components
- [ ] Add documentation

## Questions?

For UI pattern questions:
1. Check this document
2. Look at MainActivity as reference
3. Review Material 3 guidelines
4. Ask in PR review
