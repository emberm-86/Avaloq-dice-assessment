package com.assignment.dicegame;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Constants {
  public static final Integer DECIMAL_FORMAT_FRACTION_DIGITS = 2;
  public static final Integer DECIMAL_FORMAT_DIFFERENCE_FRACTION_DIGITS = 3;
  public static final BigDecimal NIL = new BigDecimal("0.00");
  public static final BigDecimal HUNDRED = new BigDecimal("100.00");
  public static final BigDecimal STEP = new BigDecimal("0.01");
  public static final MathContext MATH_CONTEXT = new MathContext(4, RoundingMode.DOWN);
  public static final MathContext MATH_CONTEXT_DIFF = new MathContext(5, RoundingMode.HALF_UP);
}
