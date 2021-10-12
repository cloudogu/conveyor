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

import com.github.sdorra.Exclude;
import com.github.sdorra.GenerateDto;
import com.github.sdorra.Include;
import com.github.sdorra.Strategy;
import com.github.sdorra.View;
import com.google.auto.common.MoreElements;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModelBuilder {

  private final TypeElement classElement;
  private final GenerateDto generateDto;

  private final List<VariableElement> fields = new ArrayList<>();
  private final Map<String, Element> methods = new HashMap<>();

  public ModelBuilder(TypeElement classElement, GenerateDto generateDto) {
    this.classElement = checkDefaultConstructor(classElement);
    this.generateDto = generateDto;
    collect();
  }

  @SuppressWarnings("UnstableApiUsage")
  private TypeElement checkDefaultConstructor(TypeElement classElement) {
    Optional<ExecutableElement> constructor = classElement.getEnclosedElements()
      .stream()
      .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
      .map(MoreElements::asExecutable)
      .filter(e -> e.getParameters().isEmpty())
      .filter(e -> !e.getModifiers().contains(Modifier.PRIVATE))
      .findAny();
    if (!constructor.isPresent()) {
      throw new MissingDefaultConstructorException(classElement);
    }
    return classElement;
  }

  private void collect() {
    collect(classElement);

    TypeMirror superClass = classElement.getSuperclass();

    while ((superClass != null) && (superClass.getKind() == TypeKind.DECLARED)) {
      DeclaredType type = (DeclaredType) superClass;
      Element e = type.asElement();

      if (e.getKind() == ElementKind.CLASS) {
        TypeElement parent = (TypeElement) e;

        collect(parent);
        superClass = parent.getSuperclass();
      } else {
        break;
      }
    }
  }

  private void collect(Element element) {
    for (Element e : element.getEnclosedElements()) {
      if (e.getKind() == ElementKind.FIELD) {
        Include includeAnnotation = e.getAnnotation(Include.class);
        if (includeAnnotation != null) {
          addField(e);
        } else if (generateDto.strategy() == Strategy.EXCLUDE) {
          Exclude excludeAnnotation = e.getAnnotation(Exclude.class);
          if (excludeAnnotation == null) {
            addField(e);
          }
        }
      } else if (e.getKind() == ElementKind.METHOD) {
        methods.put(e.getSimpleName().toString(), e);
      }
    }
  }

  private void addField(Element element) {
    fields.add((VariableElement) element);
  }

  public Model create() {
    List<DtoField> exportedFields = fields.stream()
      .map(this::field)
      .collect(Collectors.toList());
    List<ViewModel> views = createViews(exportedFields);
    return new Model(classElement, exportedFields, views, className());
  }

  private List<ViewModel> createViews(List<DtoField> exportedFields) {
    Map<String, ViewModel> views = new HashMap<>();
    for (DtoField exportedField : exportedFields) {
      for (String view : exportedField.getViews()) {
        ViewModel viewModel = views.computeIfAbsent(view, this::createViewModel);
        viewModel.addField(exportedField);
      }
    }
    return ImmutableList.copyOf(views.values());
  }

  private ViewModel createViewModel(String view) {
    return new ViewModel(createViewClassName(view));
  }

  private String createViewClassName(String view) {
    String className = className();
    String prefix = view.substring(0, 1).toUpperCase() + view.substring(1);
    return prefix + className;
  }

  private DtoField field(VariableElement field) {
    String name = field.getSimpleName().toString();
    Include includeAnnotation = field.getAnnotation(Include.class);

    String capName = name.substring(0, 1).toUpperCase() + name.substring(1);

    String prefix = isBoolean(field) ? "is" : "get";
    Element getter = findRequiredMethod(prefix + capName);
    Element setter = null;
    if (includeAnnotation == null || !includeAnnotation.readOnly()) {
      setter = findRequiredMethod("set" + capName);
    }

    String[] views = new String[0];
    View viewAnnotation = field.getAnnotation(View.class);
    if (viewAnnotation != null) {
      views = viewAnnotation.value();
    }

    return new DtoField(field, views, getter, setter);
  }

  private boolean isBoolean(VariableElement field) {
    return "boolean".equals(field.asType().toString());
  }

  private Element findRequiredMethod(String name) {
    Element method = methods.get(name);
    if (method == null) {
      throw new MissingMethodException(classElement, name);
    }
    return method;
  }

  private String className() {
    if (Strings.isNullOrEmpty(generateDto.className())) {
      return classElement.getSimpleName().toString() + "Dto";
    }
    return generateDto.className();
  }

}
