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

package com.cloudogu.conveyor;

import de.otto.edison.hal.Links;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThat;

class PersonTest {

  @Test
  void shouldCreateDto() {
    Person person = createTrillian();
    PersonDto dto = PersonDto.from(person);
    assertThat(dto)
      .usingRecursiveComparison()
      .ignoringFields("notes", "links", "embedded", "curies", "attributes")
      .isEqualTo(person);
  }

  @Test
  void shouldCreateDtoWithLinks() {
    Person person = createTrillian();

    Links links = Links.linkingTo()
      .self("/people/trillian")
      .build();

    PersonDto dto = PersonDto.from(person, links);
    assertThat(dto.getLinks()).isEqualTo(links);
  }

  @Test
  void shouldCopySimpleAnnotation() throws NoSuchFieldException {
    NotNull annotation = annotation("firstName", NotNull.class);
    assertThat(annotation).isNotNull();
  }

  @Test
  void shouldNotCopyExportedAnnotation() throws NoSuchFieldException {
    Include annotation = annotation("firstName", Include.class);
    assertThat(annotation).isNull();
  }

  @Test
  void shouldCopyComplexAnnotation() throws NoSuchFieldException {
    Size annotation = annotation("lastName", Size.class);
    assertThat(annotation.min()).isEqualTo(1);
    assertThat(annotation.max()).isEqualTo(42);
  }

  @Test
  void shouldUpdateEntity() {
    Person person = createTrillian();
    PersonDto dto = PersonDto.from(person);
    dto.setLastName("M ...");
    dto.update(person);

    assertThat(person.getLastName()).isEqualTo("M ...");
  }

  @Test
  void shouldCreateEntityFromDto() {
    Person trillian = createTrillian();
    Person person = PersonDto.from(trillian).toEntity();

    assertThat(trillian)
      .usingRecursiveComparison()
      // age is not writable and note is not exported
      .ignoringFields("age", "notes")
      .isEqualTo(person);
  }

  private <T extends Annotation> T annotation(String field, Class<T> annotation) throws NoSuchFieldException {
    return PersonDto.class.getDeclaredField(field).getAnnotation(annotation);
  }

  private Person createTrillian() {
    Person person = new Person();
    person.setFirstName("Trillian");
    person.setLastName("McMillan");
    person.setAge(26);
    person.setHuman(true);
    person.setNotes("This should not be in the dto");
    return person;
  }

}
