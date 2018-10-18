package com.lilac.core.slideback;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.Stack;

/**
 * Created by kan212 on 2018/10/17.
 * 利用ActivityLifecycleCallbacks建立activity堆栈
 */

public class ActivityHelper implements Application.ActivityLifecycleCallbacks {

    private static Stack<Activity> mActivityStack;

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (null == mActivityStack) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.push(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mActivityStack.remove(activity);
    }

    public Activity getPreActivity() {
        if (null == mActivityStack) {
            return null;
        }
        int size = mActivityStack.size();
        if (size < 2) {
            return null;
        }
        return mActivityStack.elementAt(size - 2);
    }


    /**
     * 强制删掉activity，用于用户快速滑动页面的时候，因为页面还没来得及destroy导致的问题
     */
    void postRemoveActivity(Activity activity) {
        if (mActivityStack != null) {
            mActivityStack.remove(activity);
        }
    }
}
