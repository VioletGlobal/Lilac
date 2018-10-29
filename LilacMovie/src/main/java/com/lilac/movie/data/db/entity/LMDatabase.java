package com.lilac.movie.data.db.entity;

import android.app.Application;

import androidx.annotation.WorkerThread;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Created by kan212 on 2018/10/19.
 */

public abstract class LMDatabase extends RoomDatabase {

    private static final String DB_NAME = "lilac_movie.db";

    @WorkerThread
    public static LMDatabase createAsync(Application application) {
        return Room.databaseBuilder(application, LMDatabase.class, DB_NAME).build();
    }
}
