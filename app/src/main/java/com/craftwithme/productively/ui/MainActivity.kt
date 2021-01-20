package com.craftwithme.productively.ui

import android.content.Intent
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.craftwithme.productively.R
import com.craftwithme.productively.database.DatabaseContract
import com.craftwithme.productively.database.TodoHelper
import com.craftwithme.productively.model.Todo
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: TodoAdapter
    private lateinit var todoHelper: TodoHelper

    companion object {
        private const val EXTRA_STATE = "EXTRA_STATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "Todo List"

        todo_recycler_view.layoutManager = LinearLayoutManager(this)
        todo_recycler_view.setHasFixedSize(true)
        adapter = TodoAdapter(this)
        todo_recycler_view.adapter = adapter

        add_todo_fab.setOnClickListener {
            val intent = Intent(this@MainActivity, AddUpdateActivity::class.java)
            startActivityForResult(intent, AddUpdateActivity.REQUEST_ADD)
        }

        todoHelper = TodoHelper.getInstance(applicationContext)
        todoHelper.open()

        if (savedInstanceState == null) {
            loadTodoAsync()
        } else {
            val list = savedInstanceState.getParcelableArrayList<Todo>(EXTRA_STATE)
            if (list != null) {
                adapter.todoList = list
            }
        }
    }

    private fun loadTodoAsync() {
        GlobalScope.launch(Dispatchers.Main) {
            progress_bar.visibility = View.VISIBLE
            val deferredTodo = async(Dispatchers.IO) {
                val cursor = todoHelper.queryAll()
                MappingHelper.mapCursorToArrayList(cursor)
            }
            progress_bar.visibility = View.INVISIBLE
            val todo = deferredTodo.await()
            if (todo.size > 0) {
                adapter.todoList = todo
            } else {
                adapter.todoList = ArrayList()
                showSnackBarMessage("No Data")
            }
        }
    }

    object MappingHelper {
        fun mapCursorToArrayList(todoCursor: Cursor?): ArrayList<Todo> {
            val todoList = ArrayList<Todo>()

            todoCursor?.apply {
                while (moveToNext()) {
                    val id = getInt(getColumnIndexOrThrow(DatabaseContract.TodoColumns.ID))
                    val title = getString(getColumnIndexOrThrow(DatabaseContract.TodoColumns.TITLE))
                    val time = getString(getColumnIndexOrThrow(DatabaseContract.TodoColumns.TIME))
                    val date = getString(getColumnIndexOrThrow(DatabaseContract.TodoColumns.DATE))
                    todoList.add(Todo(id, title, time, date))
                }
            }
            return todoList
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.todoList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            when (requestCode) {
                AddUpdateActivity.REQUEST_ADD -> if (resultCode == AddUpdateActivity.RESULT_ADD) {
                    val todo = data.getParcelableExtra<Todo>(AddUpdateActivity.EXTRA_TODO)

                    if (todo != null) {
                        adapter.addItem(todo)
                    }
                    todo_recycler_view.smoothScrollToPosition(adapter.itemCount - 1)

                    showSnackBarMessage("One Item Added")
                }
                AddUpdateActivity.REQUEST_UPDATE ->
                    when (resultCode) {
                        AddUpdateActivity.RESULT_UPDATE -> {

                            val todo = data.getParcelableExtra<Todo>(AddUpdateActivity.EXTRA_TODO)
                            val position = data.getIntExtra(AddUpdateActivity.EXTRA_POSITION, 0)

                            if (todo != null) {
                                adapter.updateItem(position, todo)
                            }
                            todo_recycler_view.smoothScrollToPosition(position)

                            showSnackBarMessage("One Item Updated")
                        }

                        AddUpdateActivity.RESULT_DELETE -> {
                            val position = data.getIntExtra(AddUpdateActivity.EXTRA_POSITION, 0)

                            adapter.deleteItem(position)

                            showSnackBarMessage("One Item Deleted")
                        }
                    }
            }
        }
    }

    private fun showSnackBarMessage(message: String) {
        Snackbar.make(todo_recycler_view, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        todoHelper.close()
    }
}