package com.inpows.gradientseekbar

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val gsb = findViewById<GradientSeekBar>(R.id.gradient_seek_bar)
        gsb.setIndicatorTextFormat("Slide to start \${CUSTOM_TEXT}")
        gsb.setThumbDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_circle_thumb_seek_bar))
        gsb.setBackgroundIndicator(R.drawable.square_skeleton, R.color.grey)
        gsb.setIndicatorTextColor(R.color.grey_text)
        gsb.setOnSeekChangeListener(object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {
                when (seekParams?.progress) {
                    0 -> gsb.setIndicatorTextFormat("Not at all \${CUSTOM_TEXT}")
                    in 1..6 -> gsb.setIndicatorTextFormat("Not really \${CUSTOM_TEXT}")
                    in 7..8 -> gsb.setIndicatorTextFormat("Try sliding to the right \${CUSTOM_TEXT}")
                    9 -> gsb.setIndicatorTextFormat("Of course! \${CUSTOM_TEXT}")
                    10 -> gsb.setIndicatorTextFormat("I Love it! \${CUSTOM_TEXT}")
                    else -> gsb.setIndicatorTextFormat("Slide to start \${CUSTOM_TEXT}")
                }
            }

            override fun onStartTrackingTouch(seekBar: GradientSeekBar?) {
                gsb.setBackgroundIndicator(R.drawable.rounded_blue, R.color.blue_50)
                gsb.setIndicatorTextColor(R.color.colorWhite)
                gsb.setThumbDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.active_circle_thumb_seek_bar))
            }
            override fun onStopTrackingTouch(seekBar: GradientSeekBar?) {}
        })
    }

    companion object {
        const val TAG = "GradientSeekBarActivity"
    }
}