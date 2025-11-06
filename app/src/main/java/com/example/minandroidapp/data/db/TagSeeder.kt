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
            TagEntity(id = "tag_me", label = "I", category = TagCategory.PERSON),
            TagEntity(id = "tag_partner", label = "Partner", category = TagCategory.PERSON),
            TagEntity(id = "tag_kids", label = "Kids", category = TagCategory.PERSON),
            TagEntity(id = "tag_friend", label = "Friend", category = TagCategory.PERSON),
            TagEntity(id = "tag_wfh", label = "WFH", category = TagCategory.ACTIVITY),
            TagEntity(id = "tag_jog", label = "Jog", category = TagCategory.ACTIVITY),
            TagEntity(id = "tag_meeting", label = "Meeting", category = TagCategory.ACTIVITY),
            TagEntity(id = "tag_reading", label = "Reading", category = TagCategory.ACTIVITY),
            TagEntity(id = "tag_coffee", label = "Coffee", category = TagCategory.ACTIVITY),
            TagEntity(id = "tag_lunch", label = "Lunch", category = TagCategory.ACTIVITY),
            TagEntity(id = "tag_dinner", label = "Dinner", category = TagCategory.ACTIVITY),
            TagEntity(id = "tag_walk", label = "Walk", category = TagCategory.ACTIVITY),
            TagEntity(id = "tag_school_run", label = "School Run", category = TagCategory.ACTIVITY),
            TagEntity(id = "tag_home", label = "Home", category = TagCategory.PLACE),
            TagEntity(id = "tag_office", label = "Office", category = TagCategory.PLACE),
            TagEntity(id = "tag_outside", label = "Outside", category = TagCategory.PLACE),
            TagEntity(id = "tag_cafe", label = "Cafe", category = TagCategory.PLACE),
            TagEntity(id = "tag_focus", label = "Deep Focus", category = TagCategory.CONTEXT),
            TagEntity(id = "tag_relax", label = "Relax", category = TagCategory.CONTEXT),
            TagEntity(id = "tag_energized", label = "Energized", category = TagCategory.MOOD),
            TagEntity(id = "tag_tired", label = "Tired", category = TagCategory.MOOD),
        )

        val links = listOf(
            TagLinkEntity(parentTagId = "tag_me", childTagId = "tag_wfh"),
            TagLinkEntity(parentTagId = "tag_me", childTagId = "tag_jog"),
            TagLinkEntity(parentTagId = "tag_me", childTagId = "tag_meeting"),
            TagLinkEntity(parentTagId = "tag_me", childTagId = "tag_reading"),
            TagLinkEntity(parentTagId = "tag_me", childTagId = "tag_coffee"),
            TagLinkEntity(parentTagId = "tag_me", childTagId = "tag_focus"),
            TagLinkEntity(parentTagId = "tag_partner", childTagId = "tag_dinner"),
            TagLinkEntity(parentTagId = "tag_partner", childTagId = "tag_relax"),
            TagLinkEntity(parentTagId = "tag_partner", childTagId = "tag_walk"),
            TagLinkEntity(parentTagId = "tag_kids", childTagId = "tag_school_run"),
            TagLinkEntity(parentTagId = "tag_kids", childTagId = "tag_walk"),
            TagLinkEntity(parentTagId = "tag_kids", childTagId = "tag_outside"),
            TagLinkEntity(parentTagId = "tag_friend", childTagId = "tag_coffee"),
            TagLinkEntity(parentTagId = "tag_friend", childTagId = "tag_lunch"),
            TagLinkEntity(parentTagId = "tag_friend", childTagId = "tag_meeting"),
            TagLinkEntity(parentTagId = "tag_friend", childTagId = "tag_cafe"),
            TagLinkEntity(parentTagId = "tag_wfh", childTagId = "tag_home"),
            TagLinkEntity(parentTagId = "tag_wfh", childTagId = "tag_focus"),
            TagLinkEntity(parentTagId = "tag_wfh", childTagId = "tag_coffee"),
            TagLinkEntity(parentTagId = "tag_jog", childTagId = "tag_outside"),
            TagLinkEntity(parentTagId = "tag_jog", childTagId = "tag_energized"),
            TagLinkEntity(parentTagId = "tag_meeting", childTagId = "tag_office"),
            TagLinkEntity(parentTagId = "tag_meeting", childTagId = "tag_focus"),
            TagLinkEntity(parentTagId = "tag_coffee", childTagId = "tag_cafe"),
            TagLinkEntity(parentTagId = "tag_coffee", childTagId = "tag_relax"),
            TagLinkEntity(parentTagId = "tag_lunch", childTagId = "tag_office"),
            TagLinkEntity(parentTagId = "tag_lunch", childTagId = "tag_relax"),
            TagLinkEntity(parentTagId = "tag_dinner", childTagId = "tag_home"),
            TagLinkEntity(parentTagId = "tag_dinner", childTagId = "tag_relax"),
            TagLinkEntity(parentTagId = "tag_walk", childTagId = "tag_outside"),
            TagLinkEntity(parentTagId = "tag_walk", childTagId = "tag_relax"),
            TagLinkEntity(parentTagId = "tag_focus", childTagId = "tag_energized"),
            TagLinkEntity(parentTagId = "tag_relax", childTagId = "tag_tired"),
        )

        tagDao.insertTags(tags)
        tagDao.insertLinks(links)
    }
}
