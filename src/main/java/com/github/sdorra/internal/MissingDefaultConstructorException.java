package com.github.sdorra.internal;

import javax.lang.model.element.TypeElement;

public class MissingDefaultConstructorException extends RuntimeException {
  public MissingDefaultConstructorException(TypeElement typeElement) {
    super("could not find default constructor of " + typeElement.getQualifiedName());
  }
}
