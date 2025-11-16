package com.example.minandroidapp.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for EntryDraft data class
 */
class EntryDraftTest {

    @Test
    fun `create empty draft`() {
        val draft = EntryDraft()

        assertEquals(null, draft.entryId)
        assertEquals("", draft.note)
        assertTrue(draft.selectedTags.isEmpty())
        assertEquals(null, draft.location.latitude)
        assertEquals(null, draft.location.longitude)
    }

    @Test
    fun `create draft with note`() {
        val draft = EntryDraft(note = "Test note")

        assertEquals("Test note", draft.note)
    }

    @Test
    fun `create draft with selected tags`() {
        val tag1 = LogTag("tag1", "Work", TagCategory.ACTIVITY, Instant.now())
        val tag2 = LogTag("tag2", "Meeting", TagCategory.CONTEXT, Instant.now())
        val draft = EntryDraft(selectedTags = listOf(tag1, tag2))

        assertEquals(2, draft.selectedTags.size)
        assertTrue(draft.selectedTags.contains(tag1))
        assertTrue(draft.selectedTags.contains(tag2))
    }

    @Test
    fun `create draft with location`() {
        val location = EntryLocation(37.7749, -122.4194, "San Francisco")
        val draft = EntryDraft(location = location)

        assertEquals(37.7749, draft.location.latitude!!, 0.0001)
        assertEquals(-122.4194, draft.location.longitude!!, 0.0001)
        assertEquals("San Francisco", draft.location.label)
    }

    @Test
    fun `create draft for editing existing entry`() {
        val draft = EntryDraft(entryId = 123L)

        assertEquals(123L, draft.entryId)
    }

    @Test
    fun `copy draft with new note`() {
        val original = EntryDraft(note = "Original")
        val modified = original.copy(note = "Modified")

        assertEquals("Original", original.note)
        assertEquals("Modified", modified.note)
    }

    @Test
    fun `copy draft with additional tag`() {
        val tag1 = LogTag("tag1", "Work", TagCategory.ACTIVITY, null)
        val tag2 = LogTag("tag2", "Meeting", TagCategory.CONTEXT, null)
        val original = EntryDraft(selectedTags = listOf(tag1))
        val modified = original.copy(selectedTags = listOf(tag1, tag2))

        assertEquals(1, original.selectedTags.size)
        assertEquals(2, modified.selectedTags.size)
    }

    @Test
    fun `validate draft has required data`() {
        val tag = LogTag("tag1", "Work", TagCategory.ACTIVITY, null)
        val draft = EntryDraft(
            note = "Note",
            selectedTags = listOf(tag)
        )

        // A valid draft should have at least one tag
        assertTrue(draft.selectedTags.isNotEmpty())
    }
}
