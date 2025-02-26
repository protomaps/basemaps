package com.protomaps.basemap.feature;

import static com.onthegomap.planetiler.TestUtils.newPoint;
import static com.protomaps.basemap.feature.Matcher.rule;
import static com.protomaps.basemap.feature.Matcher.use;
import static com.protomaps.basemap.feature.Matcher.with;
import static com.protomaps.basemap.feature.Matcher.without;
import static com.protomaps.basemap.feature.Matcher.getString;
import static com.protomaps.basemap.feature.Matcher.getInteger;
import static com.protomaps.basemap.feature.Matcher.getDouble;
import static com.protomaps.basemap.feature.Matcher.getBoolean;
import static com.protomaps.basemap.feature.Matcher.fromTag;
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
    var index = MultiExpression.of(List.of(
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
    var index = MultiExpression.of(List.of(
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
  void testGetStringWithAB() {
    var index = MultiExpression.of(List.of(
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
    var index = MultiExpression.of(List.of(
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
    var index = MultiExpression.of(List.of(
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
    var index = MultiExpression.of(List.of(
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
    var index = MultiExpression.of(List.of(
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
    var index = MultiExpression.of(List.of(
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
    var index = MultiExpression.of(List.of(
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
    var index = MultiExpression.of(List.of(
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
    var index = MultiExpression.of(List.of(
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
    var index = MultiExpression.of(List.of(
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
    var index = MultiExpression.of(List.of(
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
    var index = MultiExpression.of(List.of(
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
    var index = MultiExpression.of(List.of(
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
