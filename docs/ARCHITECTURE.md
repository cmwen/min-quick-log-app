# Quick Log App - Architecture Documentation

## Overview

Quick Log is a tag-first Android logging application built with modern Android architecture components. This document describes the actual implemented architecture, patterns, and structures.

## Technology Stack

### Core Technologies
- **Language**: Kotlin 1.9+ with language version 2.0
- **Minimum SDK**: Android 24 (Android 7.0)
- **Target SDK**: Android 34 (Android 14)
- **Build System**: Gradle 8.11.1 with Kotlin DSL
- **Java Version**: 17

### Key Libraries

#### Architecture & Data
- **Room**: 2.6.1 - Local database with SQLite
- **Kotlin Coroutines**: Async programming
- **Kotlin Flow**: Reactive data streams
- **StateFlow**: UI state management

#### UI & Components
- **Material 3**: Latest Material Design components
- **ViewBinding**: Type-safe view access
- **ViewPager2**: Tab-based navigation
- **RecyclerView**: List displays

#### Location & Maps
- **Google Play Services Location**: 21.3.0 - Location access
- **OSMdroid**: 6.1.18 - OpenStreetMap integration

## Application Architecture

### Current Pattern: MVVM (Model-View-ViewModel)

```
┌─────────────┐
│   Activity  │ ← UI Layer (View)
│  (Fragment) │
└──────┬──────┘
       │ observes StateFlow
       ↓
┌─────────────┐
│  ViewModel  │ ← Presentation Layer
└──────┬──────┘
       │ calls
       ↓
┌─────────────┐
│ Repository  │ ← Data Layer
└──────┬──────┘
       │ accesses
       ↓
┌─────────────┐
│Room Database│ ← Storage Layer
│  (SQLite)   │
└─────────────┘
```

### Detailed Layer Descriptions

#### 1. UI Layer (Activities & Fragments)

**Activities:**
- `MainActivity` - Main entry creation screen (448 lines)
- `EntriesOverviewActivity` - View/manage entries with tabs (242 lines)
- `TagManagerActivity` - Manage tags and relationships (428 lines)
- `LocationMapActivity` - Map visualization (457 lines)
- `SettingsActivity` - App preferences

**Current Issues:**
- Activities are too large (>400 lines)
- Duplicate bottom navigation setup in each activity
- Manual ViewModel factory creation
- No base activity for shared functionality
- Inconsistent toolbar implementations

**UI Patterns:**
- ViewBinding for all layouts
- Material 3 theming (light/dark mode)
- Bottom navigation for main screens
- Toolbar with overflow menus
- FABs for primary actions
- Dialog fragments for inputs

#### 2. Presentation Layer (ViewModels)

**Current ViewModels:**
- `QuickLogViewModel` - Manages entry creation state (364 lines)
- `EntriesOverviewViewModel` - Handles entry list and filtering (221 lines)
- `TagManagerViewModel` - Tag CRUD operations
- `LocationMapViewModel` - Map data and filtering (216 lines)

**Patterns Used:**
- StateFlow for UI state
- SharedFlow for one-time events
- Companion object factories for ViewModel creation
- Reactive data with Kotlin Flows
- Coroutines for async operations

**Current Issues:**
- Some ViewModels handle too many responsibilities
- Mixed business logic and presentation logic
- Manual factory creation instead of DI

#### 3. Data Layer

**Repository:**
- `QuickLogRepository` (307 lines) - Single repository for all data operations

**Current Structure:**
```kotlin
class QuickLogRepository(private val database: LogDatabase) {
    // Tag operations
    fun observeRecentTags(): Flow<List<LogTag>>
    fun observeAllTags(): Flow<List<LogTag>>
    suspend fun createCustomTag(): LogTag
    
    // Entry operations
    fun observeEntries(): Flow<List<LogEntry>>
    suspend fun saveEntry()
    suspend fun deleteEntry()
    
    // Tag relations
    fun observeTagRelations(): Flow<List<TagRelations>>
    suspend fun updateTagRelations()
    
    // Export operations
    suspend fun exportTagsCsv(): String
    suspend fun exportEntriesCsv(): String
}
```

**Issues:**
- Single repository does too much
- Should split into feature-specific repositories
- Business logic mixed with data access

#### 4. Database Layer (Room)

**Database:**
- `LogDatabase` - Single database instance with singleton pattern

**Entities:**
- `EntryEntity` - Log entries with timestamp, note, location
- `TagEntity` - Tag definitions with category
- `EntryTagCrossRef` - Many-to-many relationship
- `TagLinkEntity` - Tag-to-tag relationships (suggestions)

**DAOs:**
- `EntryDao` - Entry CRUD operations (72 lines)
- `TagDao` - Tag CRUD and relationship queries (88 lines)

