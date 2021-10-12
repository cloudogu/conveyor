# Conveyor

Generate dtos from annotations.

## Why?

There are a lot of dto mapping libraries out there, so why create another one?
Conveyor was created to support the special needs of [SCM-Manager](https://scm-manager.org).
For [SCM-Manager](https://scm-manager.org) we wanted a library which support the following features:

* Simple to use
* As little boilerplate code as possible
* Support of _links and _embedded of [HAL](https://stateless.group/hal_specification.html)
* Support views to keep our OpenAPI documentation clean
* Copy annotations from source entity

So we decided to create another dto mapping library.

## Usage

```java
@GenerateDto
public class Person {

  @Include
  private String firstName;
  @Include
  private String lastName;
  
  // constructor, getter and setter
}
```

Conveyor will automatically create a `PersonDto` class, which can be used as follows:

```java
Person person = new Person("Tricia", "McMillan");

Links links = Links.linkingTo()
  .self("/people/trillian")
  .build();

PersonDto dto = PersonDto.from(person, links);
```

## Installation

Get the latest stable version from [![Maven Central](https://img.shields.io/maven-central/v/com.cloudogu.conveyor/conveyor.svg)](https://search.maven.org/search?q=g:com.cloudogu.conveyor%20a:conveyor)

### Gradle

```groovy
implementation 'de.otto.edison:edison-hal:2.1.0'
compileOnly 'com.cloudogu.conveyor:conveyor:x.y.z'
annotationProcessor 'com.cloudogu.conveyor:conveyor:x.y.z'
```

### Maven

```xml
<dependency>
  <groupId>de.otto.edison</groupId>
  <artifactId>edison-hal</artifactId>
  <version>2.1.0</version>
</dependency>

<dependency>
  <groupId>com.cloudogu.conveyor</groupId>
  <artifactId>conveyor</artifactId>
  <version>x.y.z</version>
  <optional>true</optional>
</dependency>
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
