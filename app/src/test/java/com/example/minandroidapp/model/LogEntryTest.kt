package com.example.minandroidapp.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for LogEntry data class
 */
class LogEntryTest {

    @Test
    fun `create LogEntry with all properties`() {
        val now = Instant.now()
        val tags = listOf(
            LogTag("tag1", "Work", TagCategory.ACTIVITY, now),
            LogTag("tag2", "Meeting", TagCategory.CONTEXT, now)
        )
        val location = EntryLocation(37.7749, -122.4194, "San Francisco")

        val entry = LogEntry(
            id = 1L,
            createdAt = now,
            note = "Test note",
            location = location,
            tags = tags
        )

        assertEquals(1L, entry.id)
        assertEquals(now, entry.createdAt)
        assertEquals("Test note", entry.note)
        assertEquals(location, entry.location)
        assertEquals(2, entry.tags.size)
    }

    @Test
    fun `create LogEntry with null note`() {
        val now = Instant.now()
        val entry = LogEntry(
            id = 1L,
            createdAt = now,
            note = null,
            location = EntryLocation(null, null, null),
            tags = emptyList()
        )

        assertEquals(1L, entry.id)
        assertEquals(now, entry.createdAt)
        assertEquals(null, entry.note)
        assertEquals(0, entry.tags.size)
    }

    @Test
    fun `create LogEntry with empty tags`() {
        val entry = LogEntry(
            id = 1L,
            createdAt = Instant.now(),
            note = "Note",
            location = EntryLocation(null, null, null),
            tags = emptyList()
        )

        assertEquals(0, entry.tags.size)
    }

    @Test
    fun `copy entry with modified note`() {
        val original = LogEntry(
            id = 1L,
            createdAt = Instant.now(),
            note = "Original",
            location = EntryLocation(null, null, null),
            tags = emptyList()
        )

        val modified = original.copy(note = "Modified")

        assertEquals(1L, modified.id)
        assertEquals("Modified", modified.note)
    }

    @Test
    fun `two entries with same values are equal`() {
        val now = Instant.now()
        val tags = listOf(LogTag("tag1", "Work", TagCategory.ACTIVITY, now))
        val location = EntryLocation(null, null, null)

        val entry1 = LogEntry(1L, now, "Note", location, tags)
        val entry2 = LogEntry(1L, now, "Note", location, tags)

        assertEquals(entry1, entry2)
    }

    @Test
    fun `two entries with different ids are not equal`() {
        val now = Instant.now()
        val location = EntryLocation(null, null, null)

        val entry1 = LogEntry(1L, now, "Note", location, emptyList())
        val entry2 = LogEntry(2L, now, "Note", location, emptyList())

        assertNotEquals(entry1, entry2)
    }
}
