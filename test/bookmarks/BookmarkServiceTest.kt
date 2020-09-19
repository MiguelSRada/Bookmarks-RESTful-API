package bookmarks

import categories.categoryId
import com.example.bookmarks.Bookmark
import com.example.bookmarks.BookmarkService
import com.example.bookmarks.BookmarksRepository
import com.example.categories.CategoriesRepository
import com.example.categories.Category
import com.example.commons.mockDbTransactionBookmark
import com.example.commons.mockDbTransactionInt
import com.example.commons.mockDbTransactionLong
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.junit.jupiter.api.Test

import kotlin.test.assertEquals

private val bookmarks = emptyList<Bookmark>()
private const val searchText = "my-search"
private const val categoryId: Long = 1
private const val categoryName: String = "categoryName"
private const val longBookmarkId : Long = 1
private val category: Category = Category(categoryId, categoryName)



internal class BookmarkServiceTest {
    private val categoriesRepositoryMock: CategoriesRepository = mockk()
    private var bookmarksRepositoryMock: BookmarksRepository = mockk()
    private var dbMock: Database = mockk()
    private var dbTransactionMock: Transaction = mockk()

    private val testInstance: BookmarkService =
        BookmarkService(bookmarksRepositoryMock, categoriesRepositoryMock, dbMock)


    @Test
    fun findBookmarksWhenSearchTextIsNull() {
        // Setup - Given
        mockDbTransactionBookmark(dbMock, dbTransactionMock)
        every { bookmarksRepositoryMock.loadBookmarks() } returns bookmarks

        // Action - When
        val result = testInstance.findBookmarks(null)

        // Expected
        assertEquals(result, bookmarks)

    }

    @Test
    fun findBookmarksWhenSearchTextIsNotNull() {
        // Setup - Given
        mockDbTransactionBookmark(dbMock, dbTransactionMock)
        every { bookmarksRepositoryMock.loadBookmarksBySearchText(searchText) } returns bookmarks

        // Action - When
        val result = testInstance.findBookmarks(searchText)

        // Expected
        assertEquals(result, bookmarks)

    }

    @Test
    fun loadBookmarkById() {
        // Setup - Given
        mockDbTransactionBookmark(dbMock, dbTransactionMock)
        every { bookmarksRepositoryMock.loadBookmarkById(longBookmarkId) } returns bookmarks

        // Action - When
        val result = testInstance.loadBookmarkById(longBookmarkId)

        // Expected
        assertEquals(result, bookmarks)
    }

    @Test
    fun loadBookmarksByCategory() {
        // Setup - Given
        mockDbTransactionBookmark(dbMock, dbTransactionMock)
        every { bookmarksRepositoryMock.bookmarksByCategory(categoryName) } returns bookmarks

        // Action - When
        val result = testInstance.bookmarksByCategory(categoryName)

        // Expected
        assertEquals(result, bookmarks)
    }

    @Test
    fun updateBookmarkWhenDataIsNull() {
        // Setup - Given
        mockDbTransactionInt(dbMock, dbTransactionMock)
        every { bookmarksRepositoryMock.updateBookmark(longBookmarkId, null, null, null) } returns longBookmarkId.toInt()

        // Action - When
        val result = testInstance.updateBookmark(longBookmarkId, null, null, null)

        // Expected
        assertEquals(result, longBookmarkId.toInt())
    }

    @Test
    fun deleteBookmark() {
        // Setup - Given
        mockDbTransactionInt(dbMock, dbTransactionMock)
        every { bookmarksRepositoryMock.deleteBookmark(longBookmarkId) } returns longBookmarkId.toInt()

        // Action - When
        val result = testInstance.deleteBookmark(longBookmarkId)

        // Expected
        assertEquals(result, longBookmarkId.toInt())
    }

    @Test
    fun createBookmarkWhenItsCategoryAlreadyExist()  {
        // Setup - Given
        mockDbTransactionLong(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findByCategoryName(categoryName) } returns category
        every { categoriesRepositoryMock.getId(categoryName) } returns longBookmarkId
        every { bookmarksRepositoryMock.createBookmark("name", "url", longBookmarkId ) } returns longBookmarkId

        // Action - When
        val result = testInstance.createBookmark("name", "url", categoryName)

        // Expected
        verify { categoriesRepositoryMock.findByCategoryName(categoryName) }
        verify(exactly = 0) { categoriesRepositoryMock.createCategory(categoryName) }
        verify { bookmarksRepositoryMock.createBookmark("name", "url", longBookmarkId )}

        assertEquals(result, longBookmarkId)
    }

    @Test
    fun createBookmarkWhenItsCategoryDoesntExist() {
        // Setup - Given
        mockDbTransactionLong(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findByCategoryName(categoryName) } returns null
        every { categoriesRepositoryMock.createCategory(categoryName) } returns categoryId
        every { categoriesRepositoryMock.getId("categoryName") } returns categoryId
        every { bookmarksRepositoryMock.createBookmark("name", "url", categoryId) } returns longBookmarkId

        // Action - When
        val result = testInstance.createBookmark("name", "url", categoryName)

        // Expected
        verify { categoriesRepositoryMock.findByCategoryName(categoryName) }
        verify { categoriesRepositoryMock.createCategory(categoryName) }
        verify { bookmarksRepositoryMock.createBookmark("name", "url", categoryId )}

        assertEquals(result, longBookmarkId)
    }
}