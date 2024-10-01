/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.conveyor.internal;

import com.google.auto.common.MoreElements;

import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.List;

public class Model {

  private final TypeElement classElement;
  private final List<DtoField> exportedFields;
  private final List<ViewModel> views;
  private final String simpleClassName;

  Model(TypeElement classElement, List<DtoField> exportedFields, List<ViewModel> views, String simpleClassName) {
    this.classElement = classElement;
    this.exportedFields = Collections.unmodifiableList(exportedFields);
    this.views = views;
    this.simpleClassName = simpleClassName;
  }

  public TypeElement getClassElement() {
    return classElement;
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

  public List<ViewModel> getViews() {
    return views;
  }
}
