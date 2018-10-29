package com.lilac.movie;

import android.app.Application;

import com.lilac.movie.data.db.entity.LMDatabase;
import com.lilac.movie.rx.RxSchedulers;

import io.reactivex.Observable;

/**
 * Created by kan212 on 2018/10/19.
 * 一个第三方的movie的学习工程(新技术)
 */

public class LMApplication extends Application{

    public static LMApplication lmApplication;
    private LMDatabase mDatabase;


    @Override
    public void onCreate() {
        super.onCreate();
        lmApplication = this;
        Observable.just("")
                .map(s ->{
                    mDatabase = LMDatabase.createAsync(lmApplication);
                    return true;
                }).compose(RxSchedulers.apply());

    }
}
