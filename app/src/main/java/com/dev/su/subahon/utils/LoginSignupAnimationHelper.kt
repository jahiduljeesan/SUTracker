package com.dev.su.subahon.utils


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

object LoginSignupAnimationHelper {

    fun showAnimation(view: LottieAnimationView) {
        view.apply {
            visibility = View.VISIBLE
            setAnimation("loading.json")
            elevation = 100f
            repeatCount = LottieDrawable.INFINITE
            playAnimation()
        }
    }

    fun hideAnimation(view: LottieAnimationView){
        view.apply {
            cancelAnimation()
            visibility = View.GONE
        }
    }

    fun showSuccessAnimation(view: LottieAnimationView, onComplete: () -> Unit) {
        view.apply {
            cancelAnimation()
            setAnimation("loading_success.json")
            visibility = View.VISIBLE
            bringToFront()
            elevation = 100f
            repeatCount = 0
            playAnimation()

            addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                    super.onAnimationEnd(animation, isReverse)
                    visibility = View.GONE
                    removeAnimatorListener(this)
                    onComplete()
                }
            })
        }
    }

}