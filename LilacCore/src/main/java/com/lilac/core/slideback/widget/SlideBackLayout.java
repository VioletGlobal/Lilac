package com.lilac.core.slideback.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.lilac.core.slideback.SlideConfig;
import com.lilac.core.slideback.callback.OnInternalStateListener;

import androidx.annotation.NonNull;
import androidx.customview.widget.ViewDragHelper;

/**
 * Created by kan212 on 2018/10/17.
 */

public class SlideBackLayout extends FrameLayout {

    private String mTestName;
    private static final int MIN_FLING_VELOCITY = 400;

    private ViewDragHelper mDragHelper;
    //内容view
    private View mContentView;
    private View mPreContentView;
    private Drawable mPreDecorViewDrawable;
    private OnInternalStateListener mOnInternalStateListener;

    //缓存视图
    private CacheDrawView mCacheDrawView;
    //阴影视图
    private ShadowView mShadowView;

    //屏幕宽度
    private int mScreenWidth;
    //x的速率
    private float mSlideOutVelocity;
    //滑动
    private float mSlideOutRange;
    //滑动距离的比例
    private float mSlideOutRangePercent = 0.4f;
    //是否旋转屏幕
    private boolean mRotateScreen;
    //view 焦点变化的状态值
    private boolean mCloseFlagForWindowFocus;
    //view detach的时候状态值
    private boolean mCloseFlagForDetached;
    //旋转屏幕时候检查activity销毁
    private boolean mCheckPreContentView;
    //允许点击事件
    private boolean mEnableTouchEvent;
    //是否是第一次添加
    private boolean mIsFirstAttachToWindow;
    //按下的x和y值
    private float mDownX;
    private float mDownY;
    //滑动灵敏最小
    private float mSlidDistantX;
    //是不是不允许处理事件
    private boolean mLock = false;
    //边缘判断
    private boolean mEdgeOnly = false;
    //是不是在边缘区间
    private boolean mIsEdgeRangeInside;
    //边缘区间
    private float mEdgeRange;
    //边缘区间
    private float mEdgeRangePercent = 0.1f;


    public SlideBackLayout(Context context) {
        super(context);
    }

    public SlideBackLayout(Context context, View contentView, View preContentView, Drawable preDecorViewDrawable, SlideConfig config, @NonNull OnInternalStateListener onInternalStateListener) {
        super(context);
        mContentView = contentView;
        mPreContentView = preContentView;
        mPreDecorViewDrawable = preDecorViewDrawable;
        mOnInternalStateListener = onInternalStateListener;
        initConfig(config);
        if (preContentView instanceof LinearLayout) {
            mTestName = "1号滑动";
        } else {
            mTestName = "2号滑动";
        }

    }

