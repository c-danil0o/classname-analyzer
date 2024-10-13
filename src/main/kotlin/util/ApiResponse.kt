package org.example.util

import kotlinx.serialization.Serializable

@Serializable
data class Repository(
    val name: String,
    val full_name: String,
    val owner: Owner,
    val trees_url: String,
    val default_branch: String,
)
@Serializable
data class Owner(
    val login: String,
)

@Serializable
data class SearchResult(
    val items: List<Repository>
)

@Serializable
data class Tree (
    val tree: List<File>,
)

@Serializable
data class File(
    val path: String,
)
