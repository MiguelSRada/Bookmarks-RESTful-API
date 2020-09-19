package com.example.categories

import com.example.Bookmarks
import com.example.Categories
import org.jetbrains.exposed.sql.*

class CategoriesRepository {
    fun loadCategories(): List<Category> = Categories.selectAll().map { Categories.toCategory(it) }

    fun createCategory(categoryName: String): Long = Categories
        .insert { it[Categories.categoryName] = categoryName }[Categories.id]

    fun loadCategoriesBySearchText(searchText: String): List<Category> = Categories
        .select { Categories.categoryName like searchText(searchText) }
        .map { Categories.toCategory(it) }

    fun updateCategory(id: Long, categoryName: String) = Categories
        .update({ Categories.id eq id }) { it[Categories.categoryName] = categoryName }

    fun loadCategoriesById(id: Long): List<Category> = Categories
        .select { Categories.id eq id }
        .orderBy(Categories.id)
        .map { Categories.toCategory(it) }

    fun getId(categoryName: String): Long = Categories
        .select { Categories.categoryName eq categoryName }
        .map { it[Categories.id] }
        .first()

    fun deleteCategory(categoryId: Long) = Categories
        .deleteWhere { Categories.id eq categoryId }

    fun findById(id: Long): Category? = Categories
        .select { Categories.id eq id }
        .map { Categories.toCategory(it) }
        .firstOrNull()

    fun findByCategoryName(categoryName: String): Category? = Categories
        .select { Categories.categoryName eq categoryName }
        .map { Categories.toCategory(it) }
        .firstOrNull()

    fun replaceId(oldId: Long, newId: Long) = Bookmarks
        .update({ Bookmarks.categoryId eq oldId }) { it[categoryId] = newId }

    fun findIdByCategory(category: String): Long? = Categories
        .select { Categories.categoryName eq category }
        .map { it[Categories.id] }
        .firstOrNull()

    private fun Categories.toCategory(row: ResultRow) = Category(id = row[id], categoryName = row[categoryName])

    private fun searchText(searchText: String): String {
        var newString = searchText
        if (newString.endsWith("%").not()) newString = "$newString%"
        if (newString.startsWith("%").not()) newString = "%$newString"
        return newString
    }
}

