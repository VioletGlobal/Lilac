package com.lilac.core.slideback.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 * Created by kan212 on 2018/10/18.
 */

public class CacheDrawView extends View {

    public View mCacheView;

    public CacheDrawView(Context context) {
        super(context);
    }

    public void drawCacheView(View cacheView) {
        mCacheView = cacheView;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制上个Activity的内容视图
        if (null != mCacheView){
            mCacheView.draw(canvas);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCacheView = null;
    }
}
