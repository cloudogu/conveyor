/*
 * MIT License
 *
 * Copyright (c) 2021, Sebastian Sdorra
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.sdorra.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.Writer;

class SourceCodeGenerator {

  void generate(Writer writer, Model model) throws IOException {
    TypeSpec.Builder builder = TypeSpec.classBuilder(model.getSimpleClassName())
      .superclass(HalRepresentation.class)
      .addModifiers(Modifier.PUBLIC)
      .addMethod(MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(ParameterSpec.builder(Links.class, "links")
          .addAnnotation(Nullable.class)
          .build()
        )
        .addParameter(ParameterSpec.builder(Embedded.class, "embedded")
          .addAnnotation(Nullable.class)
          .build()
        )
        .addStatement("super($N, $N)", "links", "embedded")
        .build()
      );

    for (DtoField exportedField : model.getExportedFields()) {
      appendField(builder, exportedField);
    }

    appendFrom(model, builder);

    JavaFile javaFile = JavaFile.builder(model.getPackageName(), builder.build()).build();
    javaFile.writeTo(writer);
  }

  private void appendFrom(Model model, TypeSpec.Builder builder) {
    String entity = "entity";
    String dto = "dto";


    TypeName entityType = TypeName.get(model.getClassElement().asType());
    ClassName dtoType = ClassName.bestGuess(model.getSimpleClassName());


    builder.addMethod(MethodSpec.methodBuilder("from")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addParameter(entityType, entity)
      .returns(dtoType)
      .addStatement("return from($N, $N, $N)", entity, "null", "null")
      .build()
    );

    builder.addMethod(MethodSpec.methodBuilder("from")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addParameter(entityType, entity)
      .addParameter(ParameterSpec.builder(Links.class, "links")
        .addAnnotation(Nullable.class)
        .build()
      )
      .returns(dtoType)
      .addStatement("return from($N, $N, $N)", entity, "links", "null")
      .build()
    );

    MethodSpec.Builder method = MethodSpec.methodBuilder("from")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addParameter(entityType, entity)
      .addParameter(ParameterSpec.builder(Links.class, "links")
        .addAnnotation(Nullable.class)
        .build()
      )
      .addParameter(ParameterSpec.builder(Embedded.class, "embedded")
        .addAnnotation(Nullable.class)
        .build()
      )
      .returns(dtoType)
      .addStatement("$T $N = new $T($N, $N)", dtoType, dto, dtoType, "links", "embedded");

    for (DtoField field : model.getExportedFields()) {
      method.addStatement("$N.$N = $N.$N()", dto, field.getName(), entity, field.getGetter().getSimpleName());
    }

    method.addStatement("return $N", dto);

    builder.addMethod(method.build());
  }

  private void appendField(TypeSpec.Builder builder, DtoField field) {
    TypeName typeName = TypeName.get(field.getType());
    builder.addField(typeName, field.getName(), Modifier.PRIVATE);

    appendGetter(builder, field, typeName);
    field.getSetter().ifPresent(element -> appendSetter(builder, field, element));
  }

  private void appendSetter(TypeSpec.Builder builder, DtoField field, Element setter) {
    builder.addMethod(
      MethodSpec.methodBuilder(setter.getSimpleName().toString())
        .addModifiers(Modifier.PUBLIC)
        .addStatement("this.$N = $N", field.getName(), field.getName())
        .build()
    );
  }

  private void appendGetter(TypeSpec.Builder builder, DtoField field, TypeName typeName) {
    String getterName = field.getGetter().getSimpleName().toString();
    builder.addMethod(
      MethodSpec.methodBuilder(getterName)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("return $N", field.getName())
        .returns(typeName)
        .build()
    );
  }
}
