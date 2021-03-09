package dev.appkr.espoc.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Id;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "blog")
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"id"})
public class Article {

  @Id
  private String id;

  @Field(type = FieldType.Text)
  private String title;

  @Field(type = FieldType.Keyword)
  private String[] tags;

  @Field(type = FieldType.Nested, includeInParent = true)
  private List<Author> authors = new ArrayList<>();

  @Builder
  public Article(String id, String title, String[] tags, List<Author> authors) {
    this.id = id;
    this.title = title;
    this.tags = tags;
    this.authors = authors;
  }

  protected Article() {}
}

