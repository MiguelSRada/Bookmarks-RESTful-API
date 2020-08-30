package com.example

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import com.fasterxml.jackson.databind.*
import io.ktor.jackson.*
import io.ktor.features.*
import org.jetbrains.exposed.sql.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

data class Error(val error: String)
data class Result(val result: String)
data class CreateBookmarkRequest(var name: String? = null, var url: String? = null)
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

    val repository = BookmarksRepository(db)

    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, Result("bookmark application"))
        }

        route("/bookmarks") {
            get("/") {
                call.respond(HttpStatusCode.OK, repository.findBookmarks(call.request.queryParameters["searchText"]))
            }

            get("/{id}") {
                val id = call.parameters["id"]!!.toLong()

                call.respond(HttpStatusCode.OK, repository.loadBookmarkById(id = id))
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: throw IllegalArgumentException("bad id")
                val parameters = call.receive<CreateBookmarkRequest>()
                val name = parameters.name
                val url = parameters.url

                repository.updateBookmark(id = id, name = name, url = url)
                call.respond(HttpStatusCode.OK, Result("the bookmark $id has been uploaded"))
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: throw IllegalArgumentException("bad id")

                repository.deleteBookmark(id = id)
                call.respond(HttpStatusCode.OK, Result("the bookmark $id has been delete"))
            }

            post("/") {
                val parameters = call.receive<CreateBookmarkRequest>()
                val name = parameters.name ?: throw IllegalArgumentException("missing parameter name")
                val url = parameters.url ?: throw IllegalArgumentException("missing parameter url")

                repository.createBookmark(name = name, url = url)
                call.respond(HttpStatusCode.OK, Result("the bookmark has been created successfully"))
            }
        }

        route("/categories") {
            get("/") {
                call.respond(HttpStatusCode.OK, repository.findCategories(call.request.queryParameters["searchText"]))
            }

            get("/{id}") {
                val id = call.parameters["id"]!!.toLong()

                call.respond(HttpStatusCode.OK, repository.loadCategoriesById(id = id))
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: throw IllegalArgumentException("bad id")
                val parameters = call.receive<CreateCategoryRequest>()
                val category = parameters.category ?: "other"

                repository.updateCategory(id = id, category = category)
                call.respond(HttpStatusCode.OK, Result("the bookmark $id has been uploaded"))
            }

            post("/") {
                val parameters = call.receive<CreateCategoryRequest>()
                val id: Long = parameters.id ?: throw IllegalArgumentException("id can't be null")
                val category: String = parameters.category ?: "other"

                repository.createCategory(id, category)
                call.respond(HttpStatusCode.OK, Result("category created"))
            }
        }
        route("/idk") {
            get("/") {
                call.respond(HttpStatusCode.OK, repository.bookmarksByCategory())
            }
        }
    }
}

