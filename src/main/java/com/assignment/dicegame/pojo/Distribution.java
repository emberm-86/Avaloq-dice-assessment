package com.assignment.dicegame.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Distribution {

  @JsonProperty("sum_value")
  int sum;

  @JsonProperty("dist_value")
  String relDist;
}
