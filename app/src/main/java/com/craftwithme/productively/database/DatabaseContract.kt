package com.craftwithme.productively.database

import android.provider.BaseColumns

internal class DatabaseContract {

    internal class TodoColumns : BaseColumns {

        companion object {
            const val TABLE_NAME = "todo_table"
            const val ID = "id"
            const val TITLE = "title"
            const val DATE = "date"
            const val TIME = "time"
        }

    }
}