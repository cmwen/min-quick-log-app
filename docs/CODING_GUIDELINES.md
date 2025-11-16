# Coding Guidelines

## Overview

This document establishes coding standards and best practices for the Quick Log Android app. Following these guidelines ensures code quality, consistency, and maintainability.

## Kotlin Coding Standards

### General Principles

1. **Follow Kotlin conventions** - Use official [Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html)
2. **Prefer immutability** - Use `val` over `var` whenever possible
3. **Null safety** - Leverage Kotlin's null safety features
4. **Functional style** - Use higher-order functions, lambda expressions
5. **Avoid platform types** - Always specify nullability explicitly

### Naming Conventions

#### Classes and Objects
```kotlin
// PascalCase for classes
class QuickLogViewModel { }
class LogEntry { }

// PascalCase for objects
object TagSeeder { }

// PascalCase for interfaces
interface EntryRepository { }
```

#### Functions and Variables
```kotlin
// camelCase for functions
fun saveEntry() { }
fun observeRecentTags(): Flow<List<LogTag>> { }

// camelCase for variables
val selectedTags: List<LogTag>
var isLoading: Boolean
```

#### Constants
```kotlin
// UPPER_SNAKE_CASE for constants
companion object {
    private const val POPULAR_TAG_LIMIT = 8
    private const val DATABASE_NAME = "quick_log_db"
    const val EXTRA_EDIT_ENTRY_ID = "extra_edit_entry_id"
}
```

#### Resources
```kotlin
// snake_case for all resources
R.id.save_button
R.string.entry_saved
R.layout.activity_main
R.drawable.ic_location
```

### Code Organization

#### File Structure
```kotlin
// 1. Package declaration
package com.example.minandroidapp.ui

// 2. Imports (organized, no wildcard imports)
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.flow.StateFlow

// 3. Class declaration
class MyActivity : AppCompatActivity() {
    
    // 4. Properties
    private lateinit var binding: ActivityMyBinding
    private val viewModel: MyViewModel by viewModels()
    
    // 5. Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) { }
    override fun onStart() { }
    
    // 6. Public methods
    fun publicMethod() { }
    
    // 7. Private methods
    private fun privateMethod() { }
    
    // 8. Companion object
    companion object {
        const val EXTRA_ID = "extra_id"
    }
}
```

#### Property Declaration Order
```kotlin
class MyClass {
    // 1. Companion object
    companion object { }
    
    // 2. Fields (properties)
    private val repository: Repository
    private lateinit var binding: ViewBinding
    
    // 3. Init blocks
    init { }
    
    // 4. Constructor
    constructor() { }
    
    // 5. Methods
    fun publicMethod() { }
    private fun privateMethod() { }
}
```

### Function Guidelines

#### Keep Functions Small
```kotlin
// Good: Single responsibility, clear purpose
fun saveEntry() {
    validateEntry()
    persistToDatabase()
    notifySuccess()
}

// Bad: Too many responsibilities
fun saveEntry() {
    if (tags.isEmpty()) return
    val sanitizedNote = note.trim()
    if (sanitizedNote.length > 1000) return
    database.withTransaction {
        // 50 lines of database code
    }
    showToast("Saved")
    updateUI()
    sendAnalytics()
}
```

#### Function Length
- **Ideal**: 10-15 lines
- **Maximum**: 30 lines
- **If longer**: Extract helper functions

#### Parameter Limits
```kotlin
// Too many parameters (bad)
fun createEntry(
    timestamp: Instant,
    note: String,
    lat: Double?,
    lon: Double?,
    label: String?,
    tag1: String,
    tag2: String,
    tag3: String
)

// Better: Use data classes
fun createEntry(
    timestamp: Instant,
    note: String,
    location: EntryLocation,
    tags: List<String>
)

// Even better: Single parameter object
fun createEntry(draft: EntryDraft)
```

### Data Classes

#### When to Use
- Representing data/state
- DTOs (Data Transfer Objects)
- Database entities
- UI state models

#### Guidelines
```kotlin
// Good: Immutable with copy
data class LogEntry(
    val id: Long,
    val createdAt: Instant,
    val note: String?,
    val tags: List<LogTag>,
    val location: EntryLocation
)

// Use copy() for updates
val updated = entry.copy(note = "New note")

// Bad: Mutable data class
data class BadEntry(
    var id: Long,  // Don't use var
    var note: String?
)
```

