package com.juliusscript.rxannotation;

import com.juliusscript.rxannotation.annotations.RxFlowable;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

/**
 * Created by Julius.
 */

public final class RxFlowableCreator {

    private RxFlowableCreator() {
    }

    public static MethodSpec createRxFlowableMethods(ExecutableElement executableElement) {
        RxFlowable rxFlowable = executableElement.getAnnotation(RxFlowable.class);
        Pair<MethodSpec.Builder, List<String>> methodData = RxMethodCreator.createRxMethods(executableElement, Flowable.class);
        TypeSpec onSubscribe = buildFlowable(executableElement, methodData.second);

        methodData.first.addStatement("return $T.create($L, $T.$L).subscribeOn($N).observeOn($N)",
                Flowable.class, onSubscribe, BackpressureStrategy.class, rxFlowable.backpressure(), Creator.getScheduler(rxFlowable.subscribeOn()),
                Creator.getScheduler(rxFlowable.observeOn()));
        return methodData.first.build();
    }

    private static TypeSpec buildFlowable(ExecutableElement executableElement, List<String> parameters) {
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

        //anonymous flowable creation
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(FlowableOnSubscribe.class),
                        returnClass))
                .addMethod(MethodSpec.methodBuilder("subscribe")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterizedTypeName.get(ClassName.get(FlowableEmitter.class),
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
}
