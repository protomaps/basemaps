package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.reader.SourceFeature;

public class CountryNameZooms {

    public static float[] getMinMaxZooms(SourceFeature feature) {
        float min_zoom = 8.0f;   // default for unrecognized countries
        float max_zoom = 11.0f;  // default for all countries

        switch ( feature.getString("name:en") ) {
            case "Andorra" -> {
                // AD country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "United Arab Emirates" -> {
                // AE country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Afghanistan" -> {
                // AF country code
                min_zoom =   6.7f;
                max_zoom =  11.0f;
            }
            case "Antigua and Barb" -> {
                // AG country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Anguilla" -> {
                // AI country code
                min_zoom =  11.5f;
                max_zoom =  11.5f;
            }
            case "Albania" -> {
                // AL country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Armenia" -> {
                // AM country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Angola" -> {
                // AO country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Antarctica" -> {
                // AQ country code
                min_zoom =  14.5f;
                max_zoom =  14.5f;
            }
            case "Argentina" -> {
                // AR country code
                min_zoom =   6.0f;
                max_zoom =  11.0f;
            }
            case "American Samoa" -> {
                // AS country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Austria" -> {
                // AT country code
                min_zoom =   7.8f;
                max_zoom =  11.0f;
            }
            case "Australia" -> {
                // AU country code
                min_zoom =   4.6f;
                max_zoom =   8.1f;
            }
            case "Aruba" -> {
                // AW country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Åland" -> {
                // AX country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Azerbaijan" -> {
                // AZ country code
                min_zoom =   9.2f;
                max_zoom =  11.0f;
            }
            case "Bosnia and Herz" -> {
                // BA country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Barbados" -> {
                // BB country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Bangladesh" -> {
                // BD country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Belgium" -> {
                // BE country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Burkina Faso" -> {
                // BF country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Bulgaria" -> {
                // BG country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Bahrain" -> {
                // BH country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Burundi" -> {
                // BI country code
                min_zoom =  10.0f;
                max_zoom =  11.0f;
            }
            case "Benin" -> {
                // BJ country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "St-Barthélemy" -> {
                // BL country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Bermuda" -> {
                // BM country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Brunei" -> {
                // BN country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Bolivia" -> {
                // BO country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Brazil" -> {
                // BR country code
                min_zoom =   3.7f;
                max_zoom =   8.5f;
            }
            case "Bahamas" -> {
                // BS country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Bhutan" -> {
                // BT country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Botswana" -> {
                // BW country code
                min_zoom =   6.0f;
                max_zoom =  11.0f;
            }
            case "Belarus" -> {
                // BY country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Belize" -> {
                // BZ country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Canada" -> {
                // CA country code
                min_zoom =   3.5f;
                max_zoom =   7.5f;
            }
            case "Dem. Rep. Congo" -> {
                // CD country code
                min_zoom =   6.0f;
                max_zoom =  11.0f;
            }
            case "Central African Rep" -> {
                // CF country code
                min_zoom =   7.0f;
                max_zoom =  11.0f;
            }
            case "Congo" -> {
                // CG country code
                min_zoom =   7.6f;
                max_zoom =  11.0f;
            }
            case "Switzerland" -> {
                // CH country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Côte d'Ivoire" -> {
                // CI country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Cook Is" -> {
                // CK country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Chile" -> {
                // CL country code
                min_zoom =   6.0f;
                max_zoom =  11.0f;
            }
            case "Cameroon" -> {
                // CM country code
                min_zoom =   6.7f;
                max_zoom =  11.0f;
            }
            case "China" -> {
                // CN country code
                min_zoom =   5.0f;
                max_zoom =  10.3f;
            }
            case "Colombia" -> {
                // CO country code
                min_zoom =   7.0f;
                max_zoom =  11.2f;
            }
            case "Costa Rica" -> {
                // CR country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Cuba" -> {
                // CU country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Cabo Verde" -> {
                // CV country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Curaçao" -> {
                // CW country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Cyprus" -> {
                // CY country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Czechia" -> {
                // CZ country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Germany" -> {
                // DE country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Djibouti" -> {
                // DJ country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Denmark" -> {
                // DK country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Dominica" -> {
                // DM country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Dominican Rep" -> {
                // DO country code
                min_zoom =  10.0f;
                max_zoom =  11.0f;
            }
            case "Algeria" -> {
                // DZ country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Ecuador" -> {
                // EC country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Estonia" -> {
                // EE country code
                min_zoom =   7.0f;
                max_zoom =  11.0f;
            }
            case "Egypt" -> {
                // EG country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "W. Sahara" -> {
                // EH country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Eritrea" -> {
                // ER country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Spain" -> {
                // ES country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Ethiopia" -> {
                // ET country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Finland" -> {
                // FI country code
                min_zoom =   6.7f;
                max_zoom =  11.0f;
            }
            case "Fiji" -> {
                // FJ country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Falkland Is" -> {
                // FK country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Micronesia" -> {
                // FM country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Faeroe Is" -> {
                // FO country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Gabon" -> {
                // GA country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "United Kingdom" -> {
                // GB country code
                min_zoom =  10.0f;
                max_zoom =  11.0f;
            }
            case "Grenada" -> {
                // GD country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Georgia" -> {
                // GE country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Guernsey" -> {
                // GG country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Ghana" -> {
                // GH country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Gibraltar" -> {
                // GI country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Greenland" -> {
                // GL country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Gambia" -> {
                // GM country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Guinea" -> {
                // GN country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Eq. Guinea" -> {
                // GQ country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Greece" -> {
                // GR country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "S. Geo. and the Is" -> {
                // GS country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Guatemala" -> {
                // GT country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Guam" -> {
                // GU country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Guinea-Bissau" -> {
                // GW country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Guyana" -> {
                // GY country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Hong Kong" -> {
                // HK country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Heard I. and McDonald Is" -> {
                // HM country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Honduras" -> {
                // HN country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Croatia" -> {
                // HR country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Haiti" -> {
                // HT country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Hungary" -> {
                // HU country code
                min_zoom =   8.5f;
                max_zoom =  11.0f;
            }
            case "Indonesia" -> {
                // ID country code
                min_zoom =   5.0f;
                max_zoom =  10.1f;
            }
            case "Ireland" -> {
                // IE country code
                min_zoom =   8.2f;
                max_zoom =  11.0f;
            }
            case "Israel" -> {
                // IL country code
                min_zoom =   8.4f;
                max_zoom =  11.0f;
            }
            case "Isle of Man" -> {
                // IM country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "India" -> {
                // IN country code
                min_zoom =   4.6f;
                max_zoom =  10.1f;
            }
            case "Br. Indian Ocean Ter" -> {
                // IO country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Iraq" -> {
                // IQ country code
                min_zoom =   6.7f;
                max_zoom =  11.0f;
            }
            case "Iran" -> {
                // IR country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Iceland" -> {
                // IS country code
                min_zoom =   6.0f;
                max_zoom =  11.0f;
            }
            case "Italy" -> {
                // IT country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Jersey" -> {
                // JE country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Jamaica" -> {
                // JM country code
                min_zoom =  10.0f;
                max_zoom =  11.0f;
            }
            case "Jordan" -> {
                // JO country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Japan" -> {
                // JP country code
                min_zoom =   7.0f;
                max_zoom =  11.0f;
            }
            case "Kenya" -> {
                // KE country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Kyrgyzstan" -> {
                // KG country code
                min_zoom =   6.7f;
                max_zoom =  11.0f;
            }
            case "Cambodia" -> {
                // KH country code
                min_zoom =   8.1f;
                max_zoom =  11.0f;
            }
            case "Kiribati" -> {
                // KI country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Comoros" -> {
                // KM country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "St. Kitts and Nevis" -> {
                // KN country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "North Korea" -> {
                // KP country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "South Korea" -> {
                // KR country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Kuwait" -> {
                // KW country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Cayman Is" -> {
                // KY country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Kazakhstan" -> {
                // KZ country code
                min_zoom =   6.0f;
                max_zoom =  11.0f;
            }
            case "Laos" -> {
                // LA country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Lebanon" -> {
                // LB country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Saint Lucia" -> {
                // LC country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Liechtenstein" -> {
                // LI country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Sri Lanka" -> {
                // LK country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Liberia" -> {
                // LR country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Lesotho" -> {
                // LS country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Lithuania" -> {
                // LT country code
                min_zoom =   7.0f;
                max_zoom =  11.0f;
            }
            case "Luxembourg" -> {
                // LU country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Latvia" -> {
                // LV country code
                min_zoom =  10.0f;
                max_zoom =  11.0f;
            }
            case "Libya" -> {
                // LY country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Morocco" -> {
                // MA country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Monaco" -> {
                // MC country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Moldova" -> {
                // MD country code
                min_zoom =  10.0f;
                max_zoom =  11.0f;
            }
            case "Montenegro" -> {
                // ME country code
                min_zoom =  10.0f;
                max_zoom =  11.0f;
            }
            case "St-Martin" -> {
                // MF country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Madagascar" -> {
                // MG country code
                min_zoom =   7.0f;
                max_zoom =  11.0f;
            }
            case "Marshall Is" -> {
                // MH country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Macedonia" -> {
                // MK country code
                min_zoom =  10.0f;
                max_zoom =  11.0f;
            }
            case "Mali" -> {
                // ML country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Myanmar" -> {
                // MM country code
                min_zoom =   7.0f;
                max_zoom =  11.0f;
            }
            case "Mongolia" -> {
                // MN country code
                min_zoom =   6.0f;
                max_zoom =  11.0f;
            }
            case "Macao" -> {
                // MO country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "N. Mariana Is" -> {
                // MP country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Mauritania" -> {
                // MR country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Montserrat" -> {
                // MS country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Malta" -> {
                // MT country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Mauritius" -> {
                // MU country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Maldives" -> {
                // MV country code
                min_zoom =  10.0f;
                max_zoom =  11.0f;
            }
            case "Malawi" -> {
                // MW country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Mexico" -> {
                // MX country code
                min_zoom =   6.9f;
                max_zoom =  11.2f;
            }
            case "Malaysia" -> {
                // MY country code
                min_zoom =   7.2f;
                max_zoom =  11.0f;
            }
            case "Mozambique" -> {
                // MZ country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Namibia" -> {
                // NA country code
                min_zoom =   6.0f;
                max_zoom =  11.0f;
            }
            case "New Caledonia" -> {
                // NC country code
                min_zoom =   6.7f;
                max_zoom =  11.0f;
            }
            case "Niger" -> {
                // NE country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Norfolk Island" -> {
                // NF country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Nigeria" -> {
                // NG country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Nicaragua" -> {
                // NI country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Netherlands" -> {
                // NL country code
                min_zoom =   8.6f;
                max_zoom =  11.0f;
            }
            case "Nepal" -> {
                // NP country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Nauru" -> {
                // NR country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Niue" -> {
                // NU country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "New Zealand" -> {
                // NZ country code
                min_zoom =   8.5f;
                max_zoom =  11.3f;
            }
            case "Oman" -> {
                // OM country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Panama" -> {
                // PA country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Peru" -> {
                // PE country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Fr. Polynesia" -> {
                // PF country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Papua New Guinea" -> {
                // PG country code
                min_zoom =   7.0f;
                max_zoom =  11.0f;
            }
            case "Philippines" -> {
                // PH country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Pakistan" -> {
                // PK country code
                min_zoom =   5.0f;
                max_zoom =  10.5f;
            }
            case "Poland" -> {
                // PL country code
                min_zoom =   6.7f;
                max_zoom =  11.0f;
            }
            case "St. Pierre and Miquelon" -> {
                // PM country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Pitcairn Is" -> {
                // PN country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Puerto Rico" -> {
                // PR country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Palestine" -> {
                // PS country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Portugal" -> {
                // PT country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Palau" -> {
                // PW country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Paraguay" -> {
                // PY country code
                min_zoom =   6.7f;
                max_zoom =  11.0f;
            }
            case "Qatar" -> {
                // QA country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Romania" -> {
                // RO country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Serbia" -> {
                // RS country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Russia" -> {
                // RU country code
                min_zoom =   5.0f;
                max_zoom =  10.2f;
            }
            case "Rwanda" -> {
                // RW country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Saudi Arabia" -> {
                // SA country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Solomon Is" -> {
                // SB country code
                min_zoom =   7.0f;
                max_zoom =  11.0f;
            }
            case "Seychelles" -> {
                // SC country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Sudan" -> {
                // SD country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Sweden" -> {
                // SE country code
                min_zoom =   6.7f;
                max_zoom =  11.0f;
            }
            case "Singapore" -> {
                // SG country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Saint Helena" -> {
                // SH country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Slovenia" -> {
                // SI country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Slovakia" -> {
                // SK country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Sierra Leone" -> {
                // SL country code
                min_zoom =   7.8f;
                max_zoom =  11.0f;
            }
            case "San Marino" -> {
                // SM country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Senegal" -> {
                // SN country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Somalia" -> {
                // SO country code
                min_zoom =   7.0f;
                max_zoom =  11.0f;
            }
            case "Suriname" -> {
                // SR country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "S. Sudan" -> {
                // SS country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "São Tomé and Principe" -> {
                // ST country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "El Salvador" -> {
                // SV country code
                min_zoom =  10.0f;
                max_zoom =  11.0f;
            }
            case "Sint Maarten" -> {
                // SX country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Syria" -> {
                // SY country code
                min_zoom =   7.7f;
                max_zoom =  11.5f;
            }
            case "eSwatini" -> {
                // SZ country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Turks and Caicos Is" -> {
                // TC country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Chad" -> {
                // TD country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Fr. S. Antarctic Lands" -> {
                // TF country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Togo" -> {
                // TG country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Thailand" -> {
                // TH country code
                min_zoom =   8.2f;
                max_zoom =  11.0f;
            }
            case "Tajikistan" -> {
                // TJ country code
                min_zoom =   6.7f;
                max_zoom =  11.0f;
            }
            case "Timor-Leste" -> {
                // TL country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Turkmenistan" -> {
                // TM country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Tunisia" -> {
                // TN country code
                min_zoom =   7.7f;
                max_zoom =  11.0f;
            }
            case "Tonga" -> {
                // TO country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Turkey" -> {
                // TR country code
                min_zoom =   7.0f;
                max_zoom =  11.0f;
            }
            case "Trinidad and Tobago" -> {
                // TT country code
                min_zoom =  10.0f;
                max_zoom =  11.0f;
            }
            case "Tuvalu" -> {
                // TV country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "Taiwan" -> {
                // TW country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "Tanzania" -> {
                // TZ country code
                min_zoom =   6.7f;
                max_zoom =  11.0f;
            }
            case "Ukraine" -> {
                // UA country code
                min_zoom =   6.7f;
                max_zoom =  11.0f;
            }
            case "Uganda" -> {
                // UG country code
                min_zoom =  10.0f;
                max_zoom =  11.0f;
            }
            case "U.S. Minor Outlying Is" -> {
                // UM country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "United States of America" -> {
                // US country code
                min_zoom =   3.5f;
                max_zoom =   7.5f;
            }
            case "Uruguay" -> {
                // UY country code
                min_zoom =   8.0f;
                max_zoom =  11.0f;
            }
            case "Uzbekistan" -> {
                // UZ country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Vatican" -> {
                // VA country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "St. Vin. and Gren" -> {
                // VC country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Venezuela" -> {
                // VE country code
                min_zoom =   7.1f;
                max_zoom =  11.3f;
            }
            case "British Virgin Is" -> {
                // VG country code
                min_zoom =  18.0f;
                max_zoom =  18.0f;
            }
            case "U.S. Virgin Is" -> {
                // VI country code
                min_zoom =  11.0f;
                max_zoom =  11.0f;
            }
            case "Vietnam" -> {
                // VN country code
                min_zoom =   8.3f;
                max_zoom =  11.0f;
            }
            case "Vanuatu" -> {
                // VU country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Wallis and Futuna Is" -> {
                // WF country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Samoa" -> {
                // WS country code
                min_zoom =   9.0f;
                max_zoom =  11.0f;
            }
            case "Kosovo" -> {
                // XK country code
                min_zoom =  10.0f;
                max_zoom =  11.0f;
            }
            case "Yemen" -> {
                // YE country code
                min_zoom =   8.7f;
                max_zoom =  11.0f;
            }
            case "South Africa" -> {
                // ZA country code
                min_zoom =   4.6f;
                max_zoom =  10.1f;
            }
            case "Zambia" -> {
                // ZM country code
                min_zoom =   6.6f;
                max_zoom =  11.0f;
            }
            case "Zimbabwe" -> {
                // ZW country code
                min_zoom =   6.7f;
                max_zoom =  11.0f;
            }
        }

        float[] zoomArray = new float[2];
        zoomArray[0] = min_zoom - 1;
        zoomArray[1] = max_zoom - 1;

        return zoomArray;
    }
}
