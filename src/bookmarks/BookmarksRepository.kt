package com.example.bookmarks

import com.example.Bookmarks
import com.example.Bookmarks.name
import com.example.Bookmarks.url
import com.example.Categories
import org.jetbrains.exposed.sql.*

class BookmarksRepository {
    fun createBookmark(name: String, url: String, categoryId: Long): Long {
        return Bookmarks.insert {
            it[Bookmarks.name] = name
            it[Bookmarks.url] = url
            it[Bookmarks.categoryId] = categoryId
        }[Bookmarks.id]
    }

    fun loadBookmarkById(id: Long): List<Bookmark> =
        Bookmarks
            .select { Bookmarks.id eq id }
            .map { Bookmarks.toBookmark(it) }

    fun deleteBookmark(id: Long) = Bookmarks.deleteWhere { Bookmarks.id eq id }

    fun updateBookmark(id: Long, name: String? = null, url: String? = null, categoryId: Long? = null) =
        Bookmarks.update({ Bookmarks.id eq id })
        {
            if (name != null) it[Bookmarks.name] = name
            if (url != null) it[Bookmarks.url] = url
            if (categoryId != null) it[Bookmarks.categoryId] = categoryId
        }

    fun loadBookmarks(): List<Bookmark> = Bookmarks.selectAll().map { Bookmarks.toBookmark(it) }

    fun loadBookmarksBySearchText(searchText: String): List<Bookmark> =
        Bookmarks.select { (name like searchText(searchText)) or (url like searchText(searchText)) }
            .map { Bookmarks.toBookmark(it) }

    fun bookmarksByCategory(categoryName: String): List<Bookmark> =
        Bookmarks.select { Bookmarks.categoryId eq getId(categoryName) }
            .map { Bookmarks.toBookmark(it) }

    private fun getId(categoryName: String): Long =
        Categories.select { Categories.categoryName eq categoryName }
            .map { it[Categories.id] }
            .first()

    private fun Bookmarks.toBookmark(row: ResultRow): Bookmark =
        Bookmark(name = row[name], url = row[url], categoryId = row[categoryId])

    private fun searchText(searchText: String): String {
        var newString = searchText
        if (newString.endsWith("%").not()) newString = "$newString%"
        if (newString.startsWith("%").not()) newString = "%$newString"
        return newString
    }

    fun findByCategoryId(categoryId: Long): Long? =
        Bookmarks.select { Bookmarks.categoryId eq categoryId }
            .map { it[Bookmarks.id] }
            .firstOrNull()


}




