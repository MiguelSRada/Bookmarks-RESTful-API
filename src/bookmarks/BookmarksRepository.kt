package com.example.bookmarks

import com.example.Bookmarks
import com.example.Bookmarks.name
import com.example.Bookmarks.url
import com.example.Categories
import org.jetbrains.exposed.sql.*

class BookmarksRepository() {
    fun createBookmark(name: String, url: String, category_id: Long): Long {
        return Bookmarks.insert {
            it[Bookmarks.name] = name
            it[Bookmarks.url] = url
            it[Bookmarks.category_id] = category_id
        }[Bookmarks.id]
    }

    fun loadBookmarkById(id: Long): List<Bookmark> =
        Bookmarks
            .select { Bookmarks.id eq id }
            .map { Bookmarks.toBookmark(it) }

    fun deleteBookmark(id: Long) = Bookmarks.deleteWhere { Bookmarks.id eq id }

    fun updateBookmark(id: Long, name: String? = null, url: String? = null, category_id: Long? = null) =
        Bookmarks.update({ Bookmarks.id eq id })
        {
            if (name != null) it[Bookmarks.name] = name
            if (url != null) it[Bookmarks.url] = url
            if (category_id != null) it[Bookmarks.category_id] = category_id
        }

    fun loadBookmarks(): List<Bookmark> = Bookmarks.selectAll().map { Bookmarks.toBookmark(it) }

    fun loadBookmarksBySearchText(searchText: String): List<Bookmark> =
        Bookmarks.select { (name like searchText(searchText)) or (url like searchText(searchText)) }
            .map { Bookmarks.toBookmark(it) }

    fun bookmarksByCategory(category: String): List<Bookmark> =
        Bookmarks.select { Bookmarks.category_id eq getId(category) }
            .map { Bookmarks.toBookmark(it) }

    private fun getId(category: String): Long =
        Categories.select { Categories.category eq category }
            .map { it[Categories.id] }
            .first()

    private fun Bookmarks.toBookmark(row: ResultRow): Bookmark =
        Bookmark(name = row[name], url = row[url], category_id = row[category_id])

    private fun searchText(searchText: String): String {
        var newString = searchText
        if (newString.endsWith("%").not()) newString = "$newString%"
        if (newString.startsWith("%").not()) newString = "%$newString"
        return newString
    }

    fun findByCategoryId(categoryId: Long): Long? =
        Bookmarks.select { Bookmarks.category_id eq categoryId }
            .map { it[Bookmarks.id] }
            .firstOrNull()
}




