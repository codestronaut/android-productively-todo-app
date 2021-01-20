package com.craftwithme.productively.ui

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.craftwithme.productively.R
import com.craftwithme.productively.model.Todo
import kotlinx.android.synthetic.main.todo_item_view.view.*

class TodoAdapter(private val activity: Activity) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    var todoList = ArrayList<Todo>()
        set(todoList) {
            if (todoList.size > 0) {
                this.todoList.clear()
            }
            this.todoList.addAll(todoList)
            notifyDataSetChanged()
        }

    fun addItem(todo: Todo) {
        this.todoList.add(todo)
        notifyItemInserted(this.todoList.size)
    }

    fun updateItem(position: Int, todo: Todo) {
        this.todoList[position] = todo
        notifyItemChanged(position, todo)
    }

    fun deleteItem(position: Int) {
        this.todoList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.todoList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoAdapter.TodoViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.todo_item_view, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoAdapter.TodoViewHolder, position: Int) {
        holder.bind(todoList[position])
    }

    override fun getItemCount(): Int {
        return this.todoList.size
    }

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(todo: Todo) {
            with(itemView) {
                todo_title_text_view.text = todo.title
                todo_date_text_view.text = todo.date
                todo_time_text_view.text = todo.time
                todo_card_view.setOnClickListener(
                    CustomOnItemClickListener(
                        adapterPosition,
                        object : CustomOnItemClickListener.OnItemClickCallback {
                            override fun onItemClicked(view: View, position: Int) {
                                val intent = Intent(activity, AddUpdateActivity::class.java)
                                intent.putExtra(AddUpdateActivity.EXTRA_POSITION, position)
                                intent.putExtra(AddUpdateActivity.EXTRA_TODO, todo)
                                activity.startActivityForResult(
                                    intent,
                                    AddUpdateActivity.REQUEST_UPDATE
                                )
                            }
                        })
                )
            }
        }
    }
}