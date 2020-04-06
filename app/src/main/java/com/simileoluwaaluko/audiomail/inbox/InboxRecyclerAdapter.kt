package com.simileoluwaaluko.audiomail.inbox

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simileoluwaaluko.audiomail.R
import org.jetbrains.anko.find

/**
 * Created by The Awesome Simileoluwa Aluko on 2020-04-06.
 */
class InboxRecyclerAdapter(var resource : ArrayList<String>) : RecyclerView.Adapter<InboxRecyclerAdapter.InboxRecyclerViewHolder>() {

    class InboxRecyclerViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val subject = view.find<TextView>(R.id.mail_subject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxRecyclerViewHolder {
        return InboxRecyclerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.inbox_item, parent, false))
    }

    override fun getItemCount(): Int {
        Log.d("simi-ira", "here")
        return resource.size
    }

    override fun onBindViewHolder(holder: InboxRecyclerViewHolder, position: Int) {
        holder.subject.text = resource[position]
    }

    fun updateResource(newResource : ArrayList<String>){
        Log.d("simi-ur", newResource.toString())
        resource.clear()
        resource.addAll(newResource)
        this.notifyDataSetChanged()
    }
}