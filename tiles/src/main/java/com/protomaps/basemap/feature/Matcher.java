package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.expression.Expression;
import com.onthegomap.planetiler.expression.MultiExpression;
import com.onthegomap.planetiler.reader.SourceFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Matcher {

  public record Use(String key, Object value) {}

  public static MultiExpression.Entry<Map<String, Object>> entry(Object... arguments) {
    Map<String, Object> result = new HashMap<>();
    List<Expression> conditions = new ArrayList<>();
    for (Object argument : arguments) {
      if (argument instanceof Use use) {
        result.put(use.key, use.value);
      } else if (argument instanceof Expression condition) {
        conditions.add(condition);
      }
    }
    return MultiExpression.entry(result, Expression.and(conditions));
  }

  public static Use use(String key, Object value) {
    return new Use(key, value);
  }

  public static Expression with(String... arguments) {
    List<String> argumentList = List.of(arguments);
    if (argumentList.isEmpty()) {
        return Expression.TRUE;
    }
    else if (argumentList.size() == 1) {
        return Expression.matchField(argumentList.getFirst());
    }
    return Expression.matchAny(argumentList.getFirst(), argumentList.subList(1, argumentList.size()));
  }

  public static Expression without(String... arguments) {
    return Expression.not(with(arguments));
  }

  public record FromTag(String key) {}
  public static FromTag fromTag(String key) {
    return new FromTag(key);
  }

  public static String getString(SourceFeature sf, List<Map<String, Object>> matches, String key, String defaultValue) {
    for (var match : matches.reversed()) {
        if (match.containsKey(key)) {
            Object value = match.get(key);
            if (value instanceof String stringValue) {
                return stringValue;
            }
            else if (value instanceof FromTag fromTag) {
                return sf.getString(fromTag.key);
            }
            else {
                return defaultValue;
            }
        }
    }
    return defaultValue;
  }

  public static Integer getInteger(SourceFeature sf, List<Map<String, Object>> matches, String key, Integer defaultValue) {
    for (var match : matches.reversed()) {
        if (match.containsKey(key)) {
            Object value = match.get(key);
            if (value instanceof Integer integerValue) {
                return integerValue;
            }
            else if (value instanceof FromTag fromTag) {
                return Integer.valueOf(sf.getString(fromTag.key, String.valueOf(defaultValue)));
            }
            else {
                return defaultValue;
            }
        }
    }
    return defaultValue;
  }

  public static Boolean getBoolean(SourceFeature sf, List<Map<String, Object>> matches, String key, Boolean defaultValue) {
    for (var match : matches.reversed()) {
        if (match.containsKey(key)) {
            Object value = match.get(key);
            if (value instanceof Boolean booleanValue) {
                return booleanValue;
            }
            else if (value instanceof FromTag fromTag) {
                return sf.hasTag(fromTag.key) ? sf.getBoolean(fromTag.key) : defaultValue;
            }
            else {
                return defaultValue;
            }
        }
    }
    return defaultValue;
  }

  public static void main() {
    use("hi", 15);
    use("some", "thing");
  }
}
