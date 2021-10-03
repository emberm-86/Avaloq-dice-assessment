package com.assignment.dicegame.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Builder
public class Request {

  @JsonProperty("number_of_dice")
  int numberOfDice;

  @JsonProperty("side_of_dice")
  int sideOfDice;

  @JsonProperty("number_of_rolls")
  int numberOfRolls;
}
