package com.avs.battleship.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ComputerSquareView : SquareView {

    constructor(context: Context) : super(context) { init() }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {init()}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {init()}

    private fun init() {
        setOnTouchListener(OnTouchListener(implementCustomTouchListener()))
    }

    private fun implementCustomTouchListener(): (View, MotionEvent) -> Boolean {
        return { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    viewModel.handlePCAreaClick(event.x, event.y)
                }
            }
            true
        }
    }
}