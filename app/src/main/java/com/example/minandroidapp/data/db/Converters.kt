package com.example.minandroidapp.data.db

import androidx.room.TypeConverter
import com.example.minandroidapp.model.TagCategory
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun fromTagCategory(category: TagCategory?): String? = category?.name

    @TypeConverter
    fun toTagCategory(name: String?): TagCategory? = name?.let(TagCategory::valueOf)
}
