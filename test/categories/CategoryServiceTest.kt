package categories

import com.example.bookmarks.BookmarksRepository
import com.example.categories.CategoriesRepository
import com.example.categories.Category
import com.example.categories.CategoryService
import com.example.commons.mockDbTransactionCategories
import com.example.commons.mockDbTransactionNullableInt
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
private const val categoryName = "name"
internal const val categoryId: Long = 1
private const val bookmarkId: Long = 1
private val category: Category = Category(categoryId, categoryName)

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
        every { categoriesRepositoryMock.loadCategoriesById(categoryId) } returns categories

        // Action - When
        val result = testInstance.loadCategoriesById(categoryId)

        // Expected
        assertEquals(result, categories)
    }

    @Test
    fun updateCategoryWhenCategoryDoesntExist() {
        mockDbTransactionNullableInt(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findById(categoryId) } returns null

        testInstance.updateCategory(categoryId, categoryName)

        verify { categoriesRepositoryMock.findById(categoryId) }
        verify(exactly = 0) { categoriesRepositoryMock.findByCategoryName(categoryName) }
        verify(exactly = 0) { categoriesRepositoryMock.updateCategory(categoryId, categoryName) }
        verify(exactly = 0) { categoriesRepositoryMock.updateCategory(categoryId, categoryName) }
        verify(exactly = 0) { categoriesRepositoryMock.replaceId(categoryId, any()) }
    }

    @Test
    fun updateCategoryWhenCategoryNameAlreadyExists() {
        mockDbTransactionNullableInt(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findById(categoryId) } returns category
        every { categoriesRepositoryMock.findByCategoryName(categoryName) } returns category
        every { categoriesRepositoryMock.getId(categoryName) } returns categoryId
        every { categoriesRepositoryMock.replaceId(categoryId, any()) } returns categoryId.toInt()


        testInstance.updateCategory(categoryId, categoryName)

        verify { categoriesRepositoryMock.findById(categoryId) }
        verify { categoriesRepositoryMock.findByCategoryName(categoryName) }
        verify(exactly = 0) { categoriesRepositoryMock.updateCategory(categoryId, categoryName) }
        verify { categoriesRepositoryMock.replaceId(categoryId, any()) }
    }

    @Test
    fun updateCategoryWhenCategoryNameDoesntExists() {
        mockDbTransactionNullableInt(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findById(categoryId) } returns category
        every { categoriesRepositoryMock.findByCategoryName(categoryName) } returns null
        every { categoriesRepositoryMock.updateCategory(categoryId, categoryName) } returns categoryId.toInt()

        testInstance.updateCategory(categoryId, categoryName)

        verify { categoriesRepositoryMock.findById(categoryId) }
        verify { categoriesRepositoryMock.findByCategoryName(categoryName) }
        verify { categoriesRepositoryMock.updateCategory(categoryId, categoryName) }
        verify(exactly = 0) { categoriesRepositoryMock.replaceId(categoryId, any()) }
    }

    @Test
    fun createCategoryWhenAlreadyExists() {
        mockDbTransactionUnit(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findByCategoryName(categoryName) } returns category

        testInstance.createCategory(categoryName)

        verify { categoriesRepositoryMock.findByCategoryName(categoryName) }
        verify(exactly = 0) { categoriesRepositoryMock.createCategory(categoryName) }
    }

    @Test
    fun createCategoryWhenDoesntExists() {
        mockDbTransactionUnit(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findByCategoryName(categoryName) } returns null
        every { categoriesRepositoryMock.createCategory(categoryName) } returns categoryId

        testInstance.createCategory(categoryName)

        verify { categoriesRepositoryMock.findByCategoryName(categoryName) }
        verify { categoriesRepositoryMock.createCategory(categoryName) }
    }

    @Test
    fun deleteCategoryWhenDoesntExist() {
        // Setup - Given
        mockDbTransactionUnit(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findIdByCategory(categoryName) } returns null

        // Action - When
        testInstance.deleteCategory(categoryName)

        // Expected
        verify { categoriesRepositoryMock.findIdByCategory(categoryName) }
        verify(exactly = 0) { bookmarksRepositoryMock.findByCategoryId(categoryId) }
        verify(exactly = 0) { categoriesRepositoryMock.deleteCategory(categoryId) }
    }

    @Test
    fun deleteCategoryWhenInvalidBecauseIsBeingUsed() {
        // Setup - Given
        mockDbTransactionUnit(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findIdByCategory(categoryName) } returns categoryId
        every { bookmarksRepositoryMock.findByCategoryId(categoryId) } returns bookmarkId

        // Action - When
        testInstance.deleteCategory(categoryName)

        // Expected
        verify { categoriesRepositoryMock.findIdByCategory(categoryName) }
        verify(exactly = 1) { bookmarksRepositoryMock.findByCategoryId(categoryId) }
        verify(exactly = 0) { categoriesRepositoryMock.deleteCategory(categoryId) }
    }

    @Test
    fun deleteCategorySuccessfully() {
        // Setup - Given
        mockDbTransactionUnit(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findIdByCategory(categoryName) } returns categoryId
        every { bookmarksRepositoryMock.findByCategoryId(categoryId) } returns null
        every { categoriesRepositoryMock.deleteCategory(categoryId) } returns categoryId.toInt()

        // Action - When
        testInstance.deleteCategory(categoryName)

        // Expected
        verify { categoriesRepositoryMock.findIdByCategory(categoryName) }
        verify(exactly = 1) { bookmarksRepositoryMock.findByCategoryId(categoryId) }
        verify(exactly = 1) { categoriesRepositoryMock.deleteCategory(categoryId) }
    }
}
