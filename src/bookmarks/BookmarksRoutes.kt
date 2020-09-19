package com.example.bookmarks

import com.example.CreateBookmarkRequest
import com.example.Result
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.dsl.module
import org.koin.ktor.ext.inject

val bookmarksDependencies = module{
    single{BookmarkService(get(),get(),get())}
}
fun Routing.bookmarksRoutes() {
    val bookmarkService by inject<BookmarkService>()
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

        get("/categories/{categoryName}") {
            val category = call.parameters["categoryName"] ?: throw IllegalArgumentException("category error")

            call.respond(HttpStatusCode.OK, bookmarkService.bookmarksByCategory(category.replace("_", " ")))
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: throw IllegalArgumentException("bad id")
            val parameters = call.receive<CreateBookmarkRequest>()
            val name = parameters.name
            val url = parameters.url
            val categoryName = parameters.categoryName

            if (name == null || url == null || categoryName == null) throw IllegalArgumentException("there is nothing to update")

            bookmarkService.updateBookmark(id = id, name = name, url = url, categoryName = categoryName)
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
            val categoryName = parameters.categoryName ?: "other"

            bookmarkService.createBookmark(name = name, url = url, categoryName = categoryName)
            call.respond(HttpStatusCode.OK, Result("the bookmark has been created successfully"))
        }
    }
}