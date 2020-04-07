package com.simileoluwaaluko.audiomail.inbox

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simileoluwaaluko.audiomail.R
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.jetbrains.anko.find
import javax.mail.Message

/**
 * Created by The Awesome Simileoluwa Aluko on 2020-04-06.
 */
class InboxRecyclerAdapter(var resource : Array<Message>) : RecyclerView.Adapter<InboxRecyclerAdapter.InboxRecyclerViewHolder>() {

    class InboxRecyclerViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val subject = view.find<TextView>(R.id.subject)
        val from = view.find<TextView>(R.id.from)
        val date = view.find<TextView>(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxRecyclerViewHolder {
        return InboxRecyclerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.inbox_item, parent, false))
    }

    override fun getItemCount(): Int {
        return resource.size
    }

    override fun onBindViewHolder(holder: InboxRecyclerViewHolder, position: Int) {

        CoroutineScope(IO).launch{
            val subject : Deferred<String> = async { resource[position].subject }
            val date : Deferred<String> = async { resource[position].sentDate.toString() }
            val from : Deferred<String> = async { resource[position].from[0].toString() }
            withContext(Main){
                holder.subject.text = subject.await()
                holder.date.text = date.await()
                holder.from.text = from.await()
            }
        }
    }
}