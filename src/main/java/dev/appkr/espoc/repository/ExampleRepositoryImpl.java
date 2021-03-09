package dev.appkr.espoc.repository;

import static dev.appkr.espoc.domain.QExample.example;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.appkr.espoc.domain.Example;
import dev.appkr.espoc.support.Carbon;
import java.util.List;

public class ExampleRepositoryImpl implements ExampleRepositoryCustom {

  private final JPAQueryFactory query;

  public ExampleRepositoryImpl(JPAQueryFactory query) {
    this.query = query;
  }

  @Override
  public List<Example> findCreatedToday() {
    return query.selectFrom(example)
        .where(withinToday())
        .fetch();
  }

  private BooleanExpression withinToday() {
    return example.createdAt.gt(Carbon.seoul().startOfDay().toInstant())
        .and(example.createdAt.lt(Carbon.seoul().endOfDay().toInstant()));
  }
}
