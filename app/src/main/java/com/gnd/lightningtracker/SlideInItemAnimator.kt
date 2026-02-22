package com.gnd.lightningtracker

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class SlideInItemAnimator : DefaultItemAnimator() {

    init {
        moveDuration = 185

        addDuration = 302


        removeDuration = 100
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {

        val width = if (holder.itemView.width > 0) holder.itemView.width.toFloat() else 1000f

        holder.itemView.translationX = -width

        holder.itemView.animate()
            .translationX(0f)

            .setDuration(addDuration)
            .setInterpolator(DecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {

                    dispatchAddStarting(holder)
                }

                override fun onAnimationEnd(animation: Animator) {

                    dispatchAddFinished(holder)
                }
            })
            .start()


        return false
    }
}