package com.example.categories

import com.example.Bookmarks
import com.example.Categories
import org.jetbrains.exposed.sql.*

class CategoriesRepository() {
    fun loadCategories(): List<Category> = Categories.selectAll().map { Categories.toCategory(it) }

    fun createCategory(category: String): Long =
        Categories.insert { it[Categories.category] = category }[Categories.id]

    fun loadCategoriesBySearchText(searchText: String): List<Category> =
        Categories.select { Categories.category like searchText(searchText) }
            .map { Categories.toCategory(it) }

    fun updateCategory(id: Long, category: String) =
        Categories.update({ Categories.id eq id }) { it[Categories.category] = category }

    fun loadCategoriesById(id: Long): List<Category> =
        Categories.select { Categories.id eq id }
            .orderBy(Categories.id)
            .map { Categories.toCategory(it) }

    fun getId(category: String): Long =
        Categories.select { Categories.category eq category }
            .map { it[Categories.id] }
            .first()

    fun deleteCategory(categoryId: Long) = Categories.deleteWhere { Categories.id eq categoryId }

    fun findById(id: Long): Long? =
        Categories.select { Categories.id eq id }
            .map { it[Categories.id] }
            .firstOrNull()

    fun findByCategory(category: String): String? =
        Categories.select { Categories.category eq category }
            .map { it[Categories.category] }
            .firstOrNull()

    fun replaceId(oldId: Long, newId: Long) = Bookmarks.update({ Bookmarks.category_id eq oldId })
    {
        it[category_id] = newId
    }

    fun
            findIdByCategory(category: String): Long? =
        Categories.select { Categories.category eq category }
            .map { it[Categories.id] }
            .firstOrNull()

    private fun Categories.toCategory(row: ResultRow): Category = Category(id = row[id], category = row[category])

    private fun searchText(searchText: String): String {
        var newString = searchText
        if (newString.endsWith("%").not()) newString = "$newString%"
        if (newString.startsWith("%").not()) newString = "%$newString"
        return newString
    }
}
