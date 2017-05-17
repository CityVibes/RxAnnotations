package com.juliusscript.example;

import com.juliusscript.rxannotation.annotations.RxClass;
import com.juliusscript.rxannotation.annotations.RxCompletable;
import com.juliusscript.rxannotation.annotations.RxMaybe;
import com.juliusscript.rxannotation.annotations.RxObservable;
import com.juliusscript.rxannotation.annotations.RxSingle;
import com.juliusscript.rxannotation.annotations.RxFlowable;

import javax.inject.Singleton;

import io.reactivex.BackpressureStrategy;

/**
 * Created by Julius.
 */
@Singleton
@RxClass
public class Api {
    private boolean completed;

    @RxObservable
    public String getValue(String whut) {
        return null;
    }


    @RxObservable(defer = true)
    public Integer getInteger() {
        return 55;
    }

    @RxSingle
    public Float getFloat() {
        return 0.5f;
    }

    @RxFlowable(backpressure = BackpressureStrategy.ERROR)
    public Object getObject() {
        return new Object();
    }

    @RxMaybe
    public Integer maybe() {
        if (BuildConfig.DEBUG) {
            return 1;
        } else {
            return 0;
        }
    }

    @RxCompletable
    public void setCompleted() {
        completed = true;
    }
}
