package com.assignment.dicegame;

import com.assignment.dicegame.pojo.Request;
import com.assignment.dicegame.pojo.Roll;
import com.assignment.dicegame.pojo.RollCount;
import com.assignment.dicegame.pojo.RollSum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.stereotype.Service;

import static com.assignment.dicegame.Constants.DECIMAL_FORMAT_DIFFERENCE_FRACTION_DIGITS;
import static com.assignment.dicegame.Constants.DECIMAL_FORMAT_FRACTION_DIGITS;
import static com.assignment.dicegame.Constants.HUNDRED;
import static com.assignment.dicegame.Constants.MATH_CONTEXT;
import static com.assignment.dicegame.Constants.MATH_CONTEXT_DIFF;
import static com.assignment.dicegame.Constants.STEP;
import static com.assignment.dicegame.Util.calculateDifference;
import static com.assignment.dicegame.Util.createId;
import static com.assignment.dicegame.Util.createRollEntity;
import static com.assignment.dicegame.Util.getListItemCount;
import static com.assignment.dicegame.Util.orderBoundDists;
import static com.assignment.dicegame.Util.sumDist;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
public class MongoDbService {

  @Autowired
  private MongoTemplate mongoTemplate;

  public void save(Request request, List<Integer> allThrownValues) {
    mongoTemplate.save(createRollEntity(request, allThrownValues));
  }

  public List<RollSum> getRollSum() {
    GroupOperation totalSumOfRolls = group("numberOfDice", "sideOfDice").sum("numberOfRolls")
        .as("totalSumOfRolls");

    return mongoTemplate.aggregate(newAggregation(totalSumOfRolls),
        Roll.COLLECTION_NAME, RollSum.class).getMappedResults();
  }

  public List<RollCount> getRollCount() {
    GroupOperation totalCountOfSimulations = group("numberOfDice", "sideOfDice")
        .count().as("totalCountOfSimulations");

    return mongoTemplate.aggregate(newAggregation(totalCountOfSimulations),
        Roll.COLLECTION_NAME, RollCount.class).getMappedResults();
  }

