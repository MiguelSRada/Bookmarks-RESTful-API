package com.example

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table


object Bookmarks : Table(name = "bookmarks") {
    val id = long("id").primaryKey().autoIncrement()
    val name = varchar("name", length = 255)
    val url = varchar("url", length = 255)
}

object Categories : Table(name = "categories") {
    val id = long("id").references(Bookmarks.id, onDelete = ReferenceOption.CASCADE).primaryKey()
    val category = varchar("category", length = 255)
}