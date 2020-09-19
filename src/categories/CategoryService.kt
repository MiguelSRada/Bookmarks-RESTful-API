package com.example.categories

import com.example.bookmarks.BookmarksRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

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

    fun updateCategory(id: Long, categoryName: String) = transaction(db) {
        categoriesRepository.findById(id)?.let {
            if (categoriesRepository.findByCategoryName(categoryName) == null) {
                categoriesRepository.updateCategory(id, categoryName)
            } else {
                categoriesRepository.replaceId(id, categoriesRepository.getId(categoryName))
            }
        }
    }

    fun createCategory(categoryName: String) = transaction(db) {
        val result = categoriesRepository.findByCategoryName(categoryName)
        if (result == null) categoriesRepository.createCategory(categoryName)
    }

    fun deleteCategory(categoryName: String) {
        categoriesRepository.findIdByCategory(categoryName)?.let {
            val queryDelete: Long? = bookmarksRepository.findByCategoryId(it)
            if (queryDelete == null) categoriesRepository.deleteCategory(it)
        }
    }
}