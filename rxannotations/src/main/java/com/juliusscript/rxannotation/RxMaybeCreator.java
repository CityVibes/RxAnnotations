package com.juliusscript.rxannotation;

import com.juliusscript.rxannotation.annotations.RxMaybe;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;

/**
 * Created by Julius.
 */

public final class RxMaybeCreator {

    private RxMaybeCreator() {
    }

    public static MethodSpec createRxMaybeMethods(ExecutableElement executableElement) {
        RxMaybe rxMaybe = executableElement.getAnnotation(RxMaybe.class);
        Pair<MethodSpec.Builder, List<String>> methodData = RxMethodCreator.createRxMethods(executableElement, Maybe.class);
        TypeSpec onSubscribe = buildMaybe(executableElement, methodData.second);

        methodData.first.addStatement("return $T.create($L).subscribeOn($N).observeOn($N)",
                Maybe.class, onSubscribe, Creator.getScheduler(rxMaybe.subscribeOn()),
                Creator.getScheduler(rxMaybe.observeOn()));
        return methodData.first.build();
    }

    private static TypeSpec buildMaybe(ExecutableElement executableElement, List<String> parameters) {
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

        //anonymous maybe creation
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(MaybeOnSubscribe.class),
                        returnClass))
                .addMethod(MethodSpec.methodBuilder("subscribe")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterizedTypeName.get(ClassName.get(MaybeEmitter.class),
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
                        .beginControlFlow("finally")
                        .addStatement("emitter.onComplete()")
                        .endControlFlow()
                        .build())
                .build();
    }
}
