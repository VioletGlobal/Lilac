package com.lilac.core.slideback.callback;

import com.lilac.core.slideback.widget.SlideBackLayout;

import androidx.annotation.FloatRange;

/**
 * Created by kan212 on 2018/10/18.
 */

public interface OnInternalStateListener {

    void onSlide(@FloatRange(from = 0.0,
            to = 1.0) float percent);

    void onOpen();

    void onClose(Boolean finishActivity);

    void onCheckPreActivity(SlideBackLayout slideBackLayout);
}
