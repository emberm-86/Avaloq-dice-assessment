package com.assignment.dicegame;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.assignment.dicegame.pojo.*;
import com.assignment.dicegame.pojo.Error;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.assignment.dicegame.Util.sumDist;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApplicationTests {

  @Autowired
  Controller controller;

  @Autowired
  MongoTemplate mongoTemplate;

  @AfterEach
  public void cleanUp() {
    mongoTemplate.remove(new Query(), Roll.COLLECTION_NAME);
  }

  @Test
  void testSaveRolls() {
    Request request = createRollRequest(100, 6, 3);

    ResponseEntity<?> response = controller.saveRolls(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    Object responseBody = response.getBody();
    assertTrue(responseBody instanceof SaveResult);

    SaveResult body = (SaveResult) responseBody;
    List<Integer> thrownValues = body.getThrownValues();

    assertEquals(thrownValues.size(), 100);
    IntStream.range(0, thrownValues.size())
        .forEach(i -> assertTrue(thrownValues.get(i) >= 3 && thrownValues.get(i) <= 18));
  }

  @Test
  void testGetRollsWithDistribution() {
    Request request1 = createRollRequest(683209, 6, 3);
    Request request2 = createRollRequest(912898, 4, 2);
    Request request3 = createRollRequest(713210, 4, 2);
    Request request4 = createRollRequest(813220, 6, 3);

    controller.saveRolls(request1);
    controller.saveRolls(request2);
    controller.saveRolls(request3);
    controller.saveRolls(request4);

    ResponseEntity<List<GetResult>> getWithDistResp = controller.getRolls();

    assertEquals(HttpStatus.OK, getWithDistResp.getStatusCode());

    List<GetResult> body = getWithDistResp.getBody();
    assertNotNull(body);

    Map<String, List<Distribution>> distributions = body.stream()
        .collect(Collectors.toMap(GetResult::getId, GetResult::getRelDists));

    long hundredSumCount = distributions.entrySet().stream()
        .filter(sumDistItem -> {
          List<BigDecimal> values = sumDistItem.getValue().stream().map(Distribution::getRelDist)
              .map(BigDecimal::new).collect(Collectors.toList());
          BigDecimal bigDecimal = sumDist(values);
          return bigDecimal.compareTo(new BigDecimal("100.00")) == 0;
        }).count();
    assertEquals((int)hundredSumCount, distributions.entrySet().size());
  }

  @ParameterizedTest
  @MethodSource("createInvalidData")
  public void testValidation(Request request) {
    ResponseEntity<?> response = controller.saveRolls(request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    Object responseBody = response.getBody();
    assertTrue(responseBody instanceof Error);

    Error body = (Error) responseBody;
    assertNotNull(body);
    assertFalse(StringUtils.isBlank(body.getErrorMessage()));
  }

  private static Stream<Arguments> createInvalidData() {
    return Stream.of(
        Arguments.of( // check for different types
            createRollRequest(100, 3, 3)),

        Arguments.of( // check for different types
            createRollRequest(100, 6, 0)),

        Arguments.of( // check for different types
            createRollRequest(0, 6, 3)));
  }

  private static Request createRollRequest(int numberOfRolls, int sideOfDice, int numberOfDice) {
    return Request.builder()
        .numberOfRolls(numberOfRolls)
        .sideOfDice(sideOfDice)
        .numberOfDice(numberOfDice)
        .build();
  }
}