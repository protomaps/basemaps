package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.expression.Expression;
import com.onthegomap.planetiler.expression.MultiExpression;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.geo.GeometryType;
import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

/**
 * A utility class for matching source feature properties to values.
 *
 * <p>
 * Use the {@link #rule} function to create entries for a Planetiler {@link MultiExpression}. A rule consists of
 * multiple contitions that get joined by a logical AND, and key-value pairs that should be used if all conditions of
 * the rule are true. The key-value pairs of rules that get added later override the key-value pairs of rules that were
 * added earlier.
 * </p>
 *
 * <p>
 * The MultiExpression can be used on a source feature and the resulting list of matches can be used in
 * {@link #getString} and similar functions to retrieve a value.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * <code>
 *var index = MultiExpression.ofOrdered(List.of(rule(with("highway", "primary"), use("kind", "major_road")))).index();
 *var matches = index.getMatches(sourceFeature);
 *String kind = getString(sourceFeature, matches, "kind", "other");
 * </code>
 * </pre>
 */
public class Matcher {
  public record Use(String key, Object value) {}

  /**
   * Creates a matching rule with conditions and values.
   *
   * <p>
   * Create conditions by calling the {@link #with} or {@link #without} functions. All conditions are joined by a
   * logical AND.
   * </p>
   *
   * <p>
   * Create key-value pairs with the {@link #use} function.
   * </p>
   *
   * @param arguments A mix of {@link Use} instances for key-value pairs and {@link Expression} instances for
   *                  conditions.
   * @return A {@link MultiExpression.Entry} containing the rule definition.
   */
  public static MultiExpression.Entry<Map<String, Object>> rule(Object... arguments) {
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

  /**
   * Creates a {@link Use} instance representing a key-value pair to be supplied to the {@link #rule} function.
   *
   * <p>
   * While in principle any Object can be supplied as value, retrievalbe later on are only Strings with
   * {@link #getString}, Integers with {@link #getInteger}, Doubles with {@link #getDouble}, Booleans with
   * {@link #getBoolean}.
   * </p>
   *
   * @param key   The key.
   * @param value The value associated with the key.
   * @return A new {@link Use} instance.
   */
  public static Use use(String key, Object value) {
    return new Use(key, value);
  }

  /**
   * Creates an {@link Expression} that matches any of the specified arguments.
   *
   * <p>
   * If no argument is supplied, matches everything.
   * </p>
   *
   * <p>
   * If one argument is supplied, matches all source features that have this tag, e.g., {@code with("highway")} matches
   * to all source features with a highway tag.
   * </p>
   *
   * <p>
   * If two arguments are supplied, matches to all source features that have this tag-value pair, e.g.,
   * {@code with("highway", "primary")} matches to all source features with highway=primary.
   * </p>
   *
   * <p>
   * If more than two arguments are supplied, matches to all source features that have the first argument as tag and the
   * later arguments as possible values, e.g., {@code with("highway", "primary", "secondary")} matches to all source
   * features that have highway=primary or highway=secondary.
   * </p>
   *
   * <p>
   * If an argument consists of multiple lines, it will be broken up into one argument per line. Example:
   *
   * <pre>
   * <code>
   * with("""
   *   highway
   *   primary
   *   secondary
   * """)
   * </code>
   * </pre>
   * </p>
   *
   * @param arguments Field names to match.
   * @return An {@link Expression} for the given field names.
   */
  public static Expression with(String... arguments) {

    List<String> argumentList = Arrays.stream(arguments)
      .flatMap(String::lines)
      .map(String::trim)
      .filter(line -> !line.isBlank())
      .toList();

    if (argumentList.isEmpty()) {
      return Expression.TRUE;
    } else if (argumentList.size() == 1) {
      return Expression.matchField(argumentList.getFirst());
    }
    return Expression.matchAny(argumentList.getFirst(), argumentList.subList(1, argumentList.size()));
  }

  /**
   * Same as {@link #with}, but negated.
   */
  public static Expression without(String... arguments) {
    return Expression.not(with(arguments));
  }

  /**
   * Creates an {@link Expression} that matches when a numeric tag value is within a specified range.
   *
   * <p>
   * The lower bound is inclusive. The upper bound, if provided, is exclusive.
   * </p>
   *
   * <p>
   * Tag values that cannot be parsed as numbers or missing tags will not match.
   * </p>
   *
   * @param tagName    The name of the tag to check.
   * @param lowerBound The inclusive lower bound.
   * @param upperBound The exclusive upper bound.
   * @return An {@link Expression} for the numeric range check.
   */
  public static Expression withinRange(String tagName, Integer lowerBound, Integer upperBound) {
    return new WithinRangeExpression(tagName, Long.valueOf(lowerBound), Long.valueOf(upperBound));
  }

  /**
   * Overload withinRange to accept lower bound integer and upper bound double
   */
  public static Expression withinRange(String tagName, Integer lowerBound, Double upperBound) {
    return new WithinRangeExpression(tagName, Long.valueOf(lowerBound), upperBound.longValue());
  }

  /**
   * Overload withinRange to accept bounds as doubles
   */
  public static Expression withinRange(String tagName, Double lowerBound, Double upperBound) {
    return new WithinRangeExpression(tagName, lowerBound.longValue(), upperBound.longValue());
  }

  /**
   * Creates an {@link Expression} that matches when a numeric tag value is greater or equal to a value.
   *
   * <p>
   * Tag values that cannot be parsed as numbers or missing tags will not match.
   * </p>
   *
   * @param tagName    The name of the tag to check.
   * @param lowerBound The inclusive lower bound.
   * @return An {@link Expression} for the numeric range check.
   */
  public static Expression atLeast(String tagName, Integer lowerBound) {
    return new WithinRangeExpression(tagName, Long.valueOf(lowerBound), null);
  }

  /**
   * Overload atLeast to accept just lower bound double
   */
  public static Expression atLeast(String tagName, Double lowerBound) {
    return new WithinRangeExpression(tagName, lowerBound.longValue(), null);
  }

  /**
   * Expression implementation for numeric range matching.
   */
  private record WithinRangeExpression(String tagName, long lowerBound, Long upperBound) implements Expression {

    @Override
    public boolean evaluate(com.onthegomap.planetiler.reader.WithTags input, List<String> matchKeys) {
      if (!input.hasTag(tagName)) {
        return false;
      }
      long value = input.getLong(tagName);
      // getLong returns 0 for invalid values, so we need to check if 0 is actually the tag value
      if (value == 0 && !"0".equals(input.getString(tagName))) {
        // getLong returned 0 because parsing failed
        return false;
      }
      return value >= lowerBound && (upperBound == null || value < upperBound);
    }
  }

  public static Expression withPoint() {
    return Expression.matchGeometryType(GeometryType.POINT);
  }

  public static Expression withoutPoint() {
    return Expression.not(withPoint());
  }

  public static Expression withLine() {
    return Expression.matchGeometryType(GeometryType.LINE);
  }

  public static Expression withoutLine() {
    return Expression.not(withLine());
  }

  public static Expression withPolygon() {
    return Expression.matchGeometryType(GeometryType.POLYGON);
  }

  public static Expression withoutPolygon() {
    return Expression.not(withPolygon());
  }

  public record FromTag(String key) {}

  /**
   * Creates a {@link FromTag} instance representing a tag reference.
   *
   * <p>
   * Use this function if to retrieve a value from a source feature when calling {@link #getString} and similar.
   * </p>
   *
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>
   * <code>
   *var index = MultiExpression.ofOrdered(List.of(rule(with("highway", "primary", "secondary"), use("kind", fromTag("highway"))))).index();
   *var matches = index.getMatches(sourceFeature);
   *String kind = getString(sourceFeature, matches, "kind", "other");
   * </code>
   * </pre>
   * <p>
   * On a source feature with highway=primary the above will result in kind=primary.
   *
   * @param key The key of the tag.
   * @return A new {@link FromTag} instance.
   */
  public static FromTag fromTag(String key) {
    return new FromTag(key);
  }

  public static String getString(SourceFeature sf, List<Map<String, Object>> matches, String key, String defaultValue) {
    for (var match : matches.reversed()) {
      if (match.containsKey(key)) {
        Object value = match.get(key);
        if (value instanceof String stringValue) {
          return stringValue;
        } else if (value instanceof FromTag fromTag) {
          return sf.getString(fromTag.key, defaultValue);
        } else {
          return defaultValue;
        }
      }
    }
    return defaultValue;
  }

  public static Integer getInteger(SourceFeature sf, List<Map<String, Object>> matches, String key,
    Integer defaultValue) {
    for (var match : matches.reversed()) {
      if (match.containsKey(key)) {
        Object value = match.get(key);
        if (value instanceof Integer integerValue) {
          return integerValue;
        } else if (value instanceof FromTag fromTag) {
          try {
            return sf.hasTag(fromTag.key) ? Integer.valueOf(sf.getString(fromTag.key)) : defaultValue;
          } catch (NumberFormatException e) {
            return defaultValue;
          }
        } else {
          return defaultValue;
        }
      }
    }
    return defaultValue;
  }

  public static Double getDouble(SourceFeature sf, List<Map<String, Object>> matches, String key, Double defaultValue) {
    for (var match : matches.reversed()) {
      if (match.containsKey(key)) {
        Object value = match.get(key);
        if (value instanceof Double doubleValue) {
          return doubleValue;
        } else if (value instanceof FromTag fromTag) {
          try {
            return sf.hasTag(fromTag.key) ? Double.valueOf(sf.getString(fromTag.key)) : defaultValue;
          } catch (NumberFormatException e) {
            return defaultValue;
          }
        } else {
          return defaultValue;
        }
      }
    }
    return defaultValue;
  }

  public static Boolean getBoolean(SourceFeature sf, List<Map<String, Object>> matches, String key,
    Boolean defaultValue) {
    for (var match : matches.reversed()) {
      if (match.containsKey(key)) {
        Object value = match.get(key);
        if (value instanceof Boolean booleanValue) {
          return booleanValue;
        } else if (value instanceof FromTag fromTag) {
          return sf.hasTag(fromTag.key) ? sf.getBoolean(fromTag.key) : defaultValue;
        } else {
          return defaultValue;
        }
      }
    }
    return defaultValue;
  }

  /**
   * Wrapper that combines a SourceFeature with computed tags without mutating the original. This allows MultiExpression
   * matching to access both original and computed tags.
   *
   * <p>
   * This is useful when you need to add computed tags (like area calculations or derived properties) that should be
   * accessible to MultiExpression rules, but the original SourceFeature has immutable tags.
   * </p>
   */
  public static class SourceFeatureWithComputedTags extends SourceFeature {
    private final SourceFeature delegate;
    private final Map<String, Object> combinedTags;

    /**
     * Creates a wrapper around a SourceFeature with additional computed tags.
     *
     * @param delegate     The original SourceFeature to wrap
     * @param computedTags Additional computed tags to merge with the original tags
     */
    public SourceFeatureWithComputedTags(SourceFeature delegate, Map<String, Object> computedTags) {
      super(new HashMap<>(delegate.tags()), delegate.getSource(), delegate.getSourceLayer(), null, delegate.id());
      this.delegate = delegate;
      this.combinedTags = new HashMap<>(delegate.tags());
      this.combinedTags.putAll(computedTags);
    }

    @Override
    public Map<String, Object> tags() {
      return combinedTags;
    }

    @Override
    public Geometry worldGeometry() throws GeometryException {
      return delegate.worldGeometry();
    }

    @Override
    public Geometry latLonGeometry() throws GeometryException {
      return delegate.latLonGeometry();
    }

    @Override
    public boolean isPoint() {
      return delegate.isPoint();
    }

    @Override
    public boolean canBePolygon() {
      return delegate.canBePolygon();
    }

    @Override
    public boolean canBeLine() {
      return delegate.canBeLine();
    }

    @Override
    public boolean hasRelationInfo() {
      return delegate.hasRelationInfo();
    }
  }

}
