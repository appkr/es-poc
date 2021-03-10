package dev.appkr.espoc.repository;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.Operator.AND;
import static org.elasticsearch.index.query.QueryBuilders.fuzzyQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.regexpQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.appkr.espoc.domain.Article;
import dev.appkr.espoc.domain.Author;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ArticleRepositoryTest {

  Logger log = LoggerFactory.getLogger(getClass());

  @Autowired ElasticsearchRestTemplate esTemplate;
  @Autowired RestHighLevelClient esClient;
  @Autowired ArticleRepository repository;

  final Author johnSmith = Author.builder().name("John Smith").build();
  final Author johnDoe = Author.builder().name("John Doe").build();
  List<Article> fixtures = new ArrayList<>();

  @Test
  void whenSaveArticle_thenIdIsAssigned() {
    List<Author> authors = asList(johnSmith, johnDoe);
    Article article = fixtures.get(0);

    log.info("saved entity {}", article);
    assertNotNull(article.getId());
  }

  @Test
  void givenSavedEntities_whenSearchByAuthorName_thenRightOnesReturned() {
    Page<Article> page = repository.findByAuthorsName(johnSmith.getName(), PageRequest.of(0, 10));

    log.info("retrieved entities {}", page.getContent());
    assertEquals(2, page.getTotalElements());
  }

  @Test
  void whenSearchByAuthorsNameUsingCustomQuery_thenRightOnesReturned() {
    Page<Article> page = repository.findByAuthorsNameUsingCustomQuery("Smith", PageRequest.of(0, 10));

    log.info("retrieved entities {}", page.getContent());
    assertEquals(2, page.getTotalElements());
  }

  @Test
  void whenFilterByTag_thenRightOnesReturned() {
    Page<Article> page = repository.findByFilteredTagQuery("elasticsearch", PageRequest.of(0, 10));

    log.info("retrieved entities {}", page.getContent());
    assertEquals(3, page.getTotalElements());
  }

  @Test
  void whenFilterByTagAndSearchByAuthorsName_thenRightOnesReturned() {
    Page<Article> page = repository
        .findByAuthorsNameAndFilteredTagQuery("Doe", "elasticsearch", PageRequest.of(0, 10));

    log.info("retrieved entities {}", page.getContent());
    assertEquals(2, page.getTotalElements());
  }

  @Test
  void whenUsingRegexFilter_thenRightOnesReturned() {
    Query query = new NativeSearchQueryBuilder().withFilter(regexpQuery("title", ".*data.*")).build();
    SearchHits<Article> hits = esTemplate.search(query, Article.class, IndexCoordinates.of("blog"));

    log.info("retrieved entities {}", hits.get().map(SearchHit::getContent).collect(toList()));
    assertEquals(1, hits.getTotalHits());
  }

  @Test
  void whenUpdate_thenSuccess() {
    // When the user makes a typo in a word, it is still possible to match it with a search
    // by specifying a fuzziness parameter, which allows inexact matching.
    NativeSearchQuery query = new NativeSearchQueryBuilder()
        .withQuery(fuzzyQuery("title", "serch")).build();
    SearchHits<Article> hits = esTemplate.search(query, Article.class, IndexCoordinates.of("blog"));
    log.info("retrieved entities {}", hits.get().map(SearchHit::getContent).collect(toList()));
    assertEquals(1, hits.getTotalHits());

    Article article = hits.getSearchHits().get(0).getContent();
    String newTitle = "Getting started with Search Engines";
    article.setTitle(newTitle);
    repository.save(article);

    repository.findById(article.getId()).ifPresent(a -> {
      log.info("updated entity {}", article);
      assertEquals(newTitle, a.getTitle());
    });
  }

  @Test
  void whenDelete_thenSuccess() {
    String title = "Spring Data Elasticsearch";
    NativeSearchQuery query = new NativeSearchQueryBuilder()
        .withQuery(matchQuery("title", title).minimumShouldMatch("75%")).build();
    SearchHits<Article> hits = esTemplate.search(query, Article.class, IndexCoordinates.of("blog"));

    log.info("retrieved entities {}", hits.get().map(SearchHit::getContent).collect(toList()));
    assertEquals(1, hits.getTotalHits());
    long count = repository.count();

    repository.delete(hits.getSearchHits().get(0).getContent());

    assertEquals(count - 1L, repository.count());
  }

  @Test
  void whenOneTermMatch_thenRightOnesReturned() {
    // default operator is OR, so the following query generates "Search AND engines"
    NativeSearchQuery query = new NativeSearchQueryBuilder()
        .withQuery(matchQuery("title", "Search engines").operator(AND)).build();
    SearchHits<Article> hits = esTemplate.search(query, Article.class, IndexCoordinates.of("blog"));

    log.info("retrieved entities {}", hits.get().map(SearchHit::getContent).collect(toList()));
    assertEquals(1, hits.getTotalHits());
  }

  @Test
  void whenQueryAggregation_thenSuccess() throws IOException {
    TermsAggregationBuilder termsAggBuilder = AggregationBuilders.terms("top_tags").field("tags");
    SearchSourceBuilder builder = new SearchSourceBuilder().aggregation(termsAggBuilder);

    SearchRequest req = new SearchRequest().indices("blog").source(builder);
    SearchResponse res = esClient.search(req, RequestOptions.DEFAULT);

    Map<String, Aggregation> results = res.getAggregations().asMap();
    ParsedStringTerms topTags = (ParsedStringTerms) results.get("top_tags");
    List<String> tags = topTags.getBuckets().stream().map(b -> b.getKeyAsString()).collect(toList());

    log.info("topTags {}", tags);
    assertEquals(asList("elasticsearch", "spring data", "search engines", "tutorial"), tags);
  }

  @BeforeEach
  void setUp() {
    fixtures.add(repository.save(Article.builder()
        .title("Spring Data Elasticsearch")
        .authors(asList(johnSmith, johnDoe))
        .tags(new String[]{"elasticsearch", "spring data"})
        .build()));

    fixtures.add(repository.save(Article.builder()
        .title("Search engines")
        .authors(asList(johnDoe))
        .tags(new String[]{"search engines", "tutorial"})
        .build()));

    fixtures.add(repository.save(Article.builder()
        .title("Second Article About Elasticsearch")
        .authors(asList(johnSmith))
        .tags(new String[]{"elasticsearch", "spring data"})
        .build()));

    fixtures.add(repository.save(Article.builder()
        .title("Elasticsearch Tutorial")
        .authors(asList(johnDoe))
        .tags(new String[]{"elasticsearch"})
        .build()));
  }

  @AfterEach
  void tearDown() {
    repository.deleteAll();
    fixtures.removeAll(new ArrayList<>());
  }
}
