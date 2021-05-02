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

import com.google.auto.common.MoreElements;

import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.List;

public class Model {

  private final TypeElement classElement;
  private final List<DtoField> exportedFields;
  private final String simpleClassName;

  Model(TypeElement classElement, List<DtoField> exportedFields, String simpleClassName) {
    this.classElement = classElement;
    this.exportedFields = Collections.unmodifiableList(exportedFields);
    this.simpleClassName = simpleClassName;
  }

  public TypeElement getClassElement() {
    return classElement;
  }

  public String getClassName() {
    return getPackageName() + "." + simpleClassName;
  }

  public List<DtoField> getExportedFields() {
    return exportedFields;
  }

  public String getSimpleClassName() {
    return simpleClassName;
  }

  @SuppressWarnings("UnstableApiUsage")
  public String getPackageName() {
    return MoreElements.asPackage(classElement.getEnclosingElement()).getQualifiedName().toString();
  }
}
