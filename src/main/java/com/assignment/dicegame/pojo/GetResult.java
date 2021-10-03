package com.assignment.dicegame.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class GetResult {

  @JsonProperty
  final String id;

  @JsonProperty("total_count_simulations")
  int totalCountOfSimulations;

  @JsonProperty("total_sum_rolls")
  final int totalSumOfRolls;

  @JsonProperty("relative_distributions")
  List<Distribution> relDists;
}
