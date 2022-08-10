package com.assignment.dicegame;

import com.assignment.dicegame.pojo.Request;
import com.assignment.dicegame.pojo.Roll;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;

import static com.assignment.dicegame.Constants.DECIMAL_FORMAT_FRACTION_DIGITS;
import static com.assignment.dicegame.Constants.MATH_CONTEXT;
import static java.math.BigDecimal.ZERO;

public class Util {

  public static BigDecimal sumDist(Collection<BigDecimal> dists) {
    return dists.stream().reduce(ZERO,
        (bigDecimal, augend) -> bigDecimal.add(augend, MATH_CONTEXT)
            .setScale(DECIMAL_FORMAT_FRACTION_DIGITS, RoundingMode.DOWN));
  }

  public static long getCountOfTotals(Request rollPojo, List<Integer> allThrownValues) {
    return allThrownValues.stream()
        .filter(sum -> sum == rollPojo.getNumberOfDice() * rollPojo.getSideOfDice()).count();
  }

  public static void rollWithDice(Request req, List<Integer> allThrownValues) {
    IntStream.range(0, req.getNumberOfRolls())
        .forEach(i -> allThrownValues.add(getThrownValues(req)));
  }

  private static Integer getThrownValues(Request req) {
    return IntStream.range(0, req.getNumberOfDice())
        .map(j -> RandomUtils.nextInt(1, req.getSideOfDice() + 1)).boxed().reduce(0, Integer::sum);
  }

  public static String createId(Integer numberOfDice, Integer sideOfDice) {
    return "{\"numberOfDice\": " + numberOfDice + ", \"sideOfDice\": " + sideOfDice + "}";
  }

  public static Map<Integer, Long> getListItemCount(List<Integer> list) {
    return list.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
  }

  public static Roll createRollEntity(Request rollPojo, List<Integer> allThrownValues) {
    return Roll.builder()
        .numberOfDice(rollPojo.getNumberOfDice())
        .numberOfRolls(rollPojo.getNumberOfRolls())
        .sideOfDice(rollPojo.getSideOfDice())
        .thrownValues(allThrownValues)
        .build();
  }

  public static Map<Integer, Pair<BigDecimal, BigDecimal>> orderBoundDists(
      Map<Integer, Pair<BigDecimal, BigDecimal>> differences,
      Comparator<Entry<Integer, Pair<BigDecimal, BigDecimal>>> comparator) {
    return differences.entrySet().stream().filter(s -> s.getValue().getRight().compareTo(ZERO) > 0)
        .sorted(comparator)
        .collect(
            Collectors.toMap(Entry::getKey, Entry::getValue, (s1, s2) -> s1, LinkedHashMap::new));
  }

  public static void calculateDifference
      (String key, Integer entryIntKey, BigDecimal newDistribution,
          Map<String, Map<Integer, Pair<BigDecimal, BigDecimal>>> differences,
          BigDecimal difference) {
    Map<Integer, Pair<BigDecimal, BigDecimal>> newDifference = new HashMap<>();
    newDifference.put(entryIntKey, Pair.of(newDistribution, difference));
    differences.putIfAbsent(key, newDifference);
    differences.computeIfPresent(key, (t, u) -> {
      u.putAll(newDifference);
      return u;
    });
  }
}
