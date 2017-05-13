package com.juliusscript.rxannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Julius.
 */
@Target(ElementType.TYPE) // on class level
@Retention(RetentionPolicy.SOURCE) // not needed at runtime
public @interface RxClass {

}