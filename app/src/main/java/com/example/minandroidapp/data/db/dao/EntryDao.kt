package com.example.minandroidapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.minandroidapp.data.db.entities.EntryEntity
import com.example.minandroidapp.data.db.entities.EntryTagCrossRef
import com.example.minandroidapp.data.db.models.EntryWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Transaction
    @Query(
        value = """
            SELECT *
            FROM entries
            ORDER BY createdAt DESC
        """,
    )
    fun observeEntries(): Flow<List<EntryWithTags>>

    @Transaction
    @Query(
        value = """
            SELECT *
            FROM entries
            WHERE id = :entryId
        """,
    )
    suspend fun getEntry(entryId: Long): EntryWithTags?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: EntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntryTags(relations: List<EntryTagCrossRef>)

    @Query(
        value = """
            DELETE FROM entry_tags
            WHERE entryId = :entryId
        """,
    )
    suspend fun deleteEntryTags(entryId: Long)

    @Query(
        value = """
            DELETE FROM entries
            WHERE id = :entryId
        """,
    )
    suspend fun deleteEntry(entryId: Long)
}
