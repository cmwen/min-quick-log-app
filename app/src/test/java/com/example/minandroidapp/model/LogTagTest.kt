package com.example.minandroidapp.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for LogTag data class
 */
class LogTagTest {

    @Test
    fun `create LogTag with all properties`() {
        val now = Instant.now()
        val tag = LogTag(
            id = "tag1",
            label = "Work",
            category = TagCategory.ACTIVITY,
            lastUsedAt = now
        )

        assertEquals("tag1", tag.id)
        assertEquals("Work", tag.label)
        assertEquals(TagCategory.ACTIVITY, tag.category)
        assertEquals(now, tag.lastUsedAt)
    }

    @Test
    fun `create LogTag with null lastUsedAt`() {
        val tag = LogTag(
            id = "tag2",
            label = "Personal",
            category = TagCategory.CONTEXT,
            lastUsedAt = null
        )

        assertEquals("tag2", tag.id)
        assertEquals("Personal", tag.label)
        assertEquals(TagCategory.CONTEXT, tag.category)
        assertEquals(null, tag.lastUsedAt)
    }

    @Test
    fun `two tags with same values are equal`() {
        val now = Instant.now()
        val tag1 = LogTag("id1", "Work", TagCategory.ACTIVITY, now)
        val tag2 = LogTag("id1", "Work", TagCategory.ACTIVITY, now)

        assertEquals(tag1, tag2)
        assertEquals(tag1.hashCode(), tag2.hashCode())
    }

    @Test
    fun `two tags with different values are not equal`() {
        val now = Instant.now()
        val tag1 = LogTag("id1", "Work", TagCategory.ACTIVITY, now)
        val tag2 = LogTag("id2", "Work", TagCategory.ACTIVITY, now)

        assertNotEquals(tag1, tag2)
    }

    @Test
    fun `copy tag with modified properties`() {
        val original = LogTag("id1", "Work", TagCategory.ACTIVITY, null)
        val now = Instant.now()
        val modified = original.copy(lastUsedAt = now)

        assertEquals("id1", modified.id)
        assertEquals("Work", modified.label)
        assertEquals(TagCategory.ACTIVITY, modified.category)
        assertEquals(now, modified.lastUsedAt)
    }
}
