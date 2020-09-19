package com.example

import com.example.bookmarks.BookmarkService
import com.example.bookmarks.BookmarksRepository
import com.example.bookmarks.bookmarksDependencies
import com.example.bookmarks.bookmarksRoutes
import com.example.categories.CategoriesRepository
import com.example.categories.CategoryService
import com.example.categories.categoriesDependencies
import com.example.categories.categoriesRoutes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import com.fasterxml.jackson.databind.*
import io.ktor.jackson.*
import io.ktor.features.*
import org.jetbrains.exposed.sql.*
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.koin.ktor.ext.modules

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

data class Error(val error: String)
data class Result(val result: String)
data class CreateBookmarkRequest(var name: String? = null, var url: String? = null, var categoryName: String? = null)
data class CreateCategoryRequest(var id: Long? = null, var categoryName: String? = null)

fun Application.config(testing: Boolean = true) {

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
    startKoin {
        printLogger()
        modules(module {
            single { database() }
            single { BookmarksRepository() }
            single { CategoriesRepository() }
        })
    }

    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, Result("bookmark application"))
        }
    }
}

private fun database(): Database {
    return Database.connect(
        url = "jdbc:mysql://localhost:3306/bookmarks1?serverTimezone=UTC",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = "@Bestyear2020"
    )
}

fun Application.bookmarksModule(){
    loadKoinModules(bookmarksDependencies)
    routing{
        bookmarksRoutes()
    }
}

fun Application.categoriesModule(){
    loadKoinModules(categoriesDependencies)
    routing{
        categoriesRoutes()
    }
}
