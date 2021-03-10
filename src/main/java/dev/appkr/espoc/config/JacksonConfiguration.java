package dev.appkr.espoc.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.appkr.espoc.support.json.LocalDateDeserializer;
import dev.appkr.espoc.support.json.LocalDateSerializer;
import java.time.LocalDate;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

  @Bean
  public JsonNullableModule jsonNullableModule() {
    return new JsonNullableModule();
  }

  @Bean
  public JavaTimeModule javaTimeModule() {
    JavaTimeModule module = new JavaTimeModule();
    module.addSerializer(LocalDate.class, new LocalDateSerializer());
    module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
    return module;
  }
}
