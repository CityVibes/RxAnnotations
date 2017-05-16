package com.juliusscript.rxannotation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.concurrent.Callable;

import javax.lang.model.element.Modifier;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;

/**
 * Created by Julius.
 */

public class Creator {

    public static String getScheduler(com.juliusscript.rxannotation.Schedulers scheduler) {
        String schedulerClass = io.reactivex.schedulers.Schedulers.class.getName();
        switch (scheduler) {
            case IO:
                return schedulerClass + ".io()";
            case COMPUTATION:
                return schedulerClass + ".computation()";
            case NEW_THREAD:
                return schedulerClass + ".newThread()";
            case SINGLE:
                return schedulerClass + ".single()";
            case TRAMPOLINE:
                return schedulerClass + ".trampoline()";
            case ANDROID_MAIN:
                return "io.reactivex.android.schedulers.AndroidSchedulers.mainThread()";
        }
        return schedulerClass + ".newThread()";
    }

    public static MethodSpec.Builder wrapWithDefer(MethodSpec.Builder builder, TypeSpec observableSource,
                                             String subscribeOn, String observeOn) {
        return builder.addStatement("return $T.defer($L).subscribeOn($N).observeOn($N)", Observable.class,
                observableSource, subscribeOn, observeOn);
    }

    public static TypeSpec buildCallable(TypeName returnClass, Object onSubscribe) {
        //anonymous observable creation
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Callable.class),
                        ParameterizedTypeName.get(ClassName.get(ObservableSource.class), returnClass)))
                .addMethod(MethodSpec.methodBuilder("call")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addException(Exception.class)
                        .returns(ParameterizedTypeName.get(ClassName.get(ObservableSource.class), returnClass))
                        .addStatement("return $T.create($L)", Observable.class,
                                onSubscribe)
                        .build())
                .build();
    }
}
