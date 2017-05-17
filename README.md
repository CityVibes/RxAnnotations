# RxAnnotations: Annotations for RxJava
Generating reactiveX methods using annotations.
 
# Getting started

The first step is to include RxAnnotations into your project, for example, as a Gradle compile dependency:
```
provided 'com.juliusscript.rxannotation:rxannotations:0.0.2'
apt 'com.juliusscript.rxannotation:rxannotations:0.0.2'
//or
annotationProcessor 'com.juliusscript.rxannotation:rxannotations:0.0.2'
```

If you haven't yet include RxJava and/or RxAndroid:
```
compile 'io.reactivex.rxjava2:rxandroid:2.0.1'
compile 'io.reactivex.rxjava2:rxjava:2.1.0'
```

As Maven dependency:
```
<dependency>
  <groupId>com.juliusscript.rxannotation</groupId>
  <artifactId>rxannotations</artifactId>
  <version>0.0.2</version>
  <type>pom</type>
</dependency>
```
<b>Use RxAnnotations to wrap existing class and methods:</b>
```
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
        //could be network request or anything that shouldn't run on main thread
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
```

`@Singleton` Creates, well...singleton of annotated class.

In order to produce Observables and Singles class has to be annotated as `@RxClass`.

Code generated from above annotation could be referenced in old way and as observables or singles.

<b>Using method from generated class:</b>
```
 RxApi.getInstance().getIntegerRx()
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        Toast.makeText(MainActivity.this, "Integer value is: " + integer, Toast.LENGTH_SHORT).show();
                    }
                });
```
or non-reactiveX way
`RxApi.getInstance().getInteger();`
 
## LICENSE

Copyright (c) 2017-present, RxAnnotations Contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
