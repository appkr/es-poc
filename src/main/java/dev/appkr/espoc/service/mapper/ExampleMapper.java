package dev.appkr.espoc.service.mapper;

import dev.appkr.espoc.api.model.ExampleDto;
import dev.appkr.espoc.domain.Example;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DateTimeMapper.class})
public interface ExampleMapper extends EntityMapper<ExampleDto, Example>{

  @Override
  @Mapping(source = "id", target = "exampleId")
  ExampleDto toDto(Example entity);
}
