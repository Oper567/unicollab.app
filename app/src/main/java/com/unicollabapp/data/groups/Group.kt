package com.unicollabapp.data.groups

data class Group(
    val id: String = "",
    val name: String = "",
    val uni: String = "",
    val dept: String = "",
    val level: String = "",
    val code: String = "",
    val ownerId: String = "",
    val members: List<String> = emptyList()
)
