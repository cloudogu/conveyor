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

package com.github.sdorra;

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
    Exported annotation = annotation("firstName", Exported.class);
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
