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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

  private final Account account = new Account("dent", "arthur.dent@hitchhiker.com", "safe");
  private final AccDto dto = AccDto.from(account);

  @Test
  void shouldExcludePassword() {
    assertThat(dto).hasOnlyFields("username", "mail", "lastLogin");
  }

  @Test
  void shouldImplementViewInterfaces() {
    assertThat(dto)
      .isInstanceOf(CreateAccDto.class)
      .isInstanceOf(UpdateAccDto.class);
  }

  @Test
  void shouldHaveCreateInterface() {
    CreateAccDto createDto = dto;
    assertThat(createDto.getUsername()).isEqualTo("dent");
    assertThat(createDto.getMail()).isEqualTo("arthur.dent@hitchhiker.com");
  }

  @Test
  void shouldHaveUpdateInterface() {
    UpdateAccDto updateDto = dto;
    assertThat(updateDto.getMail()).isEqualTo("arthur.dent@hitchhiker.com");
  }

}