**Supporting:**
- `Converters` - Type converters for Room
- `TagSeeder` - Initial tag data (71 lines)
- `EntityMappers` - Convert entities to domain models

## Package Structure

```
com.example.minandroidapp/
├── MainActivity.kt                 # Main entry screen
├── data/
│   ├── QuickLogRepository.kt      # Data access layer
│   ├── db/
│   │   ├── LogDatabase.kt         # Room database
│   │   ├── Converters.kt          # Type converters
│   │   ├── TagSeeder.kt           # Initial data
│   │   ├── dao/
│   │   │   ├── EntryDao.kt        # Entry queries
│   │   │   └── TagDao.kt          # Tag queries
│   │   └── entities/
│   │       ├── EntryEntity.kt
│   │       ├── TagEntity.kt
│   │       ├── EntryTagCrossRef.kt
│   │       └── TagLinkEntity.kt
│   └── mappers/
│       └── EntityMappers.kt       # Entity → Model
├── location/
│   └── LocationProvider.kt        # Location access
├── model/
│   ├── LogEntry.kt                # Domain model
│   ├── LogTag.kt
│   ├── EntryLocation.kt
│   ├── EntryDraft.kt
│   ├── QuickLogUiState.kt
│   ├── QuickLogEvent.kt
│   └── TagRelations.kt
├── settings/
│   ├── SettingsActivity.kt
│   └── ThemeManager.kt
└── ui/
    ├── QuickLogViewModel.kt       # Main ViewModel
    ├── common/
    │   └── SwipeToDeleteCallback.kt
    ├── entries/
    │   ├── EntriesOverviewActivity.kt
    │   ├── EntriesOverviewViewModel.kt
    │   ├── SimpleEntriesFragment.kt
    │   ├── TagsFragment.kt
    │   ├── StatsFragment.kt
    │   └── [adapters and models]
    ├── map/
    │   ├── LocationMapActivity.kt
    │   ├── LocationMapViewModel.kt
    │   └── [models]
    └── tag/
        ├── TagManagerActivity.kt
        ├── TagManagerViewModel.kt
        └── TagRelationsAdapter.kt
```

## Data Flow Patterns

### 1. Entry Creation Flow

```
User Input → Activity → ViewModel → Repository → Database
     ↓                      ↓
UI Updates  ← StateFlow ← ViewModel
```

Example:
```kotlin
// 1. User taps tag
binding.chip.setOnClickListener {
    viewModel.toggleTag(tag)  // Call ViewModel
}

// 2. ViewModel updates state
fun toggleTag(tag: LogTag) {
    draftFlow.update { draft ->
        draft.copy(selectedTags = updated)  // Update StateFlow
    }
}

// 3. Activity observes state
lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        renderSelectedChips(state.draft.selectedTags)  // Update UI
    }
}

// 4. User saves
viewModel.saveEntry()  // Calls repository

// 5. Repository saves to database
suspend fun saveEntry(...) {
    database.withTransaction {
        entryDao.insertEntry(entry)
        entryDao.insertEntryTags(relations)
    }
}
```

### 2. List Display Flow

```
Database → Flow → Repository → ViewModel → StateFlow → Activity
                                              ↓
                                        RecyclerView Adapter
```

### 3. Event Handling

One-time events (toasts, navigation) use SharedFlow:

```kotlin
// ViewModel emits event
eventsFlow.emit(QuickLogEvent.EntrySaved)

// Activity collects
lifecycleScope.launch {
    viewModel.events.collect { event ->
        when (event) {
            is QuickLogEvent.EntrySaved -> showMessage()
        }
    }
}
```

## Navigation Pattern

### Current: Manual Intent-Based

```kotlin
// In each activity:
private fun openTagManager() {
    startActivity(Intent(this, TagManagerActivity::class.java))
}
```

**Issues:**
- No type safety
- Hard to test
- Deep linking requires manual handling
- Navigation logic scattered

**Improvement Needed:**
- Jetpack Navigation Component
- Single activity architecture consideration
- Navigation graph for deep links

## Dependency Management

### Current: Manual ViewModel Factory

```kotlin
private val viewModel: QuickLogViewModel by viewModels {
    val database = LogDatabase.getInstance(applicationContext)
    QuickLogViewModel.Factory(QuickLogRepository(database))
}
```

**Issues:**
- Manual dependency creation
- Hard to test
- Tight coupling
- No shared instances

**Recommendation:**
- Implement Hilt or Koin
- @AndroidEntryPoint activities
- @Inject ViewModels
- Proper scoping

## Testing Strategy