### Null Safety

#### Prefer Non-Null Types
```kotlin
// Good: Clear about nullability
fun processEntry(entry: LogEntry): String {
    return entry.note ?: "No note"
}

// Use safe calls and Elvis operator
val location = entry.location?.label ?: "Unknown"

// Use let for null checks
entry.note?.let { note ->
    processNote(note)
}
```

#### Avoid !! Operator
```kotlin
// Bad: Unsafe, can throw NPE
val value = nullable!!.value

// Good: Handle null case
val value = nullable?.value ?: defaultValue

// Or use requireNotNull with message
val value = requireNotNull(nullable?.value) {
    "Value must not be null at this point"
}
```

### Coroutines and Flow

#### Launch Coroutines Appropriately
```kotlin
// In ViewModel: Use viewModelScope
viewModelScope.launch {
    repository.saveEntry(entry)
}

// In Activity/Fragment: Use lifecycleScope
lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        updateUI(state)
    }
}

// Avoid GlobalScope (almost never correct)
```

#### Flow Collection
```kotlin
// Good: Lifecycle aware
lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        updateUI(state)
    }
}

// Better: Use repeatOnLifecycle for Fragments
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
            updateUI(state)
        }
    }
}
```

#### Error Handling
```kotlin
// Handle errors in coroutines
viewModelScope.launch {
    try {
        val result = repository.fetchData()
        _uiState.value = UiState.Success(result)
    } catch (e: Exception) {
        _uiState.value = UiState.Error(e.message ?: "Unknown error")
    }
}

// Or use runCatching
viewModelScope.launch {
    runCatching {
        repository.fetchData()
    }.onSuccess { result ->
        _uiState.value = UiState.Success(result)
    }.onFailure { error ->
        _uiState.value = UiState.Error(error.message ?: "Unknown error")
    }
}
```

## Architecture Guidelines

### MVVM Pattern

#### Activity/Fragment Responsibilities
- ✅ Inflate layouts
- ✅ Setup ViewBinding
- ✅ Observe ViewModels
- ✅ Handle user input (delegate to ViewModel)
- ✅ Update UI based on state
- ❌ Business logic
- ❌ Data access
- ❌ Complex calculations

#### ViewModel Responsibilities
- ✅ Hold UI state
- ✅ Handle user actions
- ✅ Coordinate with repository
- ✅ Transform data for UI
- ✅ Manage coroutines
- ❌ Reference Android framework (Context, Resources, etc.)
- ❌ Hold references to Views
- ❌ Direct database access

#### Repository Responsibilities
- ✅ Abstract data sources
- ✅ Coordinate data operations
- ✅ Map entities to domain models
- ✅ Cache data if needed
- ❌ UI logic
- ❌ ViewModel concerns

### State Management

#### UI State Pattern
```kotlin
// Define UI state as data class
data class MyUiState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedCount: Int = 0
)

// In ViewModel: Use StateFlow
private val _uiState = MutableStateFlow(MyUiState())
val uiState: StateFlow<MyUiState> = _uiState

// Update state immutably
_uiState.update { current ->
    current.copy(isLoading = true)
}

// In Activity: Collect and update UI
lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        updateUI(state)
    }
}
```

#### One-Time Events Pattern
```kotlin
// Define events as sealed class
sealed class MyEvent {
    data class ShowMessage(val message: String) : MyEvent()
    data class Navigate(val destination: String) : MyEvent()
    object SaveComplete : MyEvent()
}

// In ViewModel: Use SharedFlow
private val _events = MutableSharedFlow<MyEvent>(
    replay = 0,
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)
val events = _events.asSharedFlow()

// Emit events
_events.emit(MyEvent.ShowMessage("Entry saved"))

// In Activity: Handle events
lifecycleScope.launch {
    viewModel.events.collect { event ->
        when (event) {
            is MyEvent.ShowMessage -> showSnackbar(event.message)
            is MyEvent.Navigate -> navigate(event.destination)
            MyEvent.SaveComplete -> finish()
        }
    }
}
```

### Dependency Management

#### Current: Manual Factory (Temporary)
```kotlin
// Until Hilt is implemented
private val viewModel: MyViewModel by viewModels {
    val database = LogDatabase.getInstance(applicationContext)
    MyViewModel.Factory(MyRepository(database))
}
```

