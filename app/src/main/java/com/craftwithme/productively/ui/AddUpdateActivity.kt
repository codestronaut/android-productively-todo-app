package com.craftwithme.productively.ui

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.craftwithme.productively.R
import com.craftwithme.productively.database.DatabaseContract
import com.craftwithme.productively.database.DatabaseContract.TodoColumns.Companion.DATE
import com.craftwithme.productively.database.TodoHelper
import com.craftwithme.productively.model.Todo
import kotlinx.android.synthetic.main.activity_add_update.*
import java.text.SimpleDateFormat
import java.util.*

class AddUpdateActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var todoHelper: TodoHelper
    private var isEdit = false
    private var todo: Todo? = null
    private var position: Int = 0

    companion object {
        const val EXTRA_TODO = "extra_todo"
        const val EXTRA_POSITION = "extra_position"
        const val REQUEST_ADD = 100
        const val RESULT_ADD = 101
        const val REQUEST_UPDATE = 200
        const val RESULT_UPDATE = 201
        const val RESULT_DELETE = 301
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_update)

        todoHelper = TodoHelper.getInstance(applicationContext)
        todoHelper.open()

        todo = intent.getParcelableExtra(EXTRA_TODO)
        if (todo != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            todo = Todo()
        }

        val actionBarTitle: String
        val buttonText: String

        if (isEdit) {
            actionBarTitle = "Edit"
            buttonText = "Update"

            todo?.let {
                edit_title.setText(it.title)
                edit_time.setText(it.time)
            }
        } else {
            actionBarTitle = "Add"
            buttonText = "Save"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        button_save.text = buttonText
        button_save.setOnClickListener(this)
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val date = Date()

        return dateFormat.format(date)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isEdit) {
            menuInflater.inflate(R.menu.menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.button_save) {
            val title = edit_title.text.toString().trim()
            val time = edit_time.text.toString().trim()

            if (title.isEmpty()) {
                edit_title.error = "Field can not be blank"
                return
            }

            if (time.isEmpty()) {
                edit_time.error = "Field can not be blank"
                return
            }

            todo?.title = title
            todo?.time = time

            val intent = Intent()
            intent.putExtra(EXTRA_TODO, todo)
            intent.putExtra(EXTRA_POSITION, position)

            val values = ContentValues()
            values.put(DatabaseContract.TodoColumns.TITLE, title)
            values.put(DatabaseContract.TodoColumns.TIME, time)

            if (isEdit) {
                val result = todoHelper.updateById(todo?.id.toString(), values).toLong()
                if (result > 0) {
                    setResult(RESULT_UPDATE, intent)
                    finish()
                } else {
                    Toast
                        .makeText(this@AddUpdateActivity, "Update failed", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                todo?.date = getCurrentDate()
                values.put(DATE, getCurrentDate())
                val result = todoHelper.insert(values)

                if (result > 0) {
                    todo?.id = result.toInt()
                    setResult(RESULT_ADD, intent)
                    finish()
                } else {
                    Toast
                        .makeText(this@AddUpdateActivity, "Add failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE)
    }

    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String

        if (isDialogClose) {
            dialogTitle = "Cancel"
            dialogMessage = "Are you sure to cancel the changes?"
        } else {
            dialogTitle = "Delete"
            dialogMessage = "Are you sure to delete this item?"
        }

        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle(dialogTitle)
        alertDialogBuilder
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                if (isDialogClose) {
                    finish()
                } else {
                    val result = todoHelper.deleteById(todo?.id.toString()).toLong()
                    if (result > 0) {
                        val intent = Intent()
                        intent.putExtra(EXTRA_POSITION, position)
                        setResult(RESULT_DELETE, intent)
                        finish()
                    } else {
                        Toast.makeText(this@AddUpdateActivity, "Delete failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}