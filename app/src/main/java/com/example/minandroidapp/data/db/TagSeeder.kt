package com.example.minandroidapp.data.db

import com.example.minandroidapp.data.db.dao.TagDao
import com.example.minandroidapp.data.db.entities.TagEntity
import com.example.minandroidapp.data.db.entities.TagLinkEntity
import com.example.minandroidapp.model.TagCategory

class TagSeeder(private val tagDao: TagDao) {
    suspend fun seed() {
        if (tagDao.getAllTags().isNotEmpty()) {
            return
        }

        val tags = listOf(
            // Essential person tags
            TagEntity(id = "tag_me", label = "Me", category = TagCategory.PERSON),
            TagEntity(id = "tag_family", label = "Family", category = TagCategory.PERSON),
            TagEntity(id = "tag_friend", label = "Friend", category = TagCategory.PERSON),
            
            // Core activities
            TagEntity(id = "tag_work", label = "Work", category = TagCategory.ACTIVITY),
            TagEntity(id = "tag_exercise", label = "Exercise", category = TagCategory.ACTIVITY),
            TagEntity(id = "tag_meeting", label = "Meeting", category = TagCategory.ACTIVITY),
            TagEntity(id = "tag_meal", label = "Meal", category = TagCategory.ACTIVITY),
            TagEntity(id = "tag_learning", label = "Learning", category = TagCategory.ACTIVITY),
            
            // Common places
            TagEntity(id = "tag_home", label = "Home", category = TagCategory.PLACE),
            TagEntity(id = "tag_office", label = "Office", category = TagCategory.PLACE),
            TagEntity(id = "tag_outside", label = "Outside", category = TagCategory.PLACE),
            
            // Basic contexts and moods
            TagEntity(id = "tag_focus", label = "Focus", category = TagCategory.CONTEXT),
            TagEntity(id = "tag_relax", label = "Relax", category = TagCategory.CONTEXT),
            TagEntity(id = "tag_energized", label = "Energized", category = TagCategory.MOOD),
            TagEntity(id = "tag_tired", label = "Tired", category = TagCategory.MOOD),
        )

        val links = listOf(
            // Me relations
            TagLinkEntity(parentTagId = "tag_me", childTagId = "tag_work"),
            TagLinkEntity(parentTagId = "tag_me", childTagId = "tag_exercise"),
            TagLinkEntity(parentTagId = "tag_me", childTagId = "tag_focus"),
            
            // Family relations
            TagLinkEntity(parentTagId = "tag_family", childTagId = "tag_meal"),
            TagLinkEntity(parentTagId = "tag_family", childTagId = "tag_relax"),
            TagLinkEntity(parentTagId = "tag_family", childTagId = "tag_home"),
            
            // Friend relations
            TagLinkEntity(parentTagId = "tag_friend", childTagId = "tag_meal"),
            TagLinkEntity(parentTagId = "tag_friend", childTagId = "tag_meeting"),
            
            // Work relations
            TagLinkEntity(parentTagId = "tag_work", childTagId = "tag_office"),
            TagLinkEntity(parentTagId = "tag_work", childTagId = "tag_focus"),
            TagLinkEntity(parentTagId = "tag_work", childTagId = "tag_meeting"),
            
            // Exercise relations
            TagLinkEntity(parentTagId = "tag_exercise", childTagId = "tag_outside"),
            TagLinkEntity(parentTagId = "tag_exercise", childTagId = "tag_energized"),
            
            // Context-mood relations
            TagLinkEntity(parentTagId = "tag_focus", childTagId = "tag_energized"),
            TagLinkEntity(parentTagId = "tag_relax", childTagId = "tag_tired"),
        )

        tagDao.insertTags(tags)
        tagDao.insertLinks(links)
    }
}
