package com.juliusscript.rxannotation.annotations;

import com.juliusscript.rxannotation.Schedulers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Julius.
 */
@Target(ElementType.METHOD) // on method level
@Retention(RetentionPolicy.SOURCE) // not needed at runtime
public @interface RxMaybe {

    Schedulers subscribeOn() default Schedulers.NEW_THREAD;

    Schedulers observeOn() default Schedulers.NEW_THREAD;
}
