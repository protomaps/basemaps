package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.reader.SourceFeature;

public class RegionNameZooms {

    public static float[] getMinMaxZooms(SourceFeature sf) {
        float min_zoom = 8.0f;   // default for all regions
        float max_zoom = 11.0f;  // default for all regions

        try {
            var name = sf.getString("name:en") == null ? sf.getString("name") : sf.getString("name:en");

            if (name != null) {
                switch (name) {
                    // United States
                    case "Alabama" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Alaska" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Arizona" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Arkansas" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "California" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Colorado" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Connecticut" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Delaware" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Florida" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Georgia" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Hawaii" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Idaho" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Illinois" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Indiana" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Iowa" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Kansas" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Kentucky" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Louisiana" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Maine" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Maryland" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Massachusetts" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Michigan" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Minnesota" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Mississippi" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Missouri" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Montana" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Nebraska" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Nevada" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "New Hampshire" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "New Jersey" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "New Mexico" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "New York" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "North Carolina" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "North Dakota" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Ohio" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Oklahoma" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Oregon" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Pennsylvania" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Rhode Island" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "South Carolina" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "South Dakota" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Tennessee" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Texas" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Utah" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Vermont" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Virginia" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Washington" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "West Virginia" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Wisconsin" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Wyoming" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }

                    // Canada
                    case "Alberta" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "British Columbia" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Manitoba" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "New Brunswick" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Newfoundland and Labrador" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Northwest Territories" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Nova Scotia" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Nunavut" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Ontario" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Prince Edward Island" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Quebec" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Saskatchewan" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }
                    case "Yukon" -> {
                        min_zoom = 3.5f;
                        max_zoom = 7.5f;
                    }

                    // Australia
                    case "New South Wales" -> {
                        min_zoom = 4.6f;
                        max_zoom = 8.1f;
                    }
                    case "Queensland" -> {
                        min_zoom = 4.6f;
                        max_zoom = 8.1f;
                    }
                    case "South Australia" -> {
                        min_zoom = 4.6f;
                        max_zoom = 8.1f;
                    }
                    case "Tasmania" -> {
                        min_zoom = 4.6f;
                        max_zoom = 8.1f;
                    }
                    case "Victoria" -> {
                        min_zoom = 4.6f;
                        max_zoom = 8.1f;
                    }
                    case "Western Australia" -> {
                        min_zoom = 4.6f;
                        max_zoom = 8.1f;
                    }
                    case "Australian Capital Territory" -> {
                        min_zoom = 4.6f;
                        max_zoom = 8.1f;
                    }
                    case "Northern Territory" -> {
                        min_zoom = 4.6f;
                        max_zoom = 8.1f;
                    }
                }
            }
        } catch (Exception e) {
        }

        float[] zoomArray = new float[2];
        zoomArray[0] = min_zoom - 1;
        zoomArray[1] = max_zoom - 1;

        return zoomArray;
    }
}