#### Future: Dependency Injection
```kotlin
// With Hilt
@AndroidEntryPoint
class MyActivity : AppCompatActivity() {
    private val viewModel: MyViewModel by viewModels()
}

@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel()
```

## Testing Guidelines

### What to Test

#### ViewModel Tests (Unit Tests)
```kotlin
@Test
fun `toggleTag adds tag when not selected`() {
    // Given
    val tag = LogTag("id", "Work")
    
    // When
    viewModel.toggleTag(tag)
    
    // Then
    val state = viewModel.uiState.value
    assertTrue(state.selectedTags.contains(tag))
}

@Test
fun `toggleTag removes tag when selected`() {
    // Given
    val tag = LogTag("id", "Work")
    viewModel.toggleTag(tag) // Select first
    
    // When
    viewModel.toggleTag(tag) // Toggle again
    
    // Then
    val state = viewModel.uiState.value
    assertFalse(state.selectedTags.contains(tag))
}
```

#### Repository Tests (Unit Tests)
```kotlin
@Test
fun `saveEntry stores data correctly`() = runTest {
    // Given
    val entry = testEntry()
    
    // When
    repository.saveEntry(entry)
    
    // Then
    val saved = repository.getEntry(entry.id)
    assertEquals(entry, saved)
}
```

#### Database Tests (Instrumentation Tests)
```kotlin
@Test
fun `insert and retrieve tag`() = runBlocking {
    val tag = TagEntity("test", "Work", TagCategory.ACTIVITY)
    tagDao.insertTag(tag)
    
    val retrieved = tagDao.getTag("test")
    assertEquals(tag, retrieved)
}
```

### Test Naming
```kotlin
// Pattern: methodName_condition_expectedResult
fun saveEntry_withValidData_savesSuccessfully()
fun saveEntry_withoutTags_throwsException()
fun toggleTag_whenNotSelected_addsToSelection()

// Or use backticks for readability
fun `save entry with valid data saves successfully`()
fun `save entry without tags throws exception`()
```

### Test Structure (Given-When-Then)
```kotlin
@Test
fun `test description`() {
    // Given: Set up test data and preconditions
    val initialState = createInitialState()
    val input = createTestInput()
    
    // When: Perform the action being tested
    val result = viewModel.performAction(input)
    
    // Then: Verify the outcome
    assertEquals(expectedValue, result)
    verify(mockRepository).wasCalledWith(expectedArgs)
}
```

## Documentation Guidelines

### KDoc Comments

#### When to Add KDoc
- Public APIs
- Complex algorithms
- Non-obvious behavior
- Important classes/functions

```kotlin
/**
 * Saves a log entry with tags and location.
 *
 * @param entryId Optional ID for updating existing entry
 * @param tags Set of tag IDs to associate with entry
 * @return The ID of the saved entry
 * @throws IllegalArgumentException if tags are empty
 */
suspend fun saveEntry(
    entryId: Long?,
    tags: Set<String>
): Long
```

### Code Comments

#### When to Add Comments
- Complex logic that isn't self-explanatory
- Workarounds for bugs
- Performance optimizations
- TODOs (with ticket reference)

```kotlin
// Good: Explains WHY
// Using WeakReference to avoid memory leak when Activity is destroyed
private var activityRef: WeakReference<Activity>

// Bad: States the obvious
// Set the text
binding.title.text = title

// Good: Explains workaround
// Workaround for Android bug #12345: Force layout refresh
binding.root.post { binding.root.requestLayout() }

// Good: TODO with context
// TODO(#123): Refactor to use Hilt once dependency injection is set up
```

#### When NOT to Comment
```kotlin
// Bad: Comment explains bad code
// Get the tags and filter them and sort them
val result = tags.filter { it.used }.sortedBy { it.name }

// Good: Self-documenting code
val activeTags = tags
    .filter { it.isUsed }
    .sortedBy { it.name }
```

## File Organization

### Package Structure

```
com.example.minandroidapp/
├── MainActivity.kt              # Main screens at root
├── data/                        # Data layer
│   ├── db/                      # Database
│   ├── mappers/                 # Entity ↔ Model
│   └── repositories/            # Data access
├── domain/                      # (Future) Business logic
│   ├── models/                  # Domain models
│   └── usecases/                # Use cases
├── location/                    # Location feature
├── model/                       # Current domain models
├── settings/                    # Settings feature
└── ui/                          # UI layer
    ├── common/                  # Shared UI components
    ├── entries/                 # Entries feature
    ├── map/                     # Map feature
    └── tag/                     # Tag feature
```

