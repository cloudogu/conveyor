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

import jakarta.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class DtoField {

  private final VariableElement field;
  private final Element getter;
  private final Element setter;
  private final List<String> views = new ArrayList<>();

  DtoField(VariableElement field, String[] views, Element getter, @Nullable Element setter) {
    this.field = field;
    this.views.addAll(Arrays.asList(views));
    this.getter = getter;
    this.setter = setter;
  }

  public VariableElement getField() {
    return field;
  }

  public TypeMirror getType() {
    return field.asType();
  }

  public String getName() {
    return field.getSimpleName().toString();
  }

  public Element getGetter() {
    return getter;
  }

  public Optional<Element> getSetter() {
    return Optional.ofNullable(setter);
  }

  public List<String> getViews() {
    return Collections.unmodifiableList(views);
  }
}
