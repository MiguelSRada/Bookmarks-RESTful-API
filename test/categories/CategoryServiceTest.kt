package categories

import com.example.bookmarks.BookmarksRepository
import com.example.categories.CategoriesRepository
import com.example.categories.Category
import com.example.categories.CategoryService
import com.example.commons.mockDbTransactionCategories
import com.example.commons.mockDbTransactionUnit
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private const val searchText = "my-search"
private val categories = emptyList<Category>()
private const val category = "name"
private const val longId: Long = 1
private const val bookmarkId: Long = 1

internal class CategoryServiceTest {

    private val categoriesRepositoryMock: CategoriesRepository = mockk()
    private var bookmarksRepositoryMock: BookmarksRepository = mockk()
    private var dbMock: Database = mockk()
    private var dbTransactionMock: Transaction = mockk()

    private val testInstance: CategoryService =
        CategoryService(categoriesRepositoryMock, bookmarksRepositoryMock, dbMock)

    @Test//passed
    fun findCategoriesWhenSearchTextIsNull() {
        // Setup - Given
        mockDbTransactionCategories(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.loadCategories() } returns categories

        // Action - When
        val result = testInstance.findCategories(null)

        // Expected
        assertEquals(result, categories)
    }

    @Test//passed
    fun findCategoriesWhenSearchTextIsNotNull() {
        // Setup - Given
        mockDbTransactionCategories(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.loadCategoriesBySearchText(searchText) } returns categories

        // Action - When
        val result = testInstance.findCategories(searchText)

        // Expected
        assertEquals(result, categories)
    }

    @Test//passed
    fun loadCategoriesById() {
        // Setup - Given
        mockDbTransactionCategories(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.loadCategoriesById(longId) } returns categories

        // Action - When
        val result = testInstance.loadCategoriesById(longId)

        // Expected
        assertEquals(result, categories)
    }

    @Test
    fun updateCategory() {
    }

    @Test
    fun notCreateACategoryThatAlreadyExists() {
        mockDbTransactionUnit(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findByCategory(category) } returns category

        testInstance.createCategory(category)

        verify { categoriesRepositoryMock.findByCategory(category) }
        verify(exactly = 0) { categoriesRepositoryMock.createCategory(category) }
    }

    @Test
    fun createANewCategory() {
        mockDbTransactionUnit(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findByCategory(category) } returns null
        every { categoriesRepositoryMock.createCategory(category) } returns longId

        testInstance.createCategory(category)

        verify { categoriesRepositoryMock.findByCategory(category) }
        verify { categoriesRepositoryMock.createCategory(category) }
    }

    @Test
    fun deleteNonExistCategory() {
        // Setup - Given
        mockDbTransactionUnit(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findIdByCategory(category) } returns null

        // Action - When
        testInstance.deleteCategory(category)

        // Expected
        verify { categoriesRepositoryMock.findIdByCategory(category) }
        verify(exactly = 0) { bookmarksRepositoryMock.findByCategoryId(longId) }
        verify(exactly = 0) { categoriesRepositoryMock.deleteCategory(longId) }
    }

    @Test
    fun notDeleteExistingCategory() {
        // Setup - Given
        mockDbTransactionUnit(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findIdByCategory(category) } returns longId
        every { bookmarksRepositoryMock.findByCategoryId(longId) } returns bookmarkId

        // Action - When
        testInstance.deleteCategory(category)

        // Expected
        verify { categoriesRepositoryMock.findIdByCategory(category) }
        verify(exactly = 1) { bookmarksRepositoryMock.findByCategoryId(longId) }
        verify(exactly = 0) { categoriesRepositoryMock.deleteCategory(longId) }
    }

    @Test
    fun successfulDeleteCategory() {
        // Setup - Given
        mockDbTransactionUnit(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findIdByCategory(category) } returns longId
        every { bookmarksRepositoryMock.findByCategoryId(longId) } returns null
        every { categoriesRepositoryMock.deleteCategory(longId) } returns longId.toInt()

        // Action - When
        testInstance.deleteCategory(category)

        // Expected
        verify { categoriesRepositoryMock.findIdByCategory(category) }
        verify(exactly = 1) { bookmarksRepositoryMock.findByCategoryId(longId) }
        verify(exactly = 1) { categoriesRepositoryMock.deleteCategory(longId) }
    }
}
