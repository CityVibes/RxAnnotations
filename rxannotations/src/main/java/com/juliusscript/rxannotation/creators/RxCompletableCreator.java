package com.juliusscript.rxannotation.creators;

import com.juliusscript.rxannotation.Pair;
import com.juliusscript.rxannotation.annotations.RxCompletable;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;

/**
 * Created by Julius.
 */
public final class RxCompletableCreator {

    private RxCompletableCreator() {
    }

    public static MethodSpec createRxCompletableMethods(ExecutableElement executableElement) {
        RxCompletable rxCompletable = executableElement.getAnnotation(RxCompletable.class);
        Pair<MethodSpec.Builder, List<String>> methodData = RxMethodCreator.createRxMethodsVoid(executableElement, Completable.class);

        TypeSpec onSubscribe = buildCompletable(executableElement, methodData.second);

        methodData.first.addStatement("return $T.create($L).subscribeOn($N).observeOn($N)",
                Completable.class, onSubscribe, Creator.getScheduler(rxCompletable.subscribeOn()),
                Creator.getScheduler(rxCompletable.observeOn()));
        return methodData.first.build();
    }

    private static TypeSpec buildCompletable(ExecutableElement executableElement, List<String> parameters) {
        String methodName = executableElement.getSimpleName().toString();

        StringBuilder methodCall = new StringBuilder(methodName + "(");
        for (int i = 0; i < parameters.size(); i++) {
            methodCall.append(parameters.get(i));
            if (i != parameters.size() - 1) {
                methodCall.append(", ");
            }
        }
        methodCall.append(")");

        //anonymous completable creation
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ClassName.get(CompletableOnSubscribe.class))
                .addMethod(MethodSpec.methodBuilder("subscribe")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.get(CompletableEmitter.class), "emitter")
                        .beginControlFlow("try")
                        .addStatement(methodCall.toString())
                        .addStatement("emitter.onComplete()")
                        .endControlFlow()
                        .beginControlFlow("catch(Exception ex)")
                        .addStatement("emitter.onError(ex)")
                        .endControlFlow()
                        .build())
                .build();
    }
}
