package com.example.minandroidapp.model

data class TagRelations(
    val tag: LogTag,
    val related: List<LogTag>,
)