### Current State
- Basic unit test structure exists
- Limited test coverage
- No integration tests documented
- UI tests not present

### Recommended Structure
```
app/
├── src/
│   ├── main/           # Production code
│   ├── test/           # Unit tests
│   │   ├── java/
│   │   │   ├── viewmodel/
│   │   │   ├── repository/
│   │   │   └── mappers/
│   └── androidTest/    # Integration tests
│       └── java/
│           ├── database/
│           ├── ui/
│           └── e2e/
```

## Build Configuration

### Gradle Modules
- Single app module (no feature modules yet)
- Version catalog in `gradle/libs.versions.toml`
- Kotlin DSL for build scripts

### Build Variants
- `debug` - Development builds
- `release` - Signed production builds

### Key Build Features
- ViewBinding enabled
- Room annotation processing (KAPT)
- Compose not used (XML layouts)
- ProGuard rules defined

## Resource Organization

### Layouts
- Activity layouts: `activity_*.xml`
- Fragment layouts: `fragment_*.xml`
- Item layouts: `item_*.xml`
- Custom view layouts: `view_*.xml`
- Dialog layouts: `dialog_*.xml`

### Menus
- `menu_bottom_nav.xml` - Shared bottom navigation
- `menu_toolbar_actions.xml` - Main screen actions
- `menu_entries_overview.xml` - Entries screen actions
- `menu_tag_manager.xml` - Tag screen actions
- `menu_location_map.xml` - Map screen actions

### Localization
- `values/strings.xml` - English (default)
- `values-es/strings.xml` - Spanish
- `values-fr/strings.xml` - French

## Known Architectural Issues

### 1. No Dependency Injection
**Impact**: High
- Makes testing difficult
- Tight coupling between components
- Boilerplate factory code

**Solution**: Implement Hilt

### 2. Large Activity Files
**Impact**: Medium
- Hard to understand and maintain
- Mixed responsibilities
- Difficult for LLMs to modify

**Solution**: 
- Extract UI components
- Create base activities
- Move logic to ViewModels/Use Cases

### 3. Single Repository
**Impact**: Medium
- God object anti-pattern
- Hard to test specific features
- Unclear boundaries

**Solution**: Split into feature repositories

### 4. No Use Case Layer
**Impact**: Low-Medium
- Business logic in ViewModels
- Not reusable across features
- Harder to test

**Solution**: Create use case classes for complex operations

### 5. Manual Navigation
**Impact**: Medium
- No type safety
- Hard to test
- Duplicate code

**Solution**: Jetpack Navigation Component

### 6. Inconsistent UI Patterns
**Impact**: High (for LLM confusion)
- Different layout approaches
- Duplicate navigation code
- No base classes

**Solution**: See UI_PATTERNS.md

## Migration Path

### Phase 1: Documentation & Standards
1. ✅ Document actual architecture
2. Create UI component guidelines
3. Create coding standards
4. Update all documentation

### Phase 2: UI Consistency
1. Create base activities
2. Extract bottom navigation component
3. Standardize toolbar usage
4. Create style guide

### Phase 3: Dependency Injection
1. Add Hilt dependency
2. Migrate ViewModels
3. Provide database singleton
4. Inject repositories

### Phase 4: Feature Modules
1. Split repository by feature
2. Create use case layer
3. Implement proper navigation
4. Add comprehensive tests

## Best Practices for Contributors

### When Adding New Features

1. **Follow Existing Patterns**
   - Study similar features first
   - Match naming conventions
   - Use same architectural layers

2. **Keep Activities Thin**
   - Max 400 lines
   - Only UI setup and observation
   - Delegate to ViewModel

3. **Use StateFlow for UI State**
   - Single source of truth
   - Immutable data classes
   - Clear state updates

4. **Use SharedFlow for Events**
   - One-time actions
   - Navigation events
   - Toast messages

5. **Update Documentation**
   - Update this file if architecture changes
   - Add feature to README
   - Update UI_PATTERNS if new patterns introduced

### Code Review Checklist

- [ ] Follows MVVM pattern
- [ ] ViewModel doesn't reference Android framework
- [ ] Uses ViewBinding (not findViewById)
- [ ] Observes data with Flows
- [ ] Handles lifecycle correctly
- [ ] Updates documentation
- [ ] Adds appropriate tests
- [ ] Follows Material 3 guidelines

## Resources

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Material 3 Design](https://m3.material.io/)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Kotlin Flows](https://kotlinlang.org/docs/flow.html)
- [ViewModel Guide](https://developer.android.com/topic/libraries/architecture/viewmodel)

## Questions?

For architecture questions or clarifications:
1. Check this document first
2. Review similar existing code
3. Check CODING_GUIDELINES.md
4. Open an issue for discussion
