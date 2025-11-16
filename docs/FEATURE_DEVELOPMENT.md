# Feature Development Guide

## Overview

This guide walks you through adding a new feature to the Quick Log app, following established patterns and best practices.

## Before You Start

### 1. Review Documentation
- [ ] Read [ARCHITECTURE.md](ARCHITECTURE.md) - Understand app structure
- [ ] Read [UI_PATTERNS.md](UI_PATTERNS.md) - UI consistency guidelines
- [ ] Read [CODING_GUIDELINES.md](CODING_GUIDELINES.md) - Code standards
- [ ] Review similar existing features

### 2. Plan Your Feature
- [ ] Define user story
- [ ] List required data models
- [ ] Identify UI screens needed
- [ ] Plan data flow
- [ ] Consider edge cases

### 3. Check Existing Code
- [ ] Search for similar functionality
- [ ] Identify reusable components
- [ ] Note existing patterns to follow

## Step-by-Step Feature Development

### Step 1: Define Data Models

#### Domain Models (in `model/`)
```kotlin
// model/MyFeatureData.kt
package com.example.minandroidapp.model

/**
 * Represents a feature item in the domain layer.
 */
data class MyFeatureData(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: Instant
)
```

#### Database Entities (in `data/db/entities/`)
```kotlin
// data/db/entities/MyFeatureEntity.kt
package com.example.minandroidapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "my_feature")
data class MyFeatureEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: Instant
)
```

#### Entity Mappers (in `data/mappers/`)
```kotlin
// data/mappers/MyFeatureMappers.kt
package com.example.minandroidapp.data.mappers

import com.example.minandroidapp.data.db.entities.MyFeatureEntity
import com.example.minandroidapp.model.MyFeatureData

fun MyFeatureEntity.toModel(): MyFeatureData {
    return MyFeatureData(
        id = id,
        name = name,
        description = description,
        createdAt = createdAt
    )
}

fun MyFeatureData.toEntity(): MyFeatureEntity {
    return MyFeatureEntity(
        id = id,
        name = name,
        description = description,
        createdAt = createdAt
    )
}
```

### Step 2: Update Database

#### Add DAO (in `data/db/dao/`)
```kotlin
// data/db/dao/MyFeatureDao.kt
package com.example.minandroidapp.data.db.dao

import androidx.room.*
import com.example.minandroidapp.data.db.entities.MyFeatureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MyFeatureDao {
    
    @Query("SELECT * FROM my_feature ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MyFeatureEntity>>
    
    @Query("SELECT * FROM my_feature WHERE id = :id")
    suspend fun getById(id: String): MyFeatureEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MyFeatureEntity): Long
    
    @Update
    suspend fun update(item: MyFeatureEntity)
    
    @Delete
    suspend fun delete(item: MyFeatureEntity)
    
    @Query("DELETE FROM my_feature WHERE id = :id")
    suspend fun deleteById(id: String)
}
```

#### Update Database Class
```kotlin
// data/db/LogDatabase.kt
@Database(
    entities = [
        EntryEntity::class,
        TagEntity::class,
        EntryTagCrossRef::class,
        TagLinkEntity::class,
        MyFeatureEntity::class  // Add new entity
    ],
    version = 2  // Increment version
)
abstract class LogDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun tagDao(): TagDao
    abstract fun myFeatureDao(): MyFeatureDao  // Add new DAO
    
    companion object {
        private const val DATABASE_NAME = "quick_log_db"
        
        @Volatile
        private var INSTANCE: LogDatabase? = null
        
        fun getInstance(context: Context): LogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LogDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(SeedCallback(context))
                    .addMigrations(MIGRATION_1_2)  // Add migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        // Add migration
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS my_feature (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
```

### Step 3: Add Repository Methods

#### Update or Create Repository
```kotlin
// data/MyFeatureRepository.kt (Option 1: New repository)
package com.example.minandroidapp.data

class MyFeatureRepository(private val database: LogDatabase) {
    
    private val dao = database.myFeatureDao()
    
    fun observeItems(): Flow<List<MyFeatureData>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toModel() }
        }
    }
    
    suspend fun getItem(id: String): MyFeatureData? {
        return dao.getById(id)?.toModel()
    }
    
    suspend fun saveItem(item: MyFeatureData) {
        dao.insert(item.toEntity())
    }
    
    suspend fun deleteItem(id: String) {
        dao.deleteById(id)
    }
}

// OR Add to QuickLogRepository.kt (Option 2: Extend existing)
class QuickLogRepository(private val database: LogDatabase) {
    // ... existing code ...
    
    // Add new feature methods
    private val myFeatureDao = database.myFeatureDao()
    
    fun observeMyFeatures(): Flow<List<MyFeatureData>> {
        return myFeatureDao.observeAll().map { entities ->
            entities.map { it.toModel() }
        }
    }
    
    suspend fun saveMyFeature(item: MyFeatureData) {
        myFeatureDao.insert(item.toEntity())
    }
}
```

