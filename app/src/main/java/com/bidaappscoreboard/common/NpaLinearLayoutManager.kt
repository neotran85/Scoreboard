package com.bidaappscoreboard.common

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager

class NpaLinearLayoutManager : LinearLayoutManager {
    /**
     * Disable predictive animations. There is a bug in RecyclerView which causes views that
     * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
     * adapter size has decreased since the ViewHolder was recycled.
     */
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context?) : super(context)

    constructor(
        context: Context?,
        orientation: Int,
        reverseLayout: Boolean
    ) : super(context, orientation, reverseLayout)
}