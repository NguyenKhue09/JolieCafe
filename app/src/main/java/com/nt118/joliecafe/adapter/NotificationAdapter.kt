package com.nt118.joliecafe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ItemRowLayoutBinding
import com.nt118.joliecafe.databinding.ItemRvNotificationBinding
import com.nt118.joliecafe.models.Notification
import com.nt118.joliecafe.ui.activities.notifications.NotificationActivity
import com.nt118.joliecafe.util.Constants.Companion.LOCAL_TIME_FORMAT
import com.nt118.joliecafe.util.extenstions.formatTo
import com.nt118.joliecafe.util.extenstions.toDate

class NotificationAdapter(
    diffUtil: DiffUtil.ItemCallback<Notification>,
) : PagingDataAdapter<Notification, NotificationAdapter.MyViewHolder>(diffCallback = diffUtil) {

    class MyViewHolder(var binding: ItemRvNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRvNotificationBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val notification = getItem(position)

        notification?.let {
            holder.binding.tvNotificationTitle.text = notification.title
            holder.binding.tvNotificationBody.text = notification.message
            if (!notification.image.isNullOrEmpty()) {
                holder.binding.ivNotificationImage.visibility = View.VISIBLE
                holder.binding.ivNotificationImage.load(
                    notification.image
                ) {
                    crossfade(300)
                    error(R.drawable.image_logo)
                    placeholder(R.drawable.image_logo)
                }

            } else {
                holder.binding.ivNotificationImage.visibility = View.GONE
            }
            holder.binding.tvTime.text =
                notification.createdAt.toDate()?.formatTo(LOCAL_TIME_FORMAT)
        }
    }

}