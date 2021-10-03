package com.assignment.dicegame.pojo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;

@Data
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RollCount {

  @Id
  String id;
  int totalCountOfSimulations;
}
