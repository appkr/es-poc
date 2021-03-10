package dev.appkr.espoc.repository;

import static org.elasticsearch.action.search.SearchType.DFS_QUERY_THEN_FETCH;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.appkr.espoc.domain.Person;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ElasticsearchManualTest {

  Logger log = LoggerFactory.getLogger(getClass());
  List<Person> fixtures = new ArrayList<>();
  @Autowired RestHighLevelClient esClient;
  @Autowired ObjectMapper mapper;

  @Test
  void whenIndex_thenSuccess() throws IOException {
    String json = "{\"age\": 10, \"name\": \"John Doe\", \"dateOfBirth\": \"2021-03-10\"}";
    IndexRequest req = new IndexRequest("people").source(json, XContentType.JSON);

    IndexResponse res = esClient.index(req, DEFAULT);
    final String index = res.getIndex();
    final long version = res.getVersion();

    log.info("response {}", res);
    assertEquals(Result.CREATED, res.getResult());
    assertEquals("people", index);
    assertEquals(1, version);
  }

  @Test
  void whenDelete_thenSuccess() throws IOException {
    String json = "{\"age\": 10, \"name\": \"John Doe\", \"dateOfBirth\": \"2021-03-10\"}";
    IndexRequest indexReq = new IndexRequest("people").source(json, XContentType.JSON);

    IndexResponse indexRes = esClient.index(indexReq, DEFAULT);
    final String id = indexRes.getId();

    GetRequest getReq = new GetRequest("people").id(id);
    GetResponse getRes = esClient.get(getReq, DEFAULT);
    log.info("GET response {}", getRes.getSourceAsString());

    DeleteRequest delReq = new DeleteRequest("people").id(id);
    DeleteResponse delRes = esClient.delete(delReq, DEFAULT);

    assertEquals(Result.DELETED, delRes.getResult());
  }

  @Test
  void whenQuery_thenSuccess() throws IOException {
    SearchSourceBuilder builder = new SearchSourceBuilder()
        .postFilter(QueryBuilders.rangeQuery("age").from("5").to("15"));
    SearchRequest searchReq = new SearchRequest().searchType(DFS_QUERY_THEN_FETCH).source(builder);

    SearchResponse searchRes = esClient.search(searchReq, DEFAULT);

    Stream.of(searchRes.getHits().getHits()).map(p -> {
      Person person = null;
      try {
        person = mapper.readValue(p.getSourceAsString(), Person.class);
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage());
      }

      return person;
    }).forEach(p -> log.info("person {}", p));
  }

  @Test
  void whenQueryUsingLuceneSyntax_thenSuccess() throws IOException {
    SearchSourceBuilder builder = new SearchSourceBuilder()
        .postFilter(QueryBuilders.simpleQueryStringQuery("+John -Doe OR Janette"));
    SearchRequest searchReq = new SearchRequest().searchType(DFS_QUERY_THEN_FETCH).source(builder);

    SearchResponse searchRes = esClient.search(searchReq, DEFAULT);

    Arrays.stream(searchRes.getHits().getHits()).forEach(s -> log.info("source {}", s.getSourceAsString()));
  }

  @Test
  void whenQueryUsingWildcardMatch_thenSuccess() throws IOException {
    SearchSourceBuilder builder = new SearchSourceBuilder()
        .postFilter(QueryBuilders.matchQuery("John", "Name*"));
    SearchRequest searchReq = new SearchRequest().searchType(DFS_QUERY_THEN_FETCH).source(builder);

    SearchResponse searchRes = esClient.search(searchReq, DEFAULT);

    Arrays.stream(searchRes.getHits().getHits()).forEach(s -> log.info("source {}", s.getSourceAsString()));
  }

  @BeforeEach
  void setUp() {
    fixtures.add(new Person(10, "John Doe", LocalDate.now()));
    fixtures.add(new Person(25, "Janette Doe", LocalDate.now()));
  }
}
