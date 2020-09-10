package com.example.commons

import com.example.bookmarks.Bookmark
import com.example.categories.Category
import io.mockk.every
import io.mockk.mockkStatic
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

fun mockDbTransactionCategories(dbMock:Database, dbTransactionMock: Transaction){
    mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    every { transaction(dbMock, any<Transaction.() -> List<Category>>()) } answers {
        val execFunction: Transaction.() -> List<Category> = secondArg()
        dbTransactionMock.execFunction()
    }
}

fun mockDbTransactionBookmark(dbMock:Database, dbTransactionMock: Transaction){
    mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    every { transaction(dbMock, any<Transaction.() -> List<Bookmark>>()) } answers {
        val execFunction: Transaction.() -> List<Bookmark> = secondArg()
        dbTransactionMock.execFunction()
    }
}

fun mockDbTransactionInt(dbMock:Database, dbTransactionMock: Transaction){
    mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    every { transaction(dbMock, any<Transaction.() -> Int>()) } answers {
        val execFunction: Transaction.() -> Int = secondArg()
        dbTransactionMock.execFunction()
    }
}
fun mockDbTransactionUnit(dbMock:Database, dbTransactionMock: Transaction){
    mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    every { transaction(dbMock, any<Transaction.() -> Unit>()) } answers {
        val execFunction: Transaction.() -> Unit = secondArg()
        dbTransactionMock.execFunction()
    }
}

fun mockDbTransactionLong(dbMock:Database, dbTransactionMock: Transaction){
    mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    every { transaction(dbMock, any<Transaction.() -> Long>()) } answers {
        val execFunction: Transaction.() -> Long = secondArg()
        dbTransactionMock.execFunction()
    }
}