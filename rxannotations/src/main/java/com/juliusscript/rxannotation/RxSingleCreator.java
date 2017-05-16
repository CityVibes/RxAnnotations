package com.juliusscript.rxannotation;

import com.juliusscript.rxannotation.annotations.RxSingle;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * Created by Julius.
 */

public final class RxSingleCreator {

    private RxSingleCreator() {}

    public static MethodSpec createRxSingleMethods(ExecutableElement executableElement) {
        TypeName returnClass = ClassName.get(executableElement.getReturnType());
        RxSingle rxSingle = executableElement.getAnnotation(RxSingle.class);
        Pair<MethodSpec.Builder, List<String>> methodData = RxMethodCreator.createRxMethods(executableElement, Single.class);

        TypeSpec onSubscribe = buildSingle(executableElement, methodData.second);

        if (rxSingle.defer()) {
            Creator.wrapWithDefer(methodData.first, Creator.buildCallable(returnClass, onSubscribe),
                    Creator.getScheduler(rxSingle.subscribeOn()), Creator.getScheduler(rxSingle.observeOn()));
        } else {
            methodData.first.addStatement("return $T.create($L).subscribeOn($N).observeOn($N)",
                    Single.class, onSubscribe, Creator.getScheduler(rxSingle.subscribeOn()),
                    Creator.getScheduler(rxSingle.observeOn()));

        }
        return methodData.first.build();
    }

    private static TypeSpec buildSingle(ExecutableElement executableElement, List<String> parameters) {
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
}
