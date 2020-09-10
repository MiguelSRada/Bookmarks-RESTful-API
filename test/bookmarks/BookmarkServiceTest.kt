package bookmarks

import com.example.bookmarks.Bookmark
import com.example.bookmarks.BookmarkService
import com.example.bookmarks.BookmarksRepository
import com.example.categories.CategoriesRepository
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
private const val longId: Long = 1
private const val category = "categoryName"
private const val longBookmarkId : Long = 1


internal class BookmarkServiceTest {
    private val categoriesRepositoryMock: CategoriesRepository = mockk()
    private var bookmarksRepositoryMock: BookmarksRepository = mockk()
    private var dbMock: Database = mockk()
    private var dbTransactionMock: Transaction = mockk()

    private val testInstance: BookmarkService =
        BookmarkService(bookmarksRepositoryMock, categoriesRepositoryMock, dbMock)


    @Test//passed
    fun findBookmarksWhenSearchTextIsNull() {
        // Setup - Given
        mockDbTransactionBookmark(dbMock, dbTransactionMock)
        every { bookmarksRepositoryMock.loadBookmarks() } returns bookmarks

        // Action - When
        val result = testInstance.findBookmarks(null)

        // Expected
        assertEquals(result, bookmarks)

    }

    @Test//passed
    fun findBookmarksWhenSearchTextIsNotNull() {
        // Setup - Given
        mockDbTransactionBookmark(dbMock, dbTransactionMock)
        every { bookmarksRepositoryMock.loadBookmarksBySearchText(searchText) } returns bookmarks

        // Action - When
        val result = testInstance.findBookmarks(searchText)

        // Expected
        assertEquals(result, bookmarks)

    }

    @Test//passed
    fun loadBookmarkById() {
        // Setup - Given
        mockDbTransactionBookmark(dbMock, dbTransactionMock)
        every { bookmarksRepositoryMock.loadBookmarkById(longId) } returns bookmarks

        // Action - When
        val result = testInstance.loadBookmarkById(longId)

        // Expected
        assertEquals(result, bookmarks)
    }

    @Test//passed
    fun bookmarksByCategory() {
        // Setup - Given
        mockDbTransactionBookmark(dbMock, dbTransactionMock)
        every { bookmarksRepositoryMock.bookmarksByCategory(category) } returns bookmarks

        // Action - When
        val result = testInstance.bookmarksByCategory(category)

        // Expected
        assertEquals(result, bookmarks)
    }

    @Test//passed
    fun dontUpdateBookmark() {
        // Setup - Given
        mockDbTransactionInt(dbMock, dbTransactionMock)
        every { bookmarksRepositoryMock.updateBookmark(longId, null, null, null) } returns longId.toInt()

        // Action - When
        val result = testInstance.updateBookmark(longId, null, null, null)

        // Expected
        assertEquals(result, longId.toInt())
    }
    /*
    @Test//passed
    fun updateBookmark() {
        // Setup - Given
        mockDbTransactionInt(dbMock, dbTransactionMock)
        every { bookmarksRepositoryMock.updateBookmark(longId, null, null, longId) } returns longBookmarkId.toInt()
        every { categoriesRepositoryMock.findByCategory(category) } returns category

        // Action - When
        val result = testInstance.updateBookmark(longId, null, null, category)

        // Expected
        assertEquals(result, longBookmarkId.toInt())
    }
    */

    @Test//passed
    fun deleteBookmark() {
        // Setup - Given
        mockDbTransactionInt(dbMock, dbTransactionMock)
        every { bookmarksRepositoryMock.deleteBookmark(longId) } returns longId.toInt()

        // Action - When
        val result = testInstance.deleteBookmark(longId)

        // Expected
        assertEquals(result, longId.toInt())
    }

    @Test
    fun createBookmarkWithExistingCategory()  {
        // Setup - Given
        mockDbTransactionLong(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findByCategory(category) } returns category
        every { categoriesRepositoryMock.getId(category) } returns longId
        every { bookmarksRepositoryMock
            .createBookmark("name", "url", longId) } returns longBookmarkId

        // Action - When
        val result = testInstance.createBookmark("name", "url", category)

        // Expected
        verify { categoriesRepositoryMock.findByCategory(category) }
        verify(exactly = 0) { categoriesRepositoryMock.createCategory(category) }
        verify { bookmarksRepositoryMock.createBookmark("name", "url", longId )}

        assertEquals(result, longBookmarkId)
    }

    @Test
    fun createBookmarkWithNewCategory() {
        // Setup - Given
        mockDbTransactionLong(dbMock, dbTransactionMock)
        every { categoriesRepositoryMock.findByCategory(category) } returns null
        every { categoriesRepositoryMock.createCategory(category) } returns longId
        every { categoriesRepositoryMock.getId(category) } returns longId
        every { bookmarksRepositoryMock
            .createBookmark("name", "url", longId) } returns longBookmarkId

        // Action - When
        val result = testInstance.createBookmark("name", "url", category)

        // Expected
        verify { categoriesRepositoryMock.findByCategory(category) }
        verify { categoriesRepositoryMock.createCategory(category) }
        verify { bookmarksRepositoryMock.createBookmark("name", "url", longId )}

        assertEquals(result, longBookmarkId)
    }
}