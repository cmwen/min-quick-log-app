package com.example.minandroidapp.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.minandroidapp.R

abstract class SwipeToDeleteCallback(
    context: Context,
    private val canSwipe: (RecyclerView.ViewHolder) -> Boolean = { true },
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val background = ColorDrawable(ContextCompat.getColor(context, R.color.swipe_delete_background))
    private val icon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_delete)

    override fun isLongPressDragEnabled(): Boolean = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ): Int {
        return if (canSwipe(viewHolder)) {
            super.getMovementFlags(recyclerView, viewHolder)
        } else {
            0
        }
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        val itemView = viewHolder.itemView
        if (dX == 0f) {
            clearCanvas(canvas, itemView.left.toFloat(), itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        val iconWidth = icon?.intrinsicWidth ?: 0
        val iconHeight = icon?.intrinsicHeight ?: 0
        val iconMargin = ((itemView.height - iconHeight) / 2).coerceAtLeast(0)
        val iconTop = itemView.top + iconMargin
        val iconBottom = iconTop + iconHeight

        if (dX > 0) {
            background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
            icon?.setBounds(
                itemView.left + iconMargin,
                iconTop,
                itemView.left + iconMargin + iconWidth,
                iconBottom,
            )
        } else {
            background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
            icon?.setBounds(
                itemView.right - iconMargin - iconWidth,
                iconTop,
                itemView.right - iconMargin,
                iconBottom,
            )
        }

        background.draw(canvas)
        icon?.draw(canvas)

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        ColorDrawable(Color.TRANSPARENT).apply {
            setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
            draw(canvas)
        }
    }
}