    private void initConfig(SlideConfig config) {
        if (config == null) {
            config = new SlideConfig.Builder().create();
        }

        mScreenWidth = getResources().getDisplayMetrics().widthPixels;

        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;

        setMotionEventSplittingEnabled(false);
        SlideLeftCallback slideLeftCallback = new SlideLeftCallback();
        //sensitivity越大,对滑动的检测就越敏感,默认传1即可
        mDragHelper = ViewDragHelper.create(this, 1.0f, slideLeftCallback);
        // 最小拖动速度
        mDragHelper.setMinVelocity(minVel);
        //设置允许父View的某个边缘可以用来响应托拽事件,为左侧拉出
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
        //划出的速率
        mSlideOutVelocity = config.getSlideOutVelocity();
        //划出的百分比
        mSlideOutRangePercent = config.getSlideOutPercent();
        //触摸事件是否使用
        mLock = config.isLock();
        //旋转屏幕
        mRotateScreen = config.isRotateScreen();
        //边沿判断
        mEdgeOnly = config.isEdgeOnly();
        mEdgeRangePercent = config.getEdgePercent();
        mEdgeRange = mEdgeRangePercent * mScreenWidth;

        //划出的范围
        mSlideOutRange = mScreenWidth * mSlideOutRangePercent;
        //缓存视图
        mCacheDrawView = new CacheDrawView(getContext());
        mCacheDrawView.setVisibility(INVISIBLE);
        addView(mCacheDrawView);
        //阴影
        mShadowView = new ShadowView(getContext());
        mShadowView.setVisibility(VISIBLE);
        addView(mShadowView, mScreenWidth / 28, LayoutParams.MATCH_PARENT);
        //整体视图
        addView(mContentView);
        //滑动的最小区间
        mSlidDistantX = mScreenWidth / 20.0f;
        //设置系统不需要考虑system bar
        mContentView.setFitsSystemWindows(false);

        if (mRotateScreen) {
            mContentView.findViewById(android.R.id.content).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 屏蔽上个内容页的点击事件
                }
            });
        }
    }

    /**
     * ViewDragHelper的回调
     */
    class SlideLeftCallback extends ViewDragHelper.Callback {

        /**
         * 对触摸view判断，如果需要当前触摸的子View进行拖拽移动就返回true，否则返回false
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mContentView;
        }

        /**
         * 拖拽的子View在所属方向上移动的位置，child为拖拽的子View，left为子view应该到达的x坐标，dx为挪动差值
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return Math.max(Math.min(mScreenWidth, left), 0);
        }

        /**
         * 返回拖拽子View在相应方向上可以被拖动的最远距离，默认为0
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return mScreenWidth;
        }

        /**
         * 当前拖拽的view松手或者ACTION_CANCEL时调用，xvel、yvel为离开屏幕时的速率
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (releasedChild == mContentView) {
                if (xvel > mSlideOutVelocity) {
                    //以松手前的滑动速度为初值，让捕获到的子View自动滚动到指定位置，只能在Callback的onViewReleased()中使用
                    //速率特别快的话直接划出来
                    mDragHelper.settleCapturedViewAt(mScreenWidth, 0);
                    invalidate();
                    return;
                }
                //拖动完成的时候没有到mSlideOutRange值的话，整个页面回到原来的位置
                //超过的话就整体展现出来
                if (mContentView.getLeft() < mSlideOutRange) {
                    mDragHelper.settleCapturedViewAt(0, 0);
                } else {
                    mDragHelper.settleCapturedViewAt(mScreenWidth, 0);
                }
                invalidate();
            }
        }

        /**
         * 当ViewDragHelper状态发生变化时回调（STATE_IDLE,STATE_DRAGGING,STATE_SETTLING）
         */
        @Override
        public void onViewDragStateChanged(int state) {
            switch (state) {
                case ViewDragHelper.STATE_IDLE:
                    if (mContentView.getLeft() == 0) {
                        //回到原处
                        mOnInternalStateListener.onOpen();
                    } else if (mContentView.getLeft() == mScreenWidth) {
                        // 这里再绘制一次是因为在屏幕旋转的模式下，remove了preContentView后布局会重新调整
                        if (mRotateScreen && mCacheDrawView.getVisibility() == INVISIBLE) {
                            mCacheDrawView.setBackground(mPreDecorViewDrawable);
                            mCacheDrawView.drawCacheView(mPreContentView);
                            mCacheDrawView.setVisibility(VISIBLE);

                            mCloseFlagForWindowFocus = true;
                            mCloseFlagForDetached = true;

                            // 这里setTag是因为下面的回调会把它移除出当前页面，这时候会触发它的onDetachedFromWindow事件，
                            // 而它的onDetachedFromWindow实际上是来处理屏幕旋转的，所以设置个tag给它，让它知道是当前界面移除它的，并不是屏幕旋转导致的
                            mPreContentView.setTag("notScreenOrientationChange");
                            mOnInternalStateListener.onClose(true);
                            mPreContentView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mCacheDrawView.setBackground(mPreDecorViewDrawable);
                                    mCacheDrawView.drawCacheView(mPreContentView);
                                }
                            }, 10);
                        } else if (!mRotateScreen) {
                            mCloseFlagForWindowFocus = true;
                            mCloseFlagForDetached = true;
                            mOnInternalStateListener.onClose(true);
                        }
                    }
                    break;
            }
        }

        /**
         * 被拖拽的View位置变化时回调，changedView为位置变化的view，left、top变化后的x、y坐标，dx、dy为新位置与旧位置的偏移量
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (!mRotateScreen && mCacheDrawView.getVisibility() == INVISIBLE) {
                mCacheDrawView.setBackground(mPreDecorViewDrawable);
                mCacheDrawView.drawCacheView(mPreContentView);
                mCacheDrawView.setVisibility(VISIBLE);
            } else if (mRotateScreen) {
                if (!mCheckPreContentView) {
                    // 在旋转屏幕的模式下，这里的检查很有必要，比如一个滑动activity先旋转了屏幕，然后再返回上个滑动activity的时候，由于屏幕旋转上个activity会重建，步骤是：
                    // 上个activity会先新建一个activity，再把之前的销毁，所以新建的activity调SlideBackLayout.attach的时候传的上个activity实际上是要删掉的activity
                    // (因为要删掉的activity的destroy有延时的，还没销毁掉)，这就出错了;
                    // 所以这里还要在当前页面取得焦点的时候回调，去检查下看是不是上个activity改了，改了再重新赋值
                    mCheckPreContentView = true;
                    // 只需要检查一次上个Activity是不是变了
                    mOnInternalStateListener.onCheckPreActivity(SlideBackLayout.this);
                }
                addPreContentView();
            }
            if (mShadowView.getVisibility() != VISIBLE) {
                mShadowView.setVisibility(VISIBLE);
            }
            //变化后的left和屏幕的比例
            float percent = left * 1.0f / mScreenWidth;
            mOnInternalStateListener.onSlide(percent);
            if (mRotateScreen) {
                //旋转之后滑动上个页面
                mPreContentView.setX(-mScreenWidth / 2 + percent * (mScreenWidth / 2));
            } else {
                //滑动缓存页面
                mCacheDrawView.setX(-mScreenWidth / 2 + percent * (mScreenWidth / 2));
            }
            //绘制阴影
            mShadowView.setX(mContentView.getX() - mShadowView.getWidth());
            mShadowView.redraw(1 - percent);
        }

        /**
         * 拖拽的子View在所属方向上移动的位置，child为拖拽的子View，top为子view应该到达的y坐标,dy为弄懂差值
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return super.clampViewPositionVertical(child, top, dy);
        }
    }

    /**
     * 先remove在添加
     */
    private void addPreContentView() {
        if (mPreContentView.getParent() != SlideBackLayout.this) {
            mPreContentView.setTag("notScreenOrientationChange");
            ((ViewGroup) mPreContentView.getParent()).removeView(mPreContentView);
            SlideBackLayout.this.addView(mPreContentView);
            mShadowView.setVisibility(VISIBLE);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // 优化侧滑的逻辑，不要一有稍微的滑动就被ViewDragHelper拦截掉了
                if (Math.abs(ev.getY() - mDownY) > mSlidDistantX) {
                    return false;
                }
                if (ev.getX() - mDownX < mSlidDistantX) {
                    return false;
                }
                break;
        }
        if (mLock) {
            return false;
        }
        if (mEdgeOnly) {
            float x = ev.getX();
            mIsEdgeRangeInside = isEdgeRangeInside(x);
            return mIsEdgeRangeInside && mDragHelper.shouldInterceptTouchEvent(ev);
        } else {
            return mDragHelper.shouldInterceptTouchEvent(ev);
        }
    }

    private boolean isEdgeRangeInside(float x) {
        return x <= mEdgeRange;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLock) {
            return super.onTouchEvent(event);
        }
        if (!mEdgeOnly || mIsEdgeRangeInside) {
            if (!mEnableTouchEvent) {
                return super.onTouchEvent(event);
            }
            if (mCloseFlagForDetached || mCloseFlagForWindowFocus) {
                // 针对快速滑动的时候，页面关闭的时候移除上个页面的时候，布局重新调整，这时候我们把contentView设为invisible，
                // 但是还是可以响应DragHelper的处理，所以这里根据页面关闭的标志位不给处理事件了
                return super.onTouchEvent(event);
            }
            mDragHelper.processTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
        return true;
    }

    /**
     * 在调用settleCapturedViewAt()、flingCapturedView()和smoothSlideViewTo()时，该方法返回true，一般重写父view的computeScroll方法，进行该方法判断
     */
    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            //获得焦点的时候可以处理touch
            mEnableTouchEvent = true;
            if (!mIsFirstAttachToWindow) {
                mIsFirstAttachToWindow = true;
            }
        } else {
            if (mRotateScreen) {
                if (mCloseFlagForWindowFocus) {
                    mCloseFlagForWindowFocus = false;
                } else {
                    //跳转到另外一个Activity，例如也是需要滑动的，这时候就需要取当前Activity的contentView，所以这里把preContentView给回上个Activity
                    mOnInternalStateListener.onClose(false);
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mEnableTouchEvent = false;
        if (mRotateScreen) {
            // 1.旋转屏幕的时候必调此方法，这里掉onClose目的是把preContentView给回上个Activity
            if (mCloseFlagForDetached) {
                mCloseFlagForDetached = false;
            } else {
                //手动删除，不是旋转屏幕，不做处理
                if (getTag() != null && getTag().equals("notScreenOrientationChange")) {
                    setTag(null);
                } else {
                    //屏幕旋转了，重建界面: 把preContentView给回上个Activity
                    mOnInternalStateListener.onClose(false);
                }
            }
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //旋转屏幕时候修改阴影
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams layoutParams = mShadowView.getLayoutParams();
        layoutParams.width = mScreenWidth / 28;
        layoutParams.height = LayoutParams.MATCH_PARENT;
    }

    public void setSlideOutRangePercent(float slideOutRangePercent) {
        mSlideOutRangePercent = slideOutRangePercent;
        mSlideOutRange = mScreenWidth * mSlideOutRangePercent;
    }

    public float getSlideOutRangePercent() {
        return mSlideOutRangePercent;
    }

    public void updatePreContentView(View contentView) {
        mPreContentView = contentView;
        mCacheDrawView.drawCacheView(mPreContentView);
    }
}