  public Map<String, Map<Integer, String>> getDistributions() {
    List<Roll> allRolls = mongoTemplate.findAll(Roll.class);

    Map<String, Integer> rollsTotal = getRollsTotal(allRolls);
    Map<String, Map<Integer, Long>> rollsWithCount = getRollsWithCount(allRolls);

    Map<String, Map<Integer, BigDecimal>> distributionMap = new HashMap<>();
    Map<String, Map<Integer, Pair<BigDecimal, BigDecimal>>> differencesToUpper = new HashMap<>();
    Map<String, Map<Integer, Pair<BigDecimal, BigDecimal>>> differencesToLower = new HashMap<>();

    rollsWithCount.entrySet().forEach(roll ->
        addDistributionItem(rollsTotal, distributionMap, differencesToUpper, differencesToLower,
            roll));

    Map<String, BigDecimal> sums = distributionMap.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, e -> sumDist(e.getValue().values())));

    correctionIfNeeded(distributionMap, differencesToUpper, differencesToLower, sums);

    return distributionMap.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey,
            allDistsByConf -> allDistsByConf.getValue().entrySet().stream().
                collect(Collectors.toMap(Entry::getKey,
                    distsByRollSum -> distsByRollSum.getValue().toString()))));
  }

  private void addDistributionItem(Map<String, Integer> rollsTotal,
      Map<String, Map<Integer, BigDecimal>> distributionMap,
      Map<String, Map<Integer, Pair<BigDecimal, BigDecimal>>> differencesToUpper,
      Map<String, Map<Integer, Pair<BigDecimal, BigDecimal>>> differencesToLower,
      Map.Entry<String, Map<Integer, Long>> allRolls) {

    String rollConfigurationKey = allRolls.getKey();
    Map<Integer, Long> rollOccurrences = allRolls.getValue();

    for (Entry<Integer, Long> rollOccurrence : rollOccurrences.entrySet()) {
      BigDecimal val = new BigDecimal(rollOccurrence.getValue())
          .divide(new BigDecimal(rollsTotal.get(rollConfigurationKey)), MATH_CONTEXT_DIFF)
          .multiply(HUNDRED, MATH_CONTEXT_DIFF)
          .setScale(DECIMAL_FORMAT_DIFFERENCE_FRACTION_DIGITS, RoundingMode.HALF_UP);

      BigDecimal newDistribution = val.setScale(DECIMAL_FORMAT_FRACTION_DIGITS,
          RoundingMode.HALF_UP);

      Map<Integer, BigDecimal> newRollValueDist = new HashMap<>();
      Integer rollValueAsKey = rollOccurrence.getKey();
      newRollValueDist.put(rollValueAsKey, newDistribution);

      distributionMap.putIfAbsent(rollConfigurationKey, newRollValueDist);
      distributionMap.computeIfPresent(rollConfigurationKey, (t, u) -> {
        u.putAll(newRollValueDist);
        return u;
      });

      BigDecimal upperBound = val.setScale(DECIMAL_FORMAT_FRACTION_DIGITS, RoundingMode.CEILING);
      BigDecimal lowerBound = val.setScale(DECIMAL_FORMAT_FRACTION_DIGITS, RoundingMode.FLOOR);

      calculateDifference(rollConfigurationKey, rollValueAsKey, newDistribution, differencesToUpper,
          upperBound.subtract(val));

      calculateDifference(rollConfigurationKey, rollValueAsKey, newDistribution, differencesToLower,
          val.subtract(lowerBound));
    }
  }

  private void correctionIfNeeded(Map<String, Map<Integer, BigDecimal>> distributionMap,
      Map<String, Map<Integer, Pair<BigDecimal, BigDecimal>>> differencesToUpper,
      Map<String, Map<Integer, Pair<BigDecimal, BigDecimal>>> differencesToLower,
      Map<String, BigDecimal> sums) {

    Comparator<Entry<Integer, Pair<BigDecimal, BigDecimal>>> comparator = Comparator.comparing(
        s -> s.getValue().getRight());

    sums.entrySet().forEach(distSum -> {
      orderAndCorrection(distributionMap, differencesToLower, distSum,
          bigDecimal -> bigDecimal.compareTo(HUNDRED) > 0, STEP.negate(), comparator);

      orderAndCorrection(distributionMap, differencesToUpper, distSum,
          bigDecimal -> bigDecimal.compareTo(HUNDRED) < 0, STEP, comparator);
    });
  }

  private void orderAndCorrection(Map<String, Map<Integer, BigDecimal>> distributionMap,
      Map<String, Map<Integer, Pair<BigDecimal, BigDecimal>>> differences,
      Map.Entry<String, BigDecimal> distSum, Predicate<BigDecimal> predicate, BigDecimal step,
      Comparator<Entry<Integer, Pair<BigDecimal, BigDecimal>>> comparator) {
    Map<Integer, Pair<BigDecimal, BigDecimal>> boundDists = orderBoundDists(
        differences.get(distSum.getKey()), comparator);
    Iterator<Entry<Integer, Pair<BigDecimal, BigDecimal>>> boundDistIt = boundDists.entrySet()
        .iterator();
    correction(distributionMap, distSum, boundDistIt, predicate, step);
  }

  private void correction(Map<String, Map<Integer, BigDecimal>> distributionMap,
      Map.Entry<String, BigDecimal> distSum,
      Iterator<Entry<Integer, Pair<BigDecimal, BigDecimal>>> distIt,
      Predicate<BigDecimal> predicate, BigDecimal step) {
    while (predicate.test(distSum.getValue())) {
      if (distIt.hasNext()) {
        Entry<Integer, Pair<BigDecimal, BigDecimal>> next = distIt.next();
        BigDecimal oldDistValue = distributionMap.get(distSum.getKey()).get(next.getKey());
        BigDecimal newDistValue = oldDistValue.add(step, MATH_CONTEXT);
        distributionMap.get(distSum.getKey()).put(next.getKey(), newDistValue);
      }
      distSum.setValue(distSum.getValue().add(step, MATH_CONTEXT_DIFF));
    }
  }

  private Map<String, Integer> getRollsTotal(List<Roll> allRolls) {
    return allRolls.stream()
        .collect(Collectors.toMap(r -> createId(r.getNumberOfDice(), r.getSideOfDice()),
            Roll::getNumberOfRolls,
            (existingValue, newValue) -> {
              existingValue += newValue;
              return existingValue;
            }));
  }

  private Map<String, Map<Integer, Long>> getRollsWithCount(List<Roll> allRolls) {
    Map<String, List<Integer>> rollsMap = allRolls.stream()
        .collect(Collectors.toMap(r -> createId(r.getNumberOfDice(), r.getSideOfDice()),
            Roll::getThrownValues,
            (existingValue, newValue) -> {
              existingValue.addAll(newValue);
              return existingValue;
            }));

    return rollsMap.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, e -> getListItemCount(e.getValue())));
  }
}