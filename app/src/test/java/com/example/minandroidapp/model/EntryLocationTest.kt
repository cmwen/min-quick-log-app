package com.example.minandroidapp.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for EntryLocation data class
 */
class EntryLocationTest {

    @Test
    fun `create location with all properties`() {
        val location = EntryLocation(
            latitude = 37.7749,
            longitude = -122.4194,
            label = "San Francisco"
        )

        assertEquals(37.7749, location.latitude!!, 0.0001)
        assertEquals(-122.4194, location.longitude!!, 0.0001)
        assertEquals("San Francisco", location.label)
    }

    @Test
    fun `create location with nulls`() {
        val location = EntryLocation(
            latitude = null,
            longitude = null,
            label = null
        )

        assertEquals(null, location.latitude)
        assertEquals(null, location.longitude)
        assertEquals(null, location.label)
    }

    @Test
    fun `location with both coordinates`() {
        val location = EntryLocation(
            latitude = 37.7749,
            longitude = -122.4194,
            label = null
        )

        // Both coordinates are present
        assertEquals(37.7749, location.latitude!!, 0.0001)
        assertEquals(-122.4194, location.longitude!!, 0.0001)
    }

    @Test
    fun `location without coordinates`() {
        val location = EntryLocation(
            latitude = null,
            longitude = null,
            label = "Home"
        )

        // No coordinates
        assertEquals(null, location.latitude)
        assertEquals(null, location.longitude)
        assertEquals("Home", location.label)
    }

    @Test
    fun `location with only latitude`() {
        val location = EntryLocation(
            latitude = 37.7749,
            longitude = null,
            label = null
        )

        // Only one coordinate
        assertEquals(37.7749, location.latitude!!, 0.0001)
        assertEquals(null, location.longitude)
    }

    @Test
    fun `location with only longitude`() {
        val location = EntryLocation(
            latitude = null,
            longitude = -122.4194,
            label = null
        )

        // Only one coordinate
        assertEquals(null, location.latitude)
        assertEquals(-122.4194, location.longitude!!, 0.0001)
    }

    @Test
    fun `two locations with same coordinates are equal`() {
        val location1 = EntryLocation(37.7749, -122.4194, "SF")
        val location2 = EntryLocation(37.7749, -122.4194, "SF")

        assertEquals(location1, location2)
    }

    @Test
    fun `copy location with new label`() {
        val original = EntryLocation(37.7749, -122.4194, "SF")
        val modified = original.copy(label = "San Francisco")

        assertEquals(37.7749, modified.latitude!!, 0.0001)
        assertEquals(-122.4194, modified.longitude!!, 0.0001)
        assertEquals("San Francisco", modified.label)
    }
}
