package com.juliusscript.example;

import com.juliusscript.rxannotation.annotations.RxClass;
import com.juliusscript.rxannotation.annotations.RxObservable;

/**
 * Created by Julius.
 */
@RxClass
public class ApiTwo {

    @RxObservable
    public String getValue(String noot) {
        return "noot";
    }
}
