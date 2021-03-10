package dev.appkr.espoc.domain;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Person {

  private int age;
  private String name;
  private LocalDate dateOfBirth;

  @Builder
  public Person(int age, String name, LocalDate dateOfBirth) {
    this.age = age;
    this.name = name;
    this.dateOfBirth = dateOfBirth;
  }

  public Person() {
  }
}
