package com.juliusscript.rxannotation;

import com.juliusscript.rxannotation.annotations.RxObservable;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by Julius.
 */
public final class RxObservableCreator {

    public RxObservableCreator() {
    }

    public static MethodSpec createRxObservableMethods(ExecutableElement executableElement) {
        TypeName returnClass = ClassName.get(executableElement.getReturnType());
        RxObservable rxObservable = executableElement.getAnnotation(RxObservable.class);
        Pair<MethodSpec.Builder, List<String>> methodData = RxMethodCreator.createRxMethods(executableElement, Observable.class);
        TypeSpec onSubscribe = buildObservable(executableElement, methodData.second);

        if (rxObservable.defer()) {
            Creator.wrapWithDefer(methodData.first, Creator.buildCallable(returnClass, onSubscribe),
                    Creator.getScheduler(rxObservable.subscribeOn()), Creator.getScheduler(rxObservable.observeOn()));
        } else {
            methodData.first.addStatement("return $T.create($L).subscribeOn($N).observeOn($N)",
                    Observable.class, onSubscribe, Creator.getScheduler(rxObservable.subscribeOn()),
                    Creator.getScheduler(rxObservable.observeOn()));

        }
        return methodData.first.build();
    }

    private static TypeSpec buildObservable(ExecutableElement executableElement, List<String> parameters) {
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

}
