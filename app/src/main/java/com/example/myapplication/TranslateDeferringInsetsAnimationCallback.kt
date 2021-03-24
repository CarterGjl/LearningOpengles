package com.example.myapplication

import android.graphics.Insets
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.R)
class TranslateDeferringInsetsAnimationCallback(
    private val view: View,
    private val persistentInsetTypes: Int,
    private val deferredInsetTypes: Int,
    dispatchMode: Int = DISPATCH_MODE_STOP
) : WindowInsetsAnimation.Callback(dispatchMode) {
    init {
        require(persistentInsetTypes and deferredInsetTypes == 0) {
            "persistentInsetTypes and deferredInsetTypes can not contain any of " +
                    " same WindowInsets.Type values"
        }
    }

    override fun onProgress(
        insets: WindowInsets,
        runningAnimations: List<WindowInsetsAnimation>
    ): WindowInsets {
        // onProgress() is called when any of the running animations progress...

        // First we get the insets which are potentially deferred
        val typesInset = insets.getInsets(deferredInsetTypes)
        // Then we get the persistent inset types which are applied as padding during layout
        val otherInset = insets.getInsets(persistentInsetTypes)

        // Now that we subtract the two insets, to calculate the difference. We also coerce
        // the insets to be >= 0, to make sure we don't use negative insets.
        val diff = Insets.subtract(typesInset, otherInset).let {
            Insets.max(it, Insets.NONE)
        }

        // The resulting `diff` insets contain the values for us to apply as a translation
        // to the view
        view.translationX = (diff.left - diff.right).toFloat()
        view.translationY = (diff.top - diff.bottom).toFloat()

        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimation) {
        // Once the animation has ended, reset the translation values
        view.translationX = 0f
        view.translationY = 0f
    }
}