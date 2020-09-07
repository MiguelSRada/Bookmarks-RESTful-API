package com.example

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table


object Bookmarks : Table(name = "bookmarks") {
    val id = long("id").primaryKey().autoIncrement()
    val name = varchar("name", length = 255)
    val url = varchar("url", length = 255)
    val category_id = long("category_id").references(Categories.id)
}

object Categories : Table(name = "categories") {
    val id = long("id").primaryKey().autoIncrement()
    val category = varchar("category", length = 255).primaryKey()
}