# RxAnnotations: Annotations for RxJava
Generating observables & singles using annotations.
 
# Getting started

The first step is to include RxAnnotations into your project, for example, as a Gradle compile dependency:
```
provided 'com.juliusscript.rxannotation:rxannotations:0.0.1'
apt 'com.juliusscript.rxannotation:rxannotations:0.0.1'
//or
annotationProcessor 'com.juliusscript.rxannotation:rxannotations:0.0.1'
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
  <version>0.0.1</version>
  <type>pom</type>
</dependency>
```
<b>Use RxAnnotations to wrap existing class and methods:</b>
```
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

<b>Generated code:</b>
```
public class RxApi extends Api {
  private static RxApi instance = new RxApi();

  private RxApi() {
    super();
  }

  public static RxApi getInstance() {
    return instance;
  }

  public Observable<String> getValueRx(final String whut) {
    return Observable.create(new ObservableOnSubscribe<String>() {
      @Override
      public void subscribe(ObservableEmitter<String> emitter) {
        try {
          java.lang.String result = getValue(whut);
          if (result !=null) {
            emitter.onNext(result);
          }
        }
        catch(Exception ex) {
          emitter.onError(ex);
        }
        finally {
          emitter.onComplete();
        }
      }
    }).subscribeOn(io.reactivex.schedulers.Schedulers.newThread()).observeOn(io.reactivex.schedulers.Schedulers.newThread());
  }

  public Observable<Integer> getIntegerRx() {
    return Observable.defer(new Callable<ObservableSource<Integer>>() {
      @Override
      public ObservableSource<Integer> call() throws Exception {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
          @Override
          public void subscribe(ObservableEmitter<Integer> emitter) {
            try {
              java.lang.Integer result = getInteger();
              if (result !=null) {
                emitter.onNext(result);
              }
            }
            catch(Exception ex) {
              emitter.onError(ex);
            }
            finally {
              emitter.onComplete();
            }
          }
        });
      }
    }).subscribeOn(io.reactivex.schedulers.Schedulers.newThread()).observeOn(io.reactivex.schedulers.Schedulers.newThread());
  }

  public Single<Float> getFloatRx() {
    return Single.create(new SingleOnSubscribe<Float>() {
      @Override
      public void subscribe(SingleEmitter<Float> emitter) {
        try {
          java.lang.Float result = getFloat();
          if (result !=null) {
            emitter.onSuccess(result);
          }
        }
        catch(Exception ex) {
          emitter.onError(ex);
        }
      }
    }).subscribeOn(io.reactivex.schedulers.Schedulers.newThread()).observeOn(io.reactivex.schedulers.Schedulers.newThread());
  }
}
```
 
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
