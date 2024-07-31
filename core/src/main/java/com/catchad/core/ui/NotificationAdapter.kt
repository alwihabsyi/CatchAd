package com.catchad.core.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.catchad.core.databinding.ItemNotificationBinding
import com.catchad.core.domain.model.Content

class NotificationAdapter: Adapter<NotificationAdapter.NotificationViewHolder>() {
    inner class NotificationViewHolder(private val binding: ItemNotificationBinding): ViewHolder(binding.root) {
        fun bind(content: Content) {
            with(binding) {
                notificationTitle.text = content.title
                notificationBody.text = content.description
                notificationDate.text = content.date
            }
        }
    }

    private val diffUtil = object: DiffUtil.ItemCallback<Content>() {
        override fun areItemsTheSame(oldItem: Content, newItem: Content): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Content, newItem: Content): Boolean =
            oldItem == newItem
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder =
        NotificationViewHolder(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.bind(item)

        holder.itemView.setOnClickListener { onItemClick?.invoke(item) }
    }

    var onItemClick: ((Content) -> Unit)? = null
}