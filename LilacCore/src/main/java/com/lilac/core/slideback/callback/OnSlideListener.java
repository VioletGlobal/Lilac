package com.lilac.core.slideback.callback;

import androidx.annotation.FloatRange;

/**
 * Created by kan212 on 2018/10/18.
 */

public interface OnSlideListener {
    void onSlide(@FloatRange(from = 0.0,
            to = 1.0) float percent);

    void onOpen();

    void onClose();
}
