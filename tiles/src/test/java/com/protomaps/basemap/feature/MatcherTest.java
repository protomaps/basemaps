package com.protomaps.basemap.feature;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static com.onthegomap.planetiler.TestUtils.newPoint;
import static com.onthegomap.planetiler.TestUtils.newPolygon;
import static com.protomaps.basemap.feature.Matcher.fromTag;
import static com.protomaps.basemap.feature.Matcher.getBoolean;
import static com.protomaps.basemap.feature.Matcher.getDouble;
import static com.protomaps.basemap.feature.Matcher.getInteger;
import static com.protomaps.basemap.feature.Matcher.getString;
import static com.protomaps.basemap.feature.Matcher.rule;
import static com.protomaps.basemap.feature.Matcher.use;
import static com.protomaps.basemap.feature.Matcher.with;
import static com.protomaps.basemap.feature.Matcher.withAllOf;
import static com.protomaps.basemap.feature.Matcher.withAnyOf;
import static com.protomaps.basemap.feature.Matcher.withLine;
import static com.protomaps.basemap.feature.Matcher.withPoint;
import static com.protomaps.basemap.feature.Matcher.withPolygon;
import static com.protomaps.basemap.feature.Matcher.without;
import static com.protomaps.basemap.feature.Matcher.withoutLine;
import static com.protomaps.basemap.feature.Matcher.withoutPoint;
import static com.protomaps.basemap.feature.Matcher.withoutPolygon;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthegomap.planetiler.expression.Expression;
import com.onthegomap.planetiler.expression.MultiExpression;
import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MatcherTest {
  @Test
  void testWith() {
    assertEquals(Expression.TRUE, with());

    assertEquals(Expression.matchField("a"), with("a"));

    assertEquals(Expression.matchAny("a", "b"),
      with("a", "b"));

    assertEquals(Expression.matchAny("a", "b", "c"),
      with("a", "b", "c"));

    assertEquals(Expression.matchAny("a", "b", "c"),
      with("""
          a
          b
          c
        """));
  }

  @Test
  void testWithout() {
    assertEquals(Expression.not(Expression.TRUE), without());

    assertEquals(Expression.not(Expression.matchField("a")), without("a"));

    assertEquals(Expression.not(Expression.matchAny("a", "b")),
      without("a", "b"));

    assertEquals(Expression.not(Expression.matchAny("a", "b", "c")),
      without("a", "b", "c"));
  }

  @Test
  void testWithAnyOfEmpty() {
    assertEquals(Expression.FALSE, withAnyOf());
  }

  @Test
  void testWithAnyOfSingle() {
    assertEquals(Expression.matchField("a"), withAnyOf(with("a")));
  }

  @Test
  void testWithAnyOfMultiple() {
    assertEquals(Expression.or(Expression.matchField("a"), Expression.matchField("b")),
      withAnyOf(with("a"), with("b")));

    assertEquals(Expression.or(Expression.matchField("a"), Expression.matchField("b"), Expression.matchField("c")),
      withAnyOf(with("a"), with("b"), with("c")));
  }

  @Test
  void testWithAnyOfMatching() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        withAnyOf(with("a", "b"), with("c", "d")),
        use("result", "matched")
      )
    )).index();

    // Test first condition matches (a=b)
    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "b"),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("matched", getString(sf, matches, "result", "default"));

    // Test second condition matches (c=d)
    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("c", "d"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("matched", getString(sf, matches, "result", "default"));

    // Test both conditions match
    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "b", "c", "d"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("matched", getString(sf, matches, "result", "default"));

    // Test neither condition matches
    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("e", "f"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("default", getString(sf, matches, "result", "default"));
  }

  @Test
  void testWithAnyOfNoneMatch() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        withAnyOf(with("a", "b"), with("c", "d"), with("e", "f")),
        use("result", "matched")
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("x", "y"),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("default", getString(sf, matches, "result", "default"));
  }

  @Test
  void testWithAnyOfGeometryTypes() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        withAnyOf(withPoint(), withLine()),
        use("result", "point_or_line")
      ),
      rule(
        withPolygon(),
        use("result", "polygon")
      )
    )).index();

    // Test point matches
    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("point_or_line", getString(sf, matches, "result", "default"));

    // Test line matches
    sf = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of(),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("point_or_line", getString(sf, matches, "result", "default"));

    // Test polygon doesn't match first rule but matches second
    sf = SimpleFeature.create(
      newPolygon(0, 0, 1, 1, 2, 2, 0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("polygon", getString(sf, matches, "result", "default"));
  }

  @Test
  void testWithAllOfEmpty() {
    assertEquals(Expression.TRUE, withAllOf());
  }

  @Test
  void testWithAllOfSingle() {
    assertEquals(Expression.matchField("a"), withAllOf(with("a")));
  }

  @Test
  void testWithAllOfMultiple() {
    assertEquals(Expression.and(Expression.matchField("a"), Expression.matchField("b")),
      withAllOf(with("a"), with("b")));

    assertEquals(Expression.and(Expression.matchField("a"), Expression.matchField("b"), Expression.matchField("c")),
      withAllOf(with("a"), with("b"), with("c")));
  }

  @Test
  void testWithAllOfMatching() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        withAllOf(with("a", "b"), with("c", "d")),
        use("result", "matched")
      )
    )).index();

    // Test first condition matches but not second (a=b, no c)
    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "b"),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("default", getString(sf, matches, "result", "default"));

    // Test second condition matches but not first (c=d, no a)
    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("c", "d"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("default", getString(sf, matches, "result", "default"));

    // Test both conditions match (a=b AND c=d)
    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "b", "c", "d"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("matched", getString(sf, matches, "result", "default"));

    // Test neither condition matches
    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("e", "f"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("default", getString(sf, matches, "result", "default"));
  }

  @Test
  void testWithAllOfPartialMatch() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        withAllOf(with("a", "b"), with("c", "d"), with("e", "f")),
        use("result", "matched")
      )
    )).index();

    // Test only one condition matches
    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "b"),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("default", getString(sf, matches, "result", "default"));

    // Test two conditions match but not all three
    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "b", "c", "d"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("default", getString(sf, matches, "result", "default"));

    // Test all three conditions match
    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "b", "c", "d", "e", "f"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("matched", getString(sf, matches, "result", "default"));
  }

  @Test
  void testWithAllOfGeometryTypes() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        withAllOf(withPoint(), with("name")),
        use("result", "named_point")
      )
    )).index();

    // Test point without name (should not match)
    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("default", getString(sf, matches, "result", "default"));

    // Test point with name (should match)
    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("name", "Test Point"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("named_point", getString(sf, matches, "result", "default"));

    // Test line with name (should not match - wrong geometry type)
    sf = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of("name", "Test Line"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("default", getString(sf, matches, "result", "default"));

    // Test polygon with name (should not match - wrong geometry type)
    sf = SimpleFeature.create(
      newPolygon(0, 0, 1, 1, 2, 2, 0, 0),
      Map.of("name", "Test Polygon"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("default", getString(sf, matches, "result", "default"));
  }

  @Test
  void testWithPoint() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        withPoint(),
        use("a", "b")
      ),
      rule(
        withoutPoint(),
        use("a", "c")
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);

    assertEquals("b", getString(sf, matches, "a", "d"));

    sf = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of(),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);

    assertEquals("c", getString(sf, matches, "a", "d"));

    sf = SimpleFeature.create(
      newPolygon(0, 0, 1, 1, 2, 2, 0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);

    assertEquals("c", getString(sf, matches, "a", "d"));
  }

  @Test
  void testWithLine() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        withLine(),
        use("a", "b")
      ),
      rule(
        withoutLine(),
        use("a", "c")
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);

    assertEquals("c", getString(sf, matches, "a", "d"));

    sf = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of(),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);

    assertEquals("b", getString(sf, matches, "a", "d"));

    sf = SimpleFeature.create(
      newPolygon(0, 0, 1, 1, 2, 2, 0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);

    assertEquals("c", getString(sf, matches, "a", "d"));
  }

  @Test
  void testWithPolygon() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        withPolygon(),
        use("a", "b")
      ),
      rule(
        withoutPolygon(),
        use("a", "c")
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);

    assertEquals("c", getString(sf, matches, "a", "d"));

    sf = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of(),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);

    assertEquals("c", getString(sf, matches, "a", "d"));

    sf = SimpleFeature.create(
      newPolygon(0, 0, 1, 1, 2, 2, 0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);

    assertEquals("b", getString(sf, matches, "a", "d"));
  }

  @Test
  void testRule() {
    assertEquals(MultiExpression.entry(Map.of(), Expression.and(Expression.TRUE)),
      rule(with()));

    assertEquals(MultiExpression.entry(Map.of(), Expression.and(Expression.TRUE, Expression.TRUE)),
      rule(with(), with()));

    assertEquals(MultiExpression.entry(Map.of("a", "b"), Expression.and(Expression.TRUE)),
      rule(use("a", "b"), with()));

    assertEquals(MultiExpression.entry(Map.of("a", "b"), Expression.and(Expression.TRUE)),
      rule(with(), use("a", "b")));

    assertEquals(MultiExpression.entry(Map.of("a", 1), Expression.and(Expression.TRUE)),
      rule(with(), use("a", 1)));

    assertEquals(MultiExpression.entry(Map.of("a", 1.5), Expression.and(Expression.TRUE)),
      rule(with(), use("a", 1.5)));

    assertEquals(MultiExpression.entry(Map.of("a", true), Expression.and(Expression.TRUE)),
      rule(with(), use("a", true)));

    assertEquals(
      MultiExpression.entry(Map.of("a", "b", "c", "d"), Expression.and(Expression.TRUE)),
      rule(use("a", "b"), use("c", "d"), with()));
  }

  @Test
  void testGetStringWith() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with(),
        use("a", "b")
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);

    assertEquals("b", getString(sf, matches, "a", "c"));
    assertEquals("c", getString(sf, matches, "z", "c"));
    assertEquals(null, getString(sf, matches, "z", null));
  }

  @Test
  void testGetStringWithA() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with("a"),
        use("b", "c")
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("d", getString(sf, matches, "b", "d"));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "something"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("c", getString(sf, matches, "b", "d"));
  }

  @Test
  void testGetStringWithNull() {
    var index = MultiExpression.of(List.of(
      rule(
        with("a"),
        use("b", null)
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "something"),
      "osm",
      null,
      0
    );

    var matches = index.getMatches(sf);
    assertEquals("d", getString(sf, matches, "b", "d"));
  }

  @Test
  void testGetStringWithAB() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with("a", "b"),
        use("c", "d")
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "b"),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("d", getString(sf, matches, "c", "e"));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "something"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("e", getString(sf, matches, "c", "e"));
  }

  @Test
  void testGetStringWithoutAB() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        without("a", "b"),
        use("c", "d")
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "b"),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("e", getString(sf, matches, "c", "e"));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "something"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("d", getString(sf, matches, "c", "e"));
  }

  @Test
  void testGetStringWithABC() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with("a", "b", "c"),
        use("d", "e")
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "b"),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("e", getString(sf, matches, "d", "f"));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "c"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("e", getString(sf, matches, "d", "f"));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("a", "something"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("f", getString(sf, matches, "d", "f"));
  }

  @Test
  void testGetStringMultipleUse() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with(),
        use("a", "b"),
        use("c", "d")
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("b", getString(sf, matches, "a", "e"));
    assertEquals("d", getString(sf, matches, "c", "e"));
  }

  @Test
  void testGetStringLastRule() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with(),
        use("a", "b")
      ),
      rule(
        with(),
        use("a", "c")
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("c", getString(sf, matches, "a", "d"));
  }

  @Test
  void testGetStringTypeMismatch() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with(),
        use("a", 1)
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("b", getString(sf, matches, "a", "b"));
  }

  @Test
  void testGetStringFromTag() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with(),
        use("a", fromTag("b"))
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals("c", getString(sf, matches, "a", "c"));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("b", "d"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals("d", getString(sf, matches, "a", "c"));
  }

  @Test
  void testGetInteger() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with(),
        use("a", 1)
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals(1, getInteger(sf, matches, "a", 2));
  }

  @Test
  void testGetIntegerFromTag() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with(),
        use("a", fromTag("b"))
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals(1, getInteger(sf, matches, "a", 1));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("b", "2"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals(2, getInteger(sf, matches, "a", 1));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("b", "hello"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals(1, getInteger(sf, matches, "a", 1));
  }

  @Test
  void testGetDouble() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with(),
        use("a", 1.5)
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals(1.5, getDouble(sf, matches, "a", 2.5));
  }

  @Test
  void testGetDoubleFromTag() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with(),
        use("a", fromTag("b"))
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals(1.5, getDouble(sf, matches, "a", 1.5));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("b", "2.5"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals(2.5, getDouble(sf, matches, "a", 1.5));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("b", "hello"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals(1.5, getDouble(sf, matches, "a", 1.5));
  }

  @Test
  void testGetBoolean() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with(),
        use("a", true)
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals(true, getBoolean(sf, matches, "a", false));
  }

  @Test
  void testGetBooleanFromTag() {
    var index = MultiExpression.ofOrdered(List.of(
      rule(
        with(),
        use("a", fromTag("b"))
      )
    )).index();

    var sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of(),
      "osm",
      null,
      0
    );
    var matches = index.getMatches(sf);
    assertEquals(false, getBoolean(sf, matches, "a", false));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("b", "true"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals(true, getBoolean(sf, matches, "a", false));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("b", "yes"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals(true, getBoolean(sf, matches, "a", false));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("b", "no"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals(false, getBoolean(sf, matches, "a", true));

    sf = SimpleFeature.create(
      newPoint(0, 0),
      Map.of("b", "1"),
      "osm",
      null,
      0
    );
    matches = index.getMatches(sf);
    assertEquals(true, getBoolean(sf, matches, "a", false));
  }

}
