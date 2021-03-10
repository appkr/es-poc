package dev.appkr.espoc.domain;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

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
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

@Document(indexName = "blog")
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"id"})
public class Article {

  @Id
  private String id;

  // We use FieldType.keyword to indicate that we do not want to use an analyzer
  //   when performing the additional indexing of the field,
  //   and that this value should be stored using a nested field with the suffix verbatim.
  //   e.g.
  //   NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
  //       .withQuery(matchQuery("title.verbatim", "Second Article About Elasticsearch"))
  //       .build();
  @MultiField(mainField = @Field(type = Text, fielddata = true),
      otherFields = {@InnerField(suffix = "verbatim", type = Keyword)})
  private String title;

  @Field(type = Keyword)
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

