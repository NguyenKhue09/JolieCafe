package com.nt118.joliecafe.util

import androidx.recyclerview.widget.DiffUtil
import com.nt118.joliecafe.models.Comment

object CommentComparator : DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }
}