package com.example

import com.example.bookmarks.BookmarkService
import com.example.bookmarks.BookmarksRepository
import com.example.categories.CategoriesRepository
import com.example.categories.CategoryService

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import com.fasterxml.jackson.databind.*
import io.ktor.jackson.*
import io.ktor.features.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

data class Error(val error: String)
data class Result(val result: String)
data class CreateBookmarkRequest(var name: String? = null, var url: String? = null, var category: String? = null)
data class CreateCategoryRequest(var id: Long? = null, var category: String? = null)

fun Application.module(testing: Boolean = false) {

    install(StatusPages) {
        exception<IllegalArgumentException> { cause ->
            call.respond(HttpStatusCode.BadRequest, Error(cause.message ?: "unknown system exception"))
        }
    }
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    val db = Database.connect(
        "jdbc:mysql://localhost:3306/bookmarks1?serverTimezone=UTC", driver = "com.mysql.cj.jdbc.Driver",
        user = "root", password = "@Bestyear2020"
    )

    val repositoryBookmarks = BookmarksRepository()
    val repositoryCategories = CategoriesRepository()
    val bookmarkService = BookmarkService(repositoryBookmarks, repositoryCategories, db)
    val categoryService = CategoryService(repositoryCategories, repositoryBookmarks, db)

    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, Result("bookmark application"))
        }

        route("/bookmarks") {
            get("/") {
                call.respond(
                    HttpStatusCode.OK,
                    bookmarkService.findBookmarks(call.request.queryParameters["searchText"])
                )
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toLong() ?: throw IllegalArgumentException("there isn't id")

                call.respond(HttpStatusCode.OK, bookmarkService.loadBookmarkById(id = id))
            }

            get("/categories/{category}") {
                val category = call.parameters["category"] ?: throw IllegalArgumentException("category error")

                call.respond(HttpStatusCode.OK, bookmarkService.bookmarksByCategory(category.replace("_", " ")))
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: throw IllegalArgumentException("bad id")
                val parameters = call.receive<CreateBookmarkRequest>()
                val name = parameters.name
                val url = parameters.url
                val category = parameters.category

                if( name == null || url == null || category == null) throw IllegalArgumentException("there is nothing to update")

                bookmarkService.updateBookmark(id = id, name = name, url = url, category = category)
                call.respond(HttpStatusCode.OK, Result("the bookmark $id has been uploaded"))
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: throw IllegalArgumentException("bad id")

                bookmarkService.deleteBookmark(id = id)
                call.respond(HttpStatusCode.OK, Result("the bookmark $id has been delete"))
            }

            post("/") {
                val parameters = call.receive<CreateBookmarkRequest>()
                val name = parameters.name ?: throw IllegalArgumentException("missing parameter name")
                val url = parameters.url ?: throw IllegalArgumentException("missing parameter url")
                val category = parameters.category ?: "other"

                bookmarkService.createBookmark(name = name, url = url, category = category)
                call.respond(HttpStatusCode.OK, Result("the bookmark has been created successfully"))
            }
        }

        route("/categories") {
            get("/") {
                call.respond(
                    HttpStatusCode.OK,
                    categoryService.findCategories(call.request.queryParameters["searchText"])
                )
            }

            get("/{id}") {
                val id = call.parameters["id"]!!.toLong()

                call.respond(HttpStatusCode.OK, categoryService.loadCategoriesById(id = id))
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: throw IllegalArgumentException("bad id")
                val parameters = call.receive<CreateCategoryRequest>()
                val category = parameters.category ?: throw IllegalArgumentException("there isn't category")

                categoryService.updateCategory(id, category)
                call.respond(HttpStatusCode.OK, Result("the bookmark $id has been uploaded"))
            }

            delete("/{category}"){
                val category = call.parameters["category"] ?: throw  IllegalArgumentException("category not found")

                categoryService.deleteCategory(category)
                call.respond(HttpStatusCode.OK, Result("the category $category has been deleted"))
            }

            post("/") {
                val parameter = call.receive<CreateCategoryRequest>()
                val category = parameter.category ?: throw IllegalArgumentException("there is not category")

                categoryService.createCategory(category)
                call.respond(HttpStatusCode.OK, Result("create the bookmark"))
            }
        }
    }
}

