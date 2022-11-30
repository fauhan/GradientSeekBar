package com.inpows.gradientseekbar

interface OnSeekChangeListener {

    fun onSeeking(seekParams: SeekParams?)

    fun onStartTrackingTouch(seekBar: GradientSeekBar?)

    fun onStopTrackingTouch(seekBar: GradientSeekBar?)
}