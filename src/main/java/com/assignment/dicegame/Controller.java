package com.assignment.dicegame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.assignment.dicegame.pojo.*;
import com.assignment.dicegame.pojo.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

  @Autowired
  MongoDbService mongoDbService;

  @PostMapping(value = "roll")
  public ResponseEntity<?> saveRolls(@RequestBody Request request) {
    String errorMessage = null;

    if (request.getNumberOfDice() < 1 || request.getNumberOfRolls() < 1) {
      errorMessage = "The number of dice and the total number of rolls must be at least 1.";
    }

    if (request.getSideOfDice() < 4) {
      errorMessage = "The sides of a dice must be at least 4.";
    }

    if (errorMessage != null) {
      return new ResponseEntity<>(new Error(errorMessage), HttpStatus.BAD_REQUEST);
    }

    List<Integer> allThrownValues = new ArrayList<>();

    Util.rollWithDice(request, allThrownValues);
    mongoDbService.save(request, allThrownValues);

    return ResponseEntity.ok(
        new SaveResult(allThrownValues, Util.getCountOfTotals(request, allThrownValues)));
  }

  @GetMapping(value = "roll")
  public ResponseEntity<List<GetResult>> getRolls() {
    List<GetResult> getResults = mongoDbService.getRollSum().stream().map(rollSum ->
        new GetResult(rollSum.getId(), rollSum.getTotalSumOfRolls())).collect(Collectors.toList());

    Map<String, Integer> rollCountMap = mongoDbService.getRollCount().stream()
        .collect(Collectors.toMap(RollCount::getId, RollCount::getTotalCountOfSimulations));

    Map<String, Map<Integer, String>> distributionMap = mongoDbService.getDistributions();

    getResults.forEach(getResult -> {
          getResult.setRelDists(new ArrayList<>());
          getResult.setTotalCountOfSimulations(rollCountMap.get(getResult.getId()));

          final Map<Integer, String> distributions = distributionMap.get(getResult.getId());
          distributions.forEach((k, v) -> getResult.getRelDists().add(new Distribution(k, v)));
        }
    );

    return ResponseEntity.ok(getResults);
  }
}
