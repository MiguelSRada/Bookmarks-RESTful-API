package com.example.categories

import com.example.CreateCategoryRequest
import com.example.Result
import com.example.bookmarks.BookmarkService
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.dsl.module
import org.koin.ktor.ext.inject

val categoriesDependencies = module{
    single{ CategoryService(get(),get(),get()) }
}
fun Routing.categoriesRoutes() {
    val categoryService by inject<CategoryService>()
    route("/categories") {
        get("/") {
            call.respond(
                HttpStatusCode.OK,
                categoryService.findCategories(call.request.queryParameters["searchText"])
            )
        }

        get("/{id}") {
            val id = call.parameters["id"]!!.toLong()

            call.respond(HttpStatusCode.OK, categoryService.loadCategoriesById(id))
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: throw IllegalArgumentException("bad id")
            val parameters = call.receive<CreateCategoryRequest>()
            val categoryName = parameters.categoryName ?: throw IllegalArgumentException("there isn't category")

            categoryService.updateCategory(id, categoryName)
            call.respond(HttpStatusCode.OK, Result("the bookmark $id has been uploaded"))
        }

        delete("/{categoryName}") {
            val categoryName = call.parameters["categoryName"] ?: throw  IllegalArgumentException("category not found")

            categoryService.deleteCategory(categoryName)
            call.respond(HttpStatusCode.OK, Result("the category $categoryName has been deleted"))
        }

        post("/") {
            val parameter = call.receive<CreateCategoryRequest>()
            val categoryName = parameter.categoryName ?: throw IllegalArgumentException("there is not category")

            categoryService.createCategory(categoryName)
            call.respond(HttpStatusCode.OK, Result("create the bookmark"))
        }
    }
}