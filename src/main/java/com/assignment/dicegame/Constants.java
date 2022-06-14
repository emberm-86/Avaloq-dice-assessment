package com.assignment.dicegame;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.stream.IntStream;

public class Constants {

  public static final Integer DECIMAL_FORMAT_FRACTION_DIGITS = 2; // Just this need to be modified.
  public static final Integer DECIMAL_FORMAT_DIFFERENCE_FRACTION_DIGITS =
      DECIMAL_FORMAT_FRACTION_DIGITS + 1;
  public static final BigDecimal NIL = new BigDecimal("0.00");
  public static final BigDecimal HUNDRED = new BigDecimal("100.00");
  public static final BigDecimal STEP;
  public static final MathContext MATH_CONTEXT = new MathContext(DECIMAL_FORMAT_FRACTION_DIGITS + 2,
      RoundingMode.DOWN);
  public static final MathContext MATH_CONTEXT_DIFF = new MathContext(
      DECIMAL_FORMAT_DIFFERENCE_FRACTION_DIGITS + 2, RoundingMode.HALF_UP);

  static {
    StringBuilder val = new StringBuilder("0.");
    IntStream.range(0, DECIMAL_FORMAT_FRACTION_DIGITS - 1).forEach(i -> val.append("0"));
    val.append("1");
    STEP = new BigDecimal(val.toString());
  }
}
