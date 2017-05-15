package com.juliusscript.rxannotation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

/**
 * Created by Julius.
 */
public final class RxMethodCreator {

    private RxMethodCreator() {
    }

    public static Pair<MethodSpec.Builder, List<String>> createRxMethods(ExecutableElement executableElement, Class type) {
        String methodName = executableElement.getSimpleName().toString();
        TypeName returnClass = ClassName.get(executableElement.getReturnType());
        ClassName flowable = ClassName.get(type);
        TypeName flowableReturn = ParameterizedTypeName.get(flowable, returnClass);

        //create new reactive method
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName + "Rx")
                .addModifiers(Modifier.PUBLIC)
                .returns(flowableReturn);

        List<String> parameters = new ArrayList<String>();
        for (VariableElement variableElement : executableElement.getParameters()) {
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(ClassName
                    .get(variableElement.asType()), variableElement.getSimpleName().toString())
                    .addModifiers(Modifier.FINAL);
            methodBuilder.addParameter(paramBuilder.build());
            parameters.add(variableElement.getSimpleName().toString());
        }
        return new Pair<MethodSpec.Builder, List<String>>(methodBuilder, parameters);
    }
}