### File Naming

- Activities: `*Activity.kt`
- Fragments: `*Fragment.kt`
- ViewModels: `*ViewModel.kt`
- Adapters: `*Adapter.kt`
- Repositories: `*Repository.kt`
- DAOs: `*Dao.kt`
- Entities: `*Entity.kt`

## Resource Guidelines

### Layout Files

```xml
<!-- Use descriptive IDs -->
android:id="@+id/saveButton"      <!-- Good -->
android:id="@+id/btn"              <!-- Bad -->

<!-- Use sp for text sizes -->
android:textSize="16sp"            <!-- Good -->
android:textSize="16dp"            <!-- Bad -->

<!-- Use dp for dimensions -->
android:layout_margin="16dp"      <!-- Good -->

<!-- Extract dimensions -->
android:layout_margin="@dimen/spacing_normal"  <!-- Better -->

<!-- Use theme attributes -->
android:textColor="?attr/colorOnSurface"  <!-- Good -->
android:textColor="#000000"                <!-- Bad -->
```

### String Resources

```xml
<!-- Use descriptive names -->
<string name="entry_saved">Entry saved successfully</string>
<string name="error_no_tags">Please select at least one tag</string>

<!-- Use placeholders for dynamic content -->
<string name="entry_count">%d entries</string>
<string name="greeting">Hello, %s!</string>

<!-- Provide descriptions for translators -->
<!-- Description: Message shown after saving an entry -->
<string name="entry_saved">Entry saved successfully</string>
```

## Git Guidelines

### Commit Messages

#### Format
```
type(scope): subject

body (optional)

footer (optional)
```

#### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code style (formatting, no logic change)
- `refactor`: Code restructuring
- `test`: Adding tests
- `chore`: Maintenance tasks

#### Examples
```
feat(entries): add CSV export functionality

docs(architecture): update with current patterns

fix(map): correct marker positioning on zoom

refactor(ui): extract bottom navigation to base activity

test(viewmodel): add tests for tag selection
```

### Branch Naming
```
feature/short-description
bugfix/short-description
refactor/short-description
docs/short-description
```

## Performance Guidelines

### Avoid Common Pitfalls

```kotlin
// Bad: Creating objects in loops
for (tag in tags) {
    val item = TagItem(tag.id, tag.label) // Creates new object each iteration
    items.add(item)
}

// Good: Use map
val items = tags.map { TagItem(it.id, it.label) }
```

```kotlin
// Bad: Unnecessary findViewById in adapter
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.itemView.findViewById<TextView>(R.id.text).text = items[position]
}

// Good: Cache views in ViewHolder
class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val text: TextView = view.findViewById(R.id.text)
}
```

### Memory Leaks

```kotlin
// Bad: Activity reference in coroutine
GlobalScope.launch {
    // This keeps Activity in memory
    activity.doSomething()
}

// Good: Use appropriate scope
lifecycleScope.launch {
    // Cancelled when lifecycle ends
    doSomething()
}
```

## Code Review Checklist

Before submitting a PR:

### Functionality
- [ ] Code works as intended
- [ ] No regressions in existing features
- [ ] Edge cases handled
- [ ] Error cases handled

### Code Quality
- [ ] Follows coding guidelines
- [ ] No code smells
- [ ] Appropriate use of language features
- [ ] No unnecessary complexity

### Architecture
- [ ] Follows MVVM pattern
- [ ] Proper separation of concerns
- [ ] No business logic in UI
- [ ] No Android framework in ViewModel

### Testing
- [ ] Unit tests added/updated
- [ ] Tests pass
- [ ] Coverage is adequate

### Documentation
- [ ] KDoc for public APIs
- [ ] README updated if needed
- [ ] Architecture docs updated if needed
- [ ] Comments for complex logic

### Resources
- [ ] Strings externalized
- [ ] Strings added to all localizations
- [ ] Dimensions use dp/sp correctly
- [ ] Accessibility labels added

## Questions?

For coding questions:
1. Check this document
2. Review similar existing code
3. Check official Kotlin style guide
4. Ask in PR review
