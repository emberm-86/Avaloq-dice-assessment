package com.assignment.dicegame.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@JsonInclude(Include.NON_NULL)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SaveResult {

  @JsonProperty("results")
  List<Integer> thrownValues;

  @JsonProperty("count_of_totals")
  long countOfTotals;
}
