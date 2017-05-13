package com.juliusscript.rxannotation;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.inject.Singleton;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Julius.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.juliusscript.rxannotation.RxObservable", "com.juliusscript.rxannotation.RxClass"})
public class RxProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Collection<? extends Element> annotatedSingleElements = roundEnvironment.getElementsAnnotatedWith(RxSingle.class);
        Collection<? extends Element> annotatedObservableElements = roundEnvironment.getElementsAnnotatedWith(RxObservable.class);
        Collection<? extends Element> annotatedClassElements = roundEnvironment.getElementsAnnotatedWith(RxClass.class);
        List<ExecutableElement> observableTypes = ElementFilter.methodsIn(annotatedObservableElements);
        List<ExecutableElement> singleTypes = ElementFilter.methodsIn(annotatedSingleElements);

        for (Element typeElement : annotatedClassElements) {
            if (typeElement.getKind() == ElementKind.CLASS) {
                createRxClass(typeElement, observableTypes, singleTypes);
            }
        }
        return true;
    }

    private TypeSpec buildCallable(TypeName returnClass, Object onSubscribe) {
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

    private void createRxClass(Element typeElement, List<ExecutableElement> observableTypes, List<ExecutableElement> singleTypes) {
        PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();
        String packageName = packageElement.getQualifiedName().toString();
        String className = typeElement.getSimpleName().toString();
        List<MethodSpec> methodSpecs = new ArrayList<MethodSpec>();

        for (ExecutableElement executableElement : observableTypes) {
            methodSpecs.add(createRxObservableMethods(executableElement));
        }
        for (ExecutableElement executableElement : singleTypes) {
            methodSpecs.add(createRxSingleMethods(executableElement));
        }
        //create class
        TypeSpec.Builder rxClassBuilder = TypeSpec.classBuilder("Rx" + className)
                .superclass(ClassName.get(packageName, className))
                .addModifiers(Modifier.PUBLIC);

        if (typeElement.getAnnotation(Singleton.class) != null) {
            rxClassBuilder
                    .addField(createSingleton(packageName, className))
                    .addMethod(createSingletonInstance(packageName, className));
        }
        rxClassBuilder.addMethod(createConstructor(typeElement));
        //add method to new class
        for (MethodSpec methodSpec : methodSpecs) {
            rxClassBuilder.addMethod(methodSpec);
        }

        writeRxClass(packageName, rxClassBuilder);
    }

    private MethodSpec createSingletonInstance(String packageName, String className) {
        return MethodSpec.methodBuilder("getInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(packageName, "Rx" + className))
                .addStatement("return instance")
                .build();
    }

    private FieldSpec createSingleton(String packageName, String className) {
        return FieldSpec.builder(ClassName.get(packageName, "Rx" + className), "instance")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .initializer("$N", "new Rx" + className + "()")
                .build();
    }

    private MethodSpec createConstructor(Element typeElement) {
        Singleton annotation = typeElement.getAnnotation(Singleton.class);
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();

        if (annotation != null) {
            constructorBuilder.addModifiers(Modifier.PRIVATE);
        } else {
            constructorBuilder.addModifiers(Modifier.PUBLIC);
        }
        constructorBuilder.addStatement("$N", "super()");
        return constructorBuilder
                .build();
    }

    private MethodSpec createRxObservableMethods(ExecutableElement executableElement) {
        String methodName = executableElement.getSimpleName().toString();
        TypeName returnClass = ClassName.get(executableElement.getReturnType());
        ClassName observable = ClassName.get(Observable.class);
        TypeName observableReturn = ParameterizedTypeName.get(observable, returnClass);
        RxObservable rxObservable = executableElement.getAnnotation(RxObservable.class);

        //create new reactive method
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName + "Rx")
                .addModifiers(Modifier.PUBLIC)
                .returns(observableReturn);

        List<String> parameters = new ArrayList<String>();
        for (VariableElement variableElement : executableElement.getParameters()) {
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(ClassName
                    .get(variableElement.asType()), variableElement.getSimpleName().toString())
                    .addModifiers(Modifier.FINAL);
            methodBuilder.addParameter(paramBuilder.build());
            parameters.add(variableElement.getSimpleName().toString());
        }
        TypeSpec onSubscribe = buildObservable(executableElement, parameters);

        if (rxObservable.defer()) {
            wrapWithDefer(methodBuilder, buildCallable(returnClass, onSubscribe),
                    getScheduler(rxObservable.subscribeOn()), getScheduler(rxObservable.observeOn()));
        } else {
            methodBuilder.addStatement("return $T.create($L).subscribeOn($N).observeOn($N)",
                    Observable.class, onSubscribe, getScheduler(rxObservable.subscribeOn()),
                    getScheduler(rxObservable.observeOn()));

        }
        return methodBuilder.build();
    }

    private MethodSpec createRxSingleMethods(ExecutableElement executableElement) {
        String methodName = executableElement.getSimpleName().toString();
        TypeName returnClass = ClassName.get(executableElement.getReturnType());
        ClassName single = ClassName.get(Single.class);
        TypeName singleReturn = ParameterizedTypeName.get(single, returnClass);
        RxSingle rxSingle = executableElement.getAnnotation(RxSingle.class);

        //create new reactive method
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName + "Rx")
                .addModifiers(Modifier.PUBLIC)
                .returns(singleReturn);

        List<String> parameters = new ArrayList<String>();
        for (VariableElement variableElement : executableElement.getParameters()) {
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(ClassName
                    .get(variableElement.asType()), variableElement.getSimpleName().toString())
                    .addModifiers(Modifier.FINAL);
            methodBuilder.addParameter(paramBuilder.build());
            parameters.add(variableElement.getSimpleName().toString());
        }
        TypeSpec onSubscribe = buildSingle(executableElement, parameters);

        if (rxSingle.defer()) {
            wrapWithDefer(methodBuilder, buildCallable(returnClass, onSubscribe),
                    getScheduler(rxSingle.subscribeOn()), getScheduler(rxSingle.observeOn()));
        } else {
            methodBuilder.addStatement("return $T.create($L).subscribeOn($N).observeOn($N)",
                    Single.class, onSubscribe, getScheduler(rxSingle.subscribeOn()),
                    getScheduler(rxSingle.observeOn()));

        }
        return methodBuilder.build();
    }

    private TypeSpec buildSingle(ExecutableElement executableElement, List<String> parameters) {
        String methodName = executableElement.getSimpleName().toString();
        TypeName returnClass = ClassName.get(executableElement.getReturnType());

        StringBuilder methodCall = new StringBuilder(methodName + "(");
        for (int i = 0; i < parameters.size(); i++) {
            methodCall.append(parameters.get(i));
            if (i != parameters.size() - 1) {
                methodCall.append(", ");
            }
        }
        methodCall.append(")");

        //anonymous observable creation
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(SingleOnSubscribe.class),
                        returnClass))
                .addMethod(MethodSpec.methodBuilder("subscribe")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterizedTypeName.get(ClassName.get(SingleEmitter.class),
                                returnClass), "emitter")
                        .beginControlFlow("try")
                        .addStatement(returnClass.toString() + " result = " + methodCall.toString())
                        .beginControlFlow("if (result !=null)")
                        .addStatement("emitter.onSuccess(result)")
                        .endControlFlow()
                        .endControlFlow()
                        .beginControlFlow("catch(Exception ex)")
                        .addStatement("emitter.onError(ex)")
                        .endControlFlow()
                        .build())
                .build();
    }

    private TypeSpec buildObservable(ExecutableElement executableElement, List<String> parameters) {
        String methodName = executableElement.getSimpleName().toString();
        TypeName returnClass = ClassName.get(executableElement.getReturnType());

        StringBuilder methodCall = new StringBuilder(methodName + "(");
        for (int i = 0; i < parameters.size(); i++) {
            methodCall.append(parameters.get(i));
            if (i != parameters.size() - 1) {
                methodCall.append(", ");
            }
        }
        methodCall.append(")");

        //anonymous observable creation
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(ObservableOnSubscribe.class),
                        returnClass))
                .addMethod(MethodSpec.methodBuilder("subscribe")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterizedTypeName.get(ClassName.get(ObservableEmitter.class),
                                returnClass), "emitter")
                        .beginControlFlow("try")
                        .addStatement(returnClass.toString() + " result = " + methodCall.toString())
                        .beginControlFlow("if (result !=null)")
                        .addStatement("emitter.onNext(result)")
                        .endControlFlow()
                        .endControlFlow()
                        .beginControlFlow("catch(Exception ex)")
                        .addStatement("emitter.onError(ex)")
                        .endControlFlow()
                        .beginControlFlow("finally")
                        .addStatement("emitter.onComplete()")
                        .endControlFlow()
                        .build())
                .build();
    }

    private String getScheduler(com.juliusscript.rxannotation.Schedulers scheduler) {
        String schedulerClass = Schedulers.class.getName();
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

    private MethodSpec.Builder wrapWithDefer(MethodSpec.Builder builder, TypeSpec observableSource,
                                             String subscribeOn, String observeOn) {
        return builder.addStatement("return $T.defer($L).subscribeOn($N).observeOn($N)", Observable.class,
                observableSource, subscribeOn, observeOn);
    }

    private void writeRxClass(String packageName, TypeSpec.Builder rxClassBuilder) {
        //build and write new class
        TypeSpec rxClass = rxClassBuilder.build();
        JavaFile javaFile = JavaFile.builder(packageName, rxClass).build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}