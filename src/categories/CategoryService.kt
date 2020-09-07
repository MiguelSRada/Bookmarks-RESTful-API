package com.example.categories

import com.example.Bookmarks
import com.example.Categories
import com.example.bookmarks.BookmarksRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class CategoryService(
    private val categoriesRepository: CategoriesRepository,
    private val bookmarksRepository: BookmarksRepository,
    private val db: Database
) {
    fun findCategories(searchText: String?) = transaction(db)
    {
        when (searchText) {
            null -> categoriesRepository.loadCategories()
            else -> categoriesRepository.loadCategoriesBySearchText(searchText)
        }
    }

    fun loadCategoriesById(id: Long) = transaction(db) { categoriesRepository.loadCategoriesById(id) }

    fun updateCategory(id: Long, category: String) = transaction(db) {
        categoriesRepository.findById(id)?.let {
            val categoryQuery: String? = categoriesRepository.findByCategory(category)
            if (categoryQuery == null) {
                categoriesRepository.updateCategory(id, category)
            } else {
                categoriesRepository.replaceId(id, categoriesRepository.getId(category))
            }
        }
    }

    fun createCategory(category: String) = transaction(db) {
        val result: String? = categoriesRepository.findByCategory(category)
        if (result == null) categoriesRepository.createCategory(category)
    }

    fun deleteCategory(category: String) {
        categoriesRepository.findIdByCategory(category)?.let {
            val queryDelete: Long? = bookmarksRepository.findByCategoryId(it)
            if (queryDelete == null) categoriesRepository.deleteCategory(it)
        }
    }
}