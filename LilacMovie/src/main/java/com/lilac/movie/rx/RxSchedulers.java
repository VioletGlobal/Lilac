package com.lilac.movie.rx;

import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by kan212 on 2018/10/19.
 */

public class RxSchedulers {

    public static <T> ObservableTransformer<T, T> apply() {
        return upstream ->
                upstream.subscribeOn(Schedulers.io())
                .observeOn(Android)
    }
}
