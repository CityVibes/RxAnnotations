package com.juliusscript.example;

import com.juliusscript.rxannotation.RxClass;
import com.juliusscript.rxannotation.RxObservable;
import com.juliusscript.rxannotation.RxSingle;
import com.juliusscript.rxannotation.RxFlowable;

import javax.inject.Singleton;

import io.reactivex.BackpressureStrategy;

/**
 * Created by Julius.
 */
@Singleton
@RxClass
public class Api {

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
}
