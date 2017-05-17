package com.juliusscript.rxannotation;

import com.google.auto.service.AutoService;
import com.juliusscript.rxannotation.annotations.RxClass;
import com.juliusscript.rxannotation.annotations.RxCompletable;
import com.juliusscript.rxannotation.annotations.RxFlowable;
import com.juliusscript.rxannotation.annotations.RxMaybe;
import com.juliusscript.rxannotation.annotations.RxObservable;
import com.juliusscript.rxannotation.annotations.RxSingle;
import com.juliusscript.rxannotation.creators.RxCompletableCreator;
import com.juliusscript.rxannotation.creators.RxFlowableCreator;
import com.juliusscript.rxannotation.creators.RxMaybeCreator;
import com.juliusscript.rxannotation.creators.RxObservableCreator;
import com.juliusscript.rxannotation.creators.RxSingleCreator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
import javax.lang.model.util.ElementFilter;

/**
 * Created by Julius.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.juliusscript.rxannotation.annotations.RxObservable", "com.juliusscript.rxannotation.annotations.RxSingle",
        "com.juliusscript.rxannotation.annotations.RxFlowable", "com.juliusscript.rxannotation.annotations.RxClass",
        "com.juliusscript.rxannotation.annotations.RxMaybe", "com.juliusscript.rxannotation.annotations.RxCompletable"})
public class RxProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Collection<? extends Element> annotatedClassElements = roundEnvironment.getElementsAnnotatedWith(RxClass.class);

        for (Element typeElement : annotatedClassElements) {
            if (typeElement.getKind() == ElementKind.CLASS) {
                createRxClass(typeElement, getMethodSpecs(roundEnvironment));
            }
        }
        return true;
    }

    private List<MethodSpec> getMethodSpecs(RoundEnvironment roundEnvironment) {
        Collection<? extends Element> annotatedSingleElements = roundEnvironment.getElementsAnnotatedWith(RxSingle.class);
        Collection<? extends Element> annotatedFlowableElements = roundEnvironment.getElementsAnnotatedWith(RxFlowable.class);
        Collection<? extends Element> annotatedObservableElements = roundEnvironment.getElementsAnnotatedWith(RxObservable.class);
        Collection<? extends Element> annotatedMaybeElements = roundEnvironment.getElementsAnnotatedWith(RxMaybe.class);
        Collection<? extends Element> annotatedCompletableElements = roundEnvironment.getElementsAnnotatedWith(RxCompletable.class);
        List<ExecutableElement> observableTypes = ElementFilter.methodsIn(annotatedObservableElements);
        List<ExecutableElement> singleTypes = ElementFilter.methodsIn(annotatedSingleElements);
        List<ExecutableElement> flowableTypes = ElementFilter.methodsIn(annotatedFlowableElements);
        List<ExecutableElement> maybeTypes = ElementFilter.methodsIn(annotatedMaybeElements);
        List<ExecutableElement> completableTypes = ElementFilter.methodsIn(annotatedCompletableElements);
        List<MethodSpec> methodSpecs = new ArrayList<MethodSpec>();

        for (ExecutableElement executableElement : observableTypes) {
            methodSpecs.add(RxObservableCreator.createRxObservableMethods(executableElement));
        }
        for (ExecutableElement executableElement : singleTypes) {
            methodSpecs.add(RxSingleCreator.createRxSingleMethods(executableElement));
        }
        for (ExecutableElement executableElement : flowableTypes) {
            methodSpecs.add(RxFlowableCreator.createRxFlowableMethods(executableElement));
        }
        for (ExecutableElement executableElement : maybeTypes) {
            methodSpecs.add(RxMaybeCreator.createRxMaybeMethods(executableElement));
        }
        for (ExecutableElement executableElement : completableTypes) {
            methodSpecs.add(RxCompletableCreator.createRxCompletableMethods(executableElement));
        }
        return methodSpecs;
    }

    private void createRxClass(Element typeElement, List<MethodSpec> methodSpecs) {
        PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();
        String packageName = packageElement.getQualifiedName().toString();
        String className = typeElement.getSimpleName().toString();

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