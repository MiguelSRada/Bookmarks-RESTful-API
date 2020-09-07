package com.example.bookmarks

import com.example.Bookmarks
import com.example.Categories
import com.example.categories.CategoriesRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class BookmarkService(
    private val bookmarksRepository: BookmarksRepository,
    private val categoriesRepository: CategoriesRepository,
    private val db: Database
) {
    fun findBookmarks(searchText: String?): List<Bookmark> = transaction(db)
    {
        when (searchText) {
            null -> bookmarksRepository.loadBookmarks()
            else -> bookmarksRepository.loadBookmarksBySearchText(searchText)
        }
    }

    fun loadBookmarkById(id: Long): List<Bookmark> = transaction(db) { bookmarksRepository.loadBookmarkById(id) }

    fun bookmarksByCategory(category: String): List<Bookmark> =
        transaction(db) { bookmarksRepository.bookmarksByCategory(category) }

    fun updateBookmark(id: Long, name: String? = null, url: String? = null, category: String? = null) = transaction(db)
    {
        bookmarksRepository.updateBookmark(id, name, url, category?.let { getCategoryId(it) })
    }

    fun deleteBookmark(id: Long) = transaction(db) { bookmarksRepository.deleteBookmark(id) }

    fun createBookmark(name: String, url: String, category: String) = transaction(db)
    {
        val result = categoriesRepository.findByCategory(category)
        if (result == null) categoriesRepository.createCategory(category)

        bookmarksRepository.createBookmark(name, url, categoriesRepository.getId(category))
    }

    private fun getCategoryId(category: String): Long {
        val categoryId = categoriesRepository.findIdByCategory(category)

        return categoryId ?: categoriesRepository.createCategory(category)
    }

}
