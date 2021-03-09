package dev.appkr.espoc.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Author {

  private String name;

  @Builder
  Author(String name) {
    this.name = name;
  }

  protected Author() {}
}
