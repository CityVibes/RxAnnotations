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
