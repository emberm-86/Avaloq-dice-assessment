package com.assignment.dicegame.pojo;

import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = Roll.COLLECTION_NAME)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Builder
public class Roll {

  public static final String COLLECTION_NAME = "rolls";

  int numberOfDice;
  int sideOfDice;
  int numberOfRolls;
  List<Integer> thrownValues;
}
