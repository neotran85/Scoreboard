package com.bidaappscoreboard.common

import android.R
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

object SetImage {

    fun setImage(view: ImageView, url: String?, context: Context) {
        try {
            val sharedOptions: RequestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(R.drawable.ic_menu_report_image)
            val newUrl = create(context, url)
            if (newUrl != null)
                Glide.with(context)
                    .load(url)
                    .apply(sharedOptions)
                    .into(
                        view
                    )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setImage(view: ImageView, url: String, context: Context, progressBar: ProgressBar) {
        try {
            val sharedOptions: RequestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(R.drawable.ic_menu_report_image)
            val newUrl: String? = create(context, url)
            if (newUrl != null)
                Glide.with(context)
                    .load(newUrl)
                    .apply(sharedOptions)
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable?>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.visibility = View.GONE
                            return false
                        }

                    })
                    .into(view)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun create(context: Context, urlImage: String?): String? {
        var newUrlImage = urlImage
        if (newUrlImage == null || newUrlImage.isEmpty()) return null
        val cacheVariable = "?time="
        val prefs =
            context.getSharedPreferences("MY_PREFS_NAME", Context.MODE_PRIVATE)
        val prefsEdit = prefs.edit()
        val url: String = newUrlImage
        if (url.contains(cacheVariable)) {
            val arraySplit =
                newUrlImage.split("\\?time=".toRegex()).toTypedArray()
            prefsEdit.putString(arraySplit[0], arraySplit[1])
            prefsEdit.apply()
        } else {
            val cacheValue = prefs.getString(newUrlImage, null)
            if (cacheValue != null) {
                newUrlImage = newUrlImage + cacheVariable + cacheValue
            }
        }
        return newUrlImage
    }
}