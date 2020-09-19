package com.example.bookmarks

import com.example.categories.CategoriesRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class BookmarkService(
    private val bookmarksRepository: BookmarksRepository,
    private val categoriesRepository: CategoriesRepository,
    private val db: Database
) {
    fun findBookmarks(searchText: String?): List<Bookmark> = transaction(db) {
        when (searchText) {
            null -> bookmarksRepository.loadBookmarks()
            else -> bookmarksRepository.loadBookmarksBySearchText(searchText)
        }
    }

    fun loadBookmarkById(id: Long): List<Bookmark> = transaction(db) { bookmarksRepository.loadBookmarkById(id) }

    fun bookmarksByCategory(categoryName: String): List<Bookmark> = transaction(db) {
        bookmarksRepository.bookmarksByCategory(categoryName)
    }

    fun updateBookmark(id: Long, name: String? = null, url: String? = null, categoryName: String? = null) =
        transaction(db) {
            bookmarksRepository.updateBookmark(id, name, url, categoryName?.let { getCategoryId(it) })
        }

    fun deleteBookmark(id: Long) = transaction(db) { bookmarksRepository.deleteBookmark(id) }

    fun createBookmark(name: String, url: String, categoryName: String) = transaction(db) {
        val result = categoriesRepository.findByCategoryName(categoryName)
        if (result == null) categoriesRepository.createCategory(categoryName)

        bookmarksRepository.createBookmark(name, url, categoriesRepository.getId(categoryName))
    }

    private fun getCategoryId(categoryName: String): Long {
        val categoryId = categoriesRepository.findIdByCategory(categoryName)

        return categoryId ?: categoriesRepository.createCategory(categoryName)
    }

}
