package jm.droid.sample.hvtablayout

import android.animation.TimeInterpolator

class BBB:TimeInterpolator {
    override fun getInterpolation(input: Float): Float {
        return input
    }
}
