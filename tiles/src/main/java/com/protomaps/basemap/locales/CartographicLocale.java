package com.protomaps.basemap.locales;

import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

/*
 * Encapsulates country-specific logic applied to OpenStreetMap tags.
 * <p>
 * This is the per-country extension point for cartographic logic that depends on a spatial
 * join of input features to country polygons: shield network priority today, and a natural
 * home for things like admin_level normalization or road reclassification in the future.
 * CartographicLocale is the parent class that applies to locales outside of any polygon,
 * locales that are unimplemented, or default behavior when a locale does not override a method.
 *
 * Each implemented locale is named by 2-letter ISO code.
 */
public class CartographicLocale {

  public record Shield(String text, String network) {}

  /** Maximum number of concurrent shields emitted per road (network_1..network_N). */
  public static final int MAX_SHIELDS = 6;

  /** Rank assigned to networks a locale does not explicitly prioritize. */
  protected static final int DEFAULT_RANK = 1000;

  protected String strip(String s) {
    if (s != null) {
      return s.replaceAll("\\s", "");
    }
    return null;
  }

  /**
   * Normalize a raw OSM route network to the network used for shield symbolization, priority and minzoom. The base
   * implementation returns the network unchanged; locales collapse variants that share a base route's shield (e.g.
   * carriageway suffixes) onto it.
   */
  public String normalizeNetwork(String network) {
    return network;
  }

  /**
   * Priority of a route network when ordering the concurrent shields on a single road. Lower rank sorts earlier, so it
   * becomes a lower shield index (network_1 is the primary shield). Priority is a national convention, so the base
   * implementation treats every network equally and leaves ordering to the ref tiebreak.
   */
  public int networkRank(String network) {
    return DEFAULT_RANK;
  }

  /**
   * Normalize, de-duplicate, prioritize and cap a road's concurrent shields.
   * <p>
   * Input order is not significant: directional (forward/backward) route relations produce duplicate (network, ref)
   * pairs, and {@code SourceFeature.relationInfo()} ordering is not stable across builds, so the deterministic ordering
   * here comes entirely from {@link #networkRank(String)} with the shield text as a tiebreak.
   */
  public List<Shield> orderShields(List<Shield> shields) {
    List<Shield> normalized = new ArrayList<>();
    for (Shield s : shields) {
      String text = strip(s.text());
      if (text != null) {
        normalized.add(new Shield(text, s.network()));
      }
    }

    List<Shield> deduped = new ArrayList<>(new LinkedHashSet<>(normalized));
    deduped.sort(
      Comparator.comparingInt((Shield s) -> networkRank(s.network()))
        .thenComparing(Shield::text));

    if (deduped.size() > MAX_SHIELDS) {
      return new ArrayList<>(deduped.subList(0, MAX_SHIELDS));
    }
    return deduped;
  }

  /**
   * Generic shield derived from the way's own {@code ref} tag, used as a fallback when a road is not a member of any
   * route relation. The network is unknown on this path, so "other".
   */
  public Shield getShield(SourceFeature sf) {
    String ref = sf.getString("ref");
    if (ref != null) {
      String firstRef = ref.split(";")[0];
      return new Shield(strip(firstRef), "other");
    }
    return new Shield(null, null);
  }
}
