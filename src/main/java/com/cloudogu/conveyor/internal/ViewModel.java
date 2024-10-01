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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewModel {

  private final String simpleClassName;
  private final List<DtoField> fields = new ArrayList<>();

  public ViewModel(String simpleClassName) {
    this.simpleClassName = simpleClassName;
  }

  public String getSimpleClassName() {
    return simpleClassName;
  }

  void addField(DtoField field) {
    this.fields.add(field);
  }

  public List<DtoField> getFields() {
    return Collections.unmodifiableList(fields);
  }
}
