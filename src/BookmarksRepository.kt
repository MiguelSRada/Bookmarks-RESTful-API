package com.example

import com.example.Bookmarks.name
import com.example.Bookmarks.url
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class BookmarksRepository(private val db: Database) {
    fun createBookmark(name: String, url: String) = transaction(db)
    {
        Bookmarks.insert { it[Bookmarks.name] = name; it[Bookmarks.url] = url }
    }

    fun findBookmarks(searchText: String?): List<Bookmark> = transaction(db)
    {
        when (searchText) {
            null -> loadBookmarks()
            else -> loadBookmarksBySearchText(searchText(searchText))
        }
    }

    fun loadBookmarkById(id: Long): List<Bookmark> = transaction(db)
    {
        Bookmarks.select { Bookmarks.id eq id }.map { Bookmarks.toBookmark(it) }
    }

    fun deleteBookmark(id: Long) = transaction(db) { Bookmarks.deleteWhere { Bookmarks.id eq id } }

    fun updateBookmark(id: Long, name: String? = null, url: String? = null) = transaction(db)
    {
        Bookmarks.update({ Bookmarks.id eq id })
        {
            if (name != null) {
                it[Bookmarks.name] = name
            }
            if (url != null) {
                it[Bookmarks.url] = url
            }
        }
    }

    private fun loadBookmarks(): List<Bookmark> = transaction(db)
    {
        Bookmarks.selectAll().map { Bookmarks.toBookmark(it) }
    }

    private fun loadBookmarksBySearchText(searchText: String): List<Bookmark> = transaction(db)
    {
        Bookmarks.select { (name like searchText) or (url like searchText) }.map { Bookmarks.toBookmark(it) }
    }

    fun createCategory(id: Long, category: String) = transaction(db)
    {
        Categories.insert { it[Categories.id] = id; it[Categories.category] = category }
    }

    fun findCategories(searchText: String?): List<Category> = transaction(db)
    {
        when (searchText) {
            null -> loadCategories()
            else -> loadCategoriesBySearchText(searchText(searchText))
        }
    }

    fun loadCategoriesById(id: Long) = transaction(db)
    {
        Categories.select { Categories.id eq id }.orderBy(Categories.id).map { Categories.toCategory(it) }
    }

    fun updateCategory(id: Long, category: String) = transaction(db)
    {
        Categories.update({ Categories.id eq id }) { it[Categories.category] = category }
    }

    private fun loadCategories(): List<Category> = transaction(db)
    {
        Categories.selectAll().groupBy(Categories.id).orderBy(Categories.id).map { Categories.toCategory(it) }
    }

    private fun loadCategoriesBySearchText(searchText: String): List<Category> = transaction(db)
    {
        Categories.select { Categories.category like searchText }
            .map { Categories.toCategory(it) }
    }

    fun bookmarksByCategory() = transaction(db)
    {
        /*(Bookmarks leftJoin  Categories)
            .slice(Categories.category, name, url)
            .selectAll()
            .map { it[Categories.category] to  Bookmarks.toBookmark(it) }

         */
        Bookmarks.selectAll().orderBy(Bookmarks.id).map { Bookmarks.toBookmark(it) }
    }

    private fun Bookmarks.toBookmark(row: ResultRow): Bookmark = Bookmark(name = row[name], url = row[url])
    private fun Categories.toCategory(row: ResultRow): Category = Category(id = row[id], category = row[category])
    private fun searchText(searchText: String): String {
        var newString = searchText
        if (newString.endsWith("%").not()) newString = "$newString%"
        if (newString.startsWith("%").not()) newString = "%$newString"
        return newString
    }
}