### Step 4: Create ViewModel

#### Define UI State and Events
```kotlin
// ui/myfeature/MyFeatureUiState.kt
package com.example.minandroidapp.ui.myfeature

import com.example.minandroidapp.model.MyFeatureData

data class MyFeatureUiState(
    val items: List<MyFeatureData> = emptyList(),
    val selectedItem: MyFeatureData? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class MyFeatureEvent {
    data class ShowMessage(val message: String) : MyFeatureEvent()
    data class NavigateToDetail(val id: String) : MyFeatureEvent()
    object ItemSaved : MyFeatureEvent()
}
```

#### Create ViewModel
```kotlin
// ui/myfeature/MyFeatureViewModel.kt
package com.example.minandroidapp.ui.myfeature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minandroidapp.data.MyFeatureRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MyFeatureViewModel(
    private val repository: MyFeatureRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(MyFeatureUiState())
    val uiState: StateFlow<MyFeatureUiState> = _uiState.asStateFlow()
    
    // One-time events
    private val _events = MutableSharedFlow<MyFeatureEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()
    
    init {
        loadItems()
    }
    
    private fun loadItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.observeItems().collect { items ->
                _uiState.update { state ->
                    state.copy(
                        items = items,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }
    
    fun selectItem(item: MyFeatureData) {
        _uiState.update { it.copy(selectedItem = item) }
    }
    
    fun saveItem(item: MyFeatureData) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                repository.saveItem(item)
                _events.emit(MyFeatureEvent.ItemSaved)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                val message = e.message ?: "Failed to save item"
                _uiState.update { 
                    it.copy(isLoading = false, errorMessage = message) 
                }
                _events.emit(MyFeatureEvent.ShowMessage(message))
            }
        }
    }
    
    fun deleteItem(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteItem(id)
                _events.emit(MyFeatureEvent.ShowMessage("Item deleted"))
            } catch (e: Exception) {
                _events.emit(
                    MyFeatureEvent.ShowMessage(e.message ?: "Failed to delete")
                )
            }
        }
    }
    
    // Factory for manual ViewModel creation (until Hilt is implemented)
    class Factory(
        private val repository: MyFeatureRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MyFeatureViewModel::class.java)) {
                "Unknown ViewModel class"
            }
            return MyFeatureViewModel(repository) as T
        }
    }
}
```

### Step 5: Create Layout

#### Activity Layout
```xml
<!-- res/layout/activity_my_feature.xml -->
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Toolbar -->
    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:title="@string/my_feature_title"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Content -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:padding="16dp"
        app:layout_constraintTop_toBottomOf="@id/toolbar"
        app:layout_constraintBottom_toTopOf="@id/bottomNav"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Loading indicator -->
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="@id/recyclerView"
        app:layout_constraintBottom_toBottomOf="@id/recyclerView"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- FAB for add action -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/addFab"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:contentDescription="@string/add_item"
        app:srcCompat="@drawable/ic_add"
        app:layout_constraintBottom_toTopOf="@id/bottomNav"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Bottom Navigation -->
    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottomNav"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:background="?attr/colorSurface"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:menu="@menu/menu_bottom_nav" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

#### List Item Layout
```xml
<!-- res/layout/item_my_feature.xml -->
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardElevation="2dp"
    app:cardCornerRadius="8dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:id="@+id/titleText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textAppearance="?attr/textAppearanceTitleMedium" />

        <TextView
            android:id="@+id/descriptionText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:textAppearance="?attr/textAppearanceBodyMedium" />

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

### Step 6: Create Activity

