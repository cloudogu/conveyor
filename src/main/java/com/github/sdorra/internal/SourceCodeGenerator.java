/*
 * MIT License
 *
 * Copyright (c) 2021, Cloudogu GmbH
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

import com.github.sdorra.Include;
import com.github.sdorra.View;
import com.google.auto.common.MoreElements;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;

import javax.annotation.Nullable;
import javax.annotation.processing.Filer;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;

class SourceCodeGenerator {

  private static final String FIELD_LINKS = "links";
  private static final String FIELD_EMBEDDED = "embedded";

  private static final String FIELD_ENTITY = "entity";
  private static final String FIELD_DTO = "dto";

  private static final String METHOD_FROM = "from";
  private static final String METHOD_UPDATE = "update";
  private static final String METHOD_TO_ENTITY = "toEntity";

  private static final String NULL = "null";

  private final Filer filer;

  public SourceCodeGenerator(Filer filer) {
    this.filer = filer;
  }

  void generate(Model model) throws IOException {
    TypeSpec.Builder builder = TypeSpec.classBuilder(model.getSimpleClassName())
      .superclass(HalRepresentation.class)
      .addModifiers(Modifier.PUBLIC)
      .addMethod(MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(ParameterSpec.builder(Links.class, FIELD_LINKS)
          .addAnnotation(Nullable.class)
          .build()
        )
        .addParameter(ParameterSpec.builder(Embedded.class, FIELD_EMBEDDED)
          .addAnnotation(Nullable.class)
          .build()
        )
        .addStatement("super($N, $N)", FIELD_LINKS, FIELD_EMBEDDED)
        .build()
      )
      .addMethod(MethodSpec.constructorBuilder()
        .build()
      );

    for (ViewModel view : model.getViews()) {
      createInterface(model, view);
      builder.addSuperinterface(ClassName.get(model.getPackageName(), view.getSimpleClassName()));
    }

    for (DtoField exportedField : model.getExportedFields()) {
      appendField(builder, exportedField);
    }

    appendFrom(model, builder);
    appendUpdate(model, builder);
    appendToEntity(model, builder);

    write(model, builder.build());
  }

  private void createInterface(Model model, ViewModel view) throws IOException {
    TypeSpec.Builder builder = TypeSpec.interfaceBuilder(view.getSimpleClassName())
      .addModifiers(Modifier.PUBLIC);

    for (DtoField field : view.getFields()) {
      builder.addMethod(
        MethodSpec.methodBuilder(field.getGetter().getSimpleName().toString())
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
          .returns(TypeName.get(field.getType()))
          .build()
      );
    }

    write(model, builder.build());
  }

  private void write(Model model, TypeSpec typeSpec) throws IOException {
    JavaFile javaFile = JavaFile.builder(model.getPackageName(), typeSpec).build();

    String className = model.getPackageName() + "." + typeSpec.name;

    JavaFileObject jfo = filer.createSourceFile(className, model.getClassElement());
    try (Writer writer = jfo.openWriter()) {
      javaFile.writeTo(writer);
    }
  }

  private void appendToEntity(Model model, TypeSpec.Builder builder) {
    TypeName entityType = TypeName.get(model.getClassElement().asType());

    MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_TO_ENTITY)
      .addModifiers(Modifier.PUBLIC)
      .returns(entityType)
      .addStatement(
        "$T $N = new $T()", entityType, FIELD_ENTITY, entityType
      ).addStatement(
        "$N($N)", METHOD_UPDATE, FIELD_ENTITY
      ).addStatement(
        "return $N", FIELD_ENTITY
      );

    builder.addMethod(method.build());
  }

  private void appendUpdate(Model model, TypeSpec.Builder builder) {
    TypeName entityType = TypeName.get(model.getClassElement().asType());

    MethodSpec.Builder updateMethod = MethodSpec.methodBuilder(METHOD_UPDATE)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(entityType, FIELD_ENTITY);

    for (DtoField field : model.getExportedFields()) {
      field.getSetter().ifPresent(element -> updateMethod.addStatement(
        "$N.$N(this.$N)",
        FIELD_ENTITY, element.getSimpleName(), field.getName()
      ));
    }

    builder.addMethod(updateMethod.build());
  }

  private void appendFrom(Model model, TypeSpec.Builder builder) {
    TypeName entityType = TypeName.get(model.getClassElement().asType());
    ClassName dtoType = ClassName.bestGuess(model.getSimpleClassName());

    builder.addMethod(MethodSpec.methodBuilder(METHOD_FROM)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addParameter(entityType, FIELD_ENTITY)
      .returns(dtoType)
      .addStatement("return from($N, $N, $N)", FIELD_ENTITY, NULL, NULL)
      .build()
    );

    builder.addMethod(MethodSpec.methodBuilder(METHOD_FROM)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addParameter(entityType, FIELD_ENTITY)
      .addParameter(ParameterSpec.builder(Links.class, FIELD_LINKS)
        .addAnnotation(Nullable.class)
        .build()
      )
      .returns(dtoType)
      .addStatement("return from($N, $N, $N)", FIELD_ENTITY, FIELD_LINKS, NULL)
      .build()
    );

    MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_FROM)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addParameter(entityType, FIELD_ENTITY)
      .addParameter(ParameterSpec.builder(Links.class, FIELD_LINKS)
        .addAnnotation(Nullable.class)
        .build()
      )
      .addParameter(ParameterSpec.builder(Embedded.class, FIELD_EMBEDDED)
        .addAnnotation(Nullable.class)
        .build()
      )
      .returns(dtoType)
      .addStatement("$T $N = new $T($N, $N)", dtoType, FIELD_DTO, dtoType, FIELD_LINKS, FIELD_EMBEDDED);

    for (DtoField field : model.getExportedFields()) {
      method.addStatement("$N.$N = $N.$N()", FIELD_DTO, field.getName(), FIELD_ENTITY, field.getGetter().getSimpleName());
    }

    method.addStatement("return $N", FIELD_DTO);

    builder.addMethod(method.build());
  }

  private void appendField(TypeSpec.Builder builder, DtoField field) {
    TypeName typeName = TypeName.get(field.getType());

    FieldSpec.Builder fieldSpec = FieldSpec.builder(typeName, field.getName(), Modifier.PRIVATE);

    for (AnnotationMirror mirror : field.getField().getAnnotationMirrors()) {
      if (!isTypeOf(mirror, Include.class) && !isTypeOf(mirror, View.class)) {
        fieldSpec.addAnnotation(AnnotationSpec.get(mirror));
      }
    }

    builder.addField(fieldSpec.build());

    appendGetter(builder, field, typeName);
    field.getSetter().ifPresent(element -> appendSetter(builder, field, element));
  }

  private boolean isTypeOf(AnnotationMirror annotationMirror, Class<? extends Annotation> annotation) {
    return isTypeOf(annotationMirror.getAnnotationType().asElement(), annotation);
  }

  @SuppressWarnings("UnstableApiUsage")
  private boolean isTypeOf(Element element, Class<?> type) {
    TypeElement typeElement = MoreElements.asType(element);
    return typeElement.getQualifiedName().contentEquals(type.getName());
  }

  private void appendSetter(TypeSpec.Builder builder, DtoField field, Element setter) {
    builder.addMethod(
      MethodSpec.methodBuilder(setter.getSimpleName().toString())
        .addModifiers(Modifier.PUBLIC)
        .addParameter(TypeName.get(field.getType()), field.getName())
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
