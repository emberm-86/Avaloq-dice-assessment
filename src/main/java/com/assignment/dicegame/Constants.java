package com.assignment.dicegame;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Constants {

  public static final Integer DECIMAL_FORMAT_FRACTION_DIGITS = 2; // Just this needs to be modified.
  public static final Integer DECIMAL_FORMAT_DIFFERENCE_FRACTION_DIGITS =
      DECIMAL_FORMAT_FRACTION_DIGITS + 100; // If it is bigger then the calculation will be more accurate.
  public static final BigDecimal HUNDRED = new BigDecimal("100.00");
  public static final MathContext MATH_CONTEXT = new MathContext(DECIMAL_FORMAT_FRACTION_DIGITS + 2,
      RoundingMode.DOWN);
  public static final MathContext MATH_CONTEXT_DIFF = new MathContext(
      DECIMAL_FORMAT_DIFFERENCE_FRACTION_DIGITS + 2, RoundingMode.HALF_UP);
}