```kotlin
// ui/myfeature/MyFeatureActivity.kt
package com.example.minandroidapp.ui.myfeature

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minandroidapp.MainActivity
import com.example.minandroidapp.R
import com.example.minandroidapp.data.MyFeatureRepository
import com.example.minandroidapp.data.db.LogDatabase
import com.example.minandroidapp.databinding.ActivityMyFeatureBinding
import com.example.minandroidapp.settings.ThemeManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MyFeatureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyFeatureBinding
    private lateinit var adapter: MyFeatureAdapter

    private val viewModel: MyFeatureViewModel by viewModels {
        val database = LogDatabase.getInstance(applicationContext)
        MyFeatureViewModel.Factory(MyFeatureRepository(database))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMyFeatureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        setupBottomNav()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = MyFeatureAdapter(
            onItemClick = { item ->
                viewModel.selectItem(item)
                // Navigate to detail or perform action
            },
            onDeleteClick = { item ->
                confirmDelete(item)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MyFeatureActivity)
            adapter = this@MyFeatureActivity.adapter
        }
    }

    private fun setupFab() {
        binding.addFab.setOnClickListener {
            showAddDialog()
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_myfeature
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_record -> {
                    startActivity(Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    finish()
                    true
                }
                R.id.nav_entries -> {
                    // Navigate to entries
                    true
                }
                R.id.nav_myfeature -> true
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        // Observe UI state
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }

        // Observe events
        lifecycleScope.launch {
            viewModel.events.collect { event ->
                handleEvent(event)
            }
        }
    }

    private fun updateUI(state: MyFeatureUiState) {
        binding.progressBar.isVisible = state.isLoading
        binding.recyclerView.isVisible = !state.isLoading
        adapter.submitList(state.items)
    }

    private fun handleEvent(event: MyFeatureEvent) {
        when (event) {
            is MyFeatureEvent.ShowMessage -> {
                showMessage(event.message)
            }
            is MyFeatureEvent.NavigateToDetail -> {
                // Navigate to detail screen
            }
            MyFeatureEvent.ItemSaved -> {
                showMessage(getString(R.string.item_saved))
            }
        }
    }

    private fun showAddDialog() {
        // Show dialog to add new item
        // See CODING_GUIDELINES.md for dialog patterns
    }

    private fun confirmDelete(item: MyFeatureData) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_item)
            .setMessage(getString(R.string.delete_confirm, item.name))
            .setPositiveButton(R.string.delete_action) { _, _ ->
                viewModel.deleteItem(item.id)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
```

### Step 7: Add String Resources

```xml
<!-- res/values/strings.xml -->
<resources>
    <!-- My Feature -->
    <string name="my_feature_title">My Feature</string>
    <string name="add_item">Add item</string>
    <string name="item_saved">Item saved successfully</string>
    <string name="delete_item">Delete item</string>
    <string name="delete_confirm">Delete %s?</string>
    <string name="delete_action">Delete</string>
</resources>

<!-- Also add to values-es/strings.xml and values-fr/strings.xml -->
```

### Step 8: Register Activity in Manifest

```xml
<!-- AndroidManifest.xml -->
<application>
    <!-- ... existing activities ... -->
    
    <activity
        android:name=".ui.myfeature.MyFeatureActivity"
        android:label="@string/my_feature_title"
        android:exported="false" />
</application>
```

### Step 9: Add Tests

#### ViewModel Test
```kotlin
// src/test/java/com/example/minandroidapp/ui/myfeature/MyFeatureViewModelTest.kt
class MyFeatureViewModelTest {

    private lateinit var repository: MyFeatureRepository
    private lateinit var viewModel: MyFeatureViewModel

    @Before
    fun setup() {
        repository = mockk()
        viewModel = MyFeatureViewModel(repository)
    }

    @Test
    fun `saveItem calls repository and emits success event`() = runTest {
        // Given
        val item = MyFeatureData("1", "Test", "Description", Instant.now())
        coEvery { repository.saveItem(any()) } returns Unit

        // When
        viewModel.saveItem(item)

        // Then
        coVerify { repository.saveItem(item) }
        // Verify event was emitted
    }
}
```

### Step 10: Update Documentation

#### Update README.md
Add feature to the key capabilities section.

#### Update docs/architecture.md
Add your new components to the architecture overview.

## Common Patterns for Different Feature Types

### List/Detail Feature
1. Create list activity with RecyclerView
2. Create detail activity/dialog
3. Support CRUD operations
4. Add search/filter if needed

### Settings/Preferences Feature
1. Use PreferenceFragmentCompat
2. Create XML preference screen
3. Create SettingsDataStore or use SharedPreferences
4. Observe preferences in relevant ViewModels

### Map/Visualization Feature
1. Use third-party library (OSM, Google Maps)
2. Create data transformation for visualization
3. Add filtering/controls
4. Support export if needed

### Export/Import Feature
1. Create file format (CSV, JSON)
2. Use Storage Access Framework
3. Handle permissions
4. Provide share intent

## Checklist Before Submitting

- [ ] Feature works as intended
- [ ] Follows MVVM architecture
- [ ] Uses ViewBinding
- [ ] Follows UI patterns
- [ ] Proper error handling
- [ ] Loading states handled
- [ ] Strings externalized
- [ ] Strings translated (all languages)
- [ ] Content descriptions added
- [ ] Tests added
- [ ] Documentation updated
- [ ] Code reviewed against guidelines
- [ ] Build passes
- [ ] Lint passes
- [ ] No new warnings

## Questions?

For feature development questions:
1. Check this document
2. Review similar existing features
3. Check ARCHITECTURE.md
4. Check CODING_GUIDELINES.md
5. Open an issue for discussion
