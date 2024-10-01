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

import com.cloudogu.conveyor.GenerateDto;
import com.google.auto.common.MoreElements;
import org.kohsuke.MetaInfServices;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;

@MetaInfServices(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.cloudogu.conveyor.GenerateDto")
public class ConveyorProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      return false;
    }

    for (TypeElement annotation : annotations) {
      for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
        process(element);
      }
    }

    return false;
  }

  @SuppressWarnings("UnstableApiUsage")
  private void process(Element element) {
    GenerateDto annotation = element.getAnnotation(GenerateDto.class);
    ModelBuilder modelBuilder = new ModelBuilder(MoreElements.asType(element), annotation);
    Model model = modelBuilder.create();

    try {
      write(model);
    } catch (IOException ex) {
      throw new IllegalStateException("failed to create model", ex);
    }
  }

  private void write(Model model) throws IOException {
    Filer filer = processingEnv.getFiler();
    SourceCodeGenerator generator = new SourceCodeGenerator(filer);
    generator.generate(model);
  }
}
