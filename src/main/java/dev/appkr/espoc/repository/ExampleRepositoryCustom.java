package dev.appkr.espoc.repository;

import dev.appkr.espoc.domain.Example;
import java.util.List;

public interface ExampleRepositoryCustom {

  List<Example> findCreatedToday();
}
