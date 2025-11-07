package com.example.minandroidapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.minandroidapp.data.db.entities.TagEntity
import com.example.minandroidapp.data.db.entities.TagLinkEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query(
        value = """
            SELECT *
            FROM tags
            ORDER BY COALESCE(lastUsedAt, 0) DESC, label ASC
            LIMIT :limit
        """,
    )
    fun observeRecentTags(limit: Int): Flow<List<TagEntity>>

    @Query(
        value = """
            SELECT *
            FROM tags
            WHERE id IN (:ids)
        """,
    )
    suspend fun getTags(ids: List<String>): List<TagEntity>

    @Query(
        value = """
            SELECT *
            FROM tags
            ORDER BY label ASC
        """,
    )
    suspend fun getAllTags(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<TagLinkEntity>)

    @Query(
        value = """
            SELECT t.*
            FROM tags t
            INNER JOIN tag_links l ON t.id = l.childTagId
            WHERE l.parentTagId IN (:parentIds)
            GROUP BY t.id
            ORDER BY COALESCE(t.lastUsedAt, 0) DESC, t.label ASC
        """,
    )
    suspend fun getSuggestions(parentIds: List<String>): List<TagEntity>

    @Query(
        value = """
            UPDATE tags
            SET lastUsedAt = :timestamp
            WHERE id IN (:tagIds)
        """,
    )
    suspend fun touchTags(tagIds: List<String>, timestamp: Instant)
}
