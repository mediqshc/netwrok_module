package com.homemedics.app.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Rect
import android.view.View
import androidx.core.view.ViewCompat

class RecyclerViewItemDecorator : RecyclerView.ItemDecoration {
    private var spacing: Int = 0
    private var displayMode: Int = 0

    companion object {
        val HORIZONTAL = 0
        val VERTICAL = 1
        val GRID = 2
    }

    var view: View? = null

    constructor(spacing: Int) {
        RecyclerViewItemDecorator(spacing, -1)
    }

    constructor(spacing: Int, displayMode: Int) {
        this.spacing = spacing
        this.displayMode = displayMode
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildViewHolder(view).adapterPosition
        val itemCount = state.itemCount
        val layoutManager = parent.layoutManager
        setSpacingForDirection(outRect, layoutManager, position, itemCount)
    }

    private fun setSpacingForDirection(
        outRect: Rect,
        layoutManager: RecyclerView.LayoutManager?,
        position: Int,
        itemCount: Int
    ) {

        // Resolve display mode automatically
        if (displayMode == -1) {
            displayMode = resolveDisplayMode(layoutManager)
        }

        when (displayMode) {
            HORIZONTAL -> {

                if (view?.let { ViewCompat.getLayoutDirection(it) } == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    outRect.right = if (position == 0) 0 else spacing
                    outRect.left = if (position == itemCount - 1) spacing else 0
                } else {
                    outRect.left = if (position == 0) 0 else spacing
                    outRect.right = if (position == itemCount - 1) spacing else 0
                }

            }
            VERTICAL -> {
                outRect.top = spacing
                outRect.bottom = if (position == itemCount - 1) spacing else 0
            }
            GRID -> if (layoutManager is GridLayoutManager) {
                val gridLayoutManager = layoutManager as GridLayoutManager?
                val cols = gridLayoutManager!!.spanCount
                var rows = itemCount / cols
                if (itemCount % 2 == 1) {
                    rows = rows + 1
                }

                outRect.left = spacing
                outRect.right = if (position % cols == cols - 1) spacing else 0
                outRect.top = spacing
                outRect.bottom = if (position / cols == rows - 1) spacing else 0
            }
        }
    }

    private fun resolveDisplayMode(layoutManager: RecyclerView.LayoutManager?): Int {
        if (layoutManager is GridLayoutManager) return GRID
        return if (layoutManager!!.canScrollHorizontally()) HORIZONTAL else VERTICAL
    }
}