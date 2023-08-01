package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Query hardcoded information about sub-national regions.
 * <p>
 * Embedded hand-curated data on sub-national regions of significant extents, to assist in labeling. Includes US states,
 * AU states and territories, CA provinces and territories, and other significant regions globally
 * </p>
 */
public class RegionInfos {

  private static String data = """
    Australian Capital Territory|AU-ACT|3.5|7.5|Q3258
    New South Wales|AU-NSW|3.5|7.5|Q3224
    Northern Territory|AU-NT|3.5|7.5|Q3235
    Queensland|AU-QLD|3.5|7.5|Q36074
    South Australia|AU-SA|3.5|7.5|Q35715
    Tasmania|AU-TAS|3.5|7.5|Q34366
    Victoria|AU-VIC|3.5|7.5|Q36687
    Western Australia|AU-WA|3.5|7.5|Q3206
    Jervis Bay Territory|AU-X02~|3.5|7.5|Q15577
    Macquarie Island|AU-X03~|3.5|7.5|Q46650
    Alberta|CA-AB|3.5|7.5|Q1951
    British Columbia|CA-BC|3.5|7.5|Q1974
    Manitoba|CA-MB|3.5|7.5|Q1948
    New Brunswick|CA-NB|3.5|7.5|Q1965
    Newfoundland and Labrador|CA-NL|3.5|7.5|Q2003
    Nova Scotia|CA-NS|3.5|7.5|Q1952
    Northwest Territories|CA-NT|3.5|7.5|Q2007
    Nunavut|CA-NU|3.5|7.5|Q2023
    Ontario|CA-ON|3.5|7.5|Q1904
    Prince Edward Island|CA-PE|3.5|7.5|Q1979
    Québec|CA-QC|3.5|7.5|Q176
    Saskatchewan|CA-SK|3.5|7.5|Q1989
    Yukon|CA-YT|3.5|7.5|Q2009
    Alaska|US-AK|3.5|7.5|Q797
    Alabama|US-AL|3.5|7.5|Q173
    Arkansas|US-AR|3.5|7.5|Q1612
    Arizona|US-AZ|3.5|7.5|Q816
    California|US-CA|3.5|7.5|Q99
    Colorado|US-CO|3.5|7.5|Q1261
    Connecticut|US-CT|3.5|7.5|Q779
    District of Columbia|US-DC|3.5|7.5|Q61
    Delaware|US-DE|3.5|7.5|Q1393
    Florida|US-FL|3.5|7.5|Q812
    Georgia|US-GA|3.5|7.5|Q1428
    Hawaii|US-HI|3.5|7.5|Q782
    Iowa|US-IA|3.5|7.5|Q1546
    Idaho|US-ID|3.5|7.5|Q1221
    Illinois|US-IL|3.5|7.5|Q1204
    Indiana|US-IN|3.5|7.5|Q1415
    Kansas|US-KS|3.5|7.5|Q1558
    Kentucky|US-KY|3.5|7.5|Q1603
    Louisiana|US-LA|3.5|7.5|Q1588
    Massachusetts|US-MA|3.5|7.5|Q771
    Maryland|US-MD|3.5|7.5|Q1391
    Maine|US-ME|3.5|7.5|Q724
    Michigan|US-MI|3.5|7.5|Q1166
    Minnesota|US-MN|3.5|7.5|Q1527
    Missouri|US-MO|3.5|7.5|Q1581
    Mississippi|US-MS|3.5|7.5|Q1494
    Montana|US-MT|3.5|7.5|Q1212
    North Carolina|US-NC|3.5|7.5|Q1454
    North Dakota|US-ND|3.5|7.5|Q1207
    Nebraska|US-NE|3.5|7.5|Q1553
    New Hampshire|US-NH|3.5|7.5|Q759
    New Jersey|US-NJ|3.5|7.5|Q1408
    New Mexico|US-NM|3.5|7.5|Q1522
    Nevada|US-NV|3.5|7.5|Q1227
    New York|US-NY|3.5|7.5|Q1384
    Ohio|US-OH|3.5|7.5|Q1397
    Oklahoma|US-OK|3.5|7.5|Q1649
    Oregon|US-OR|3.5|7.5|Q824
    Pennsylvania|US-PA|3.5|7.5|Q1400
    Rhode Island|US-RI|3.5|7.5|Q1387
    South Carolina|US-SC|3.5|7.5|Q1456
    South Dakota|US-SD|3.5|7.5|Q1211
    Tennessee|US-TN|3.5|7.5|Q1509
    Texas|US-TX|3.5|7.5|Q1439
    Utah|US-UT|3.5|7.5|Q829
    Virginia|US-VA|3.5|7.5|Q1370
    Vermont|US-VT|3.5|7.5|Q16551
    Washington|US-WA|3.5|7.5|Q1223
    Wisconsin|US-WI|3.5|7.5|Q1537
    West Virginia|US-WV|3.5|7.5|Q1371
    Wyoming|US-WY|3.5|7.5|Q1214
    Acre|BR-AC|3.7|8.5|Q40780
    Alagoas|BR-AL|3.7|8.5|Q40885
    Amazonas|BR-AM|3.7|8.5|Q40040
    Amapá|BR-AP|3.7|8.5|Q40130
    Bahia|BR-BA|3.7|8.5|Q40430
    Ceará|BR-CE|3.7|8.5|Q40123
    Distrito Federal|BR-DF|3.7|8.5|Q119158
    Espírito Santo|BR-ES|3.7|8.5|Q43233
    Goiás|BR-GO|3.7|8.5|Q41587
    Maranhão|BR-MA|3.7|8.5|Q42362
    Minas Gerais|BR-MG|3.7|8.5|Q39109
    Mato Grosso do Sul|BR-MS|3.7|8.5|Q43319
    Mato Grosso|BR-MT|3.7|8.5|Q42824
    Pará|BR-PA|3.7|8.5|Q39517
    Paraíba|BR-PB|3.7|8.5|Q38088
    Pernambuco|BR-PE|3.7|8.5|Q40942
    Piauí|BR-PI|3.7|8.5|Q42722
    Paraná|BR-PR|3.7|8.5|Q15499
    Rio de Janeiro|BR-RJ|3.7|8.5|Q41428
    Rio Grande do Norte|BR-RN|3.7|8.5|Q43255
    Rondônia|BR-RO|3.7|8.5|Q43235
    Roraima|BR-RR|3.7|8.5|Q42508
    Rio Grande do Sul|BR-RS|3.7|8.5|Q40030
    Santa Catarina|BR-SC|3.7|8.5|Q41115
    Sergipe|BR-SE|3.7|8.5|Q43783
    São Paulo|BR-SP|3.7|8.5|Q175
    Tocantins|BR-TO|3.7|8.5|Q43695
    Anhui|CN-AH|4.6|10.1|Q40956
    Beijing|CN-BJ|4.6|10.1|Q956
    Chongqing|CN-CQ|4.6|10.1|Q11725
    Fujian|CN-FJ|4.6|10.1|Q41705
    Guangdong|CN-GD|4.6|10.1|Q15175
    Gansu|CN-GS|4.6|10.1|Q42392
    Guangxi|CN-GX|4.6|10.1|Q15176
    Guizhou|CN-GZ|4.6|10.1|Q47097
    Henan|CN-HA|4.6|10.1|Q43684
    Hubei|CN-HB|4.6|10.1|Q46862
    Hebei|CN-HE|4.6|10.1|Q21208
    Hainan|CN-HI|4.6|10.1|Q42200
    Heilongjiang|CN-HL|4.6|10.1|Q19206
    Hunan|CN-HN|4.6|10.1|Q45761
    Jilin|CN-JL|4.6|10.1|Q45208
    Jiangsu|CN-JS|4.6|10.1|Q16963
    Jiangxi|CN-JX|4.6|10.1|Q57052
    Liaoning|CN-LN|4.6|10.1|Q43934
    Inner Mongol|CN-NM|4.6|10.1|Q41079
    Ningxia|CN-NX|4.6|10.1|Q57448
    Qinghai|CN-QH|4.6|10.1|Q45833
    Sichuan|CN-SC|4.6|10.1|Q19770
    Shandong|CN-SD|4.6|10.1|Q43407
    Shanghai|CN-SH|4.6|10.1|Q8686
    Shaanxi|CN-SN|4.6|10.1|Q47974
    Shanxi|CN-SX|4.6|10.1|Q46913
    Tianjin|CN-TJ|4.6|10.1|Q11736
    Xinjiang|CN-XJ|4.6|10.1|Q34800
    Xizang|CN-XZ|4.6|10.1|Q17269
    Yunnan|CN-YN|4.6|10.1|Q43194
    Zhejiang|CN-ZJ|4.6|10.1|Q16967
    Andaman and Nicobar|IN-AN|4.6|10.1|Q40888
    Andhra Pradesh|IN-AP|4.6|10.1|Q1159
    Arunachal Pradesh|IN-AR|4.6|10.1|Q1162
    Assam|IN-AS|4.6|10.1|Q1164
    Bihar|IN-BR|4.6|10.1|Q1165
    Chandigarh|IN-CH|4.6|10.1|Q43433
    Chhattisgarh|IN-CT|4.6|10.1|Q1168
    Dadra and Nagar Haveli and Daman and Diu|IN-DH|4.6|10.1|Q77997266
    Delhi|IN-DL|4.6|10.1|Q1353
    Goa|IN-GA|4.6|10.1|Q1171
    Gujarat|IN-GJ|4.6|10.1|Q1061
    Himachal Pradesh|IN-HP|4.6|10.1|Q1177
    Haryana|IN-HR|4.6|10.1|Q1174
    Jharkhand|IN-JH|4.6|10.1|Q1184
    Jammu and Kashmir|IN-JK|4.6|10.1|Q66278313
    Karnataka|IN-KA|4.6|10.1|Q1185
    Kerala|IN-KL|4.6|10.1|Q1186
    Ladakh|IN-LA|4.6|10.1|Q200667
    Lakshadweep|IN-LD|4.6|10.1|Q26927
    Maharashtra|IN-MH|4.6|10.1|Q1191
    Meghalaya|IN-ML|4.6|10.1|Q1195
    Manipur|IN-MN|4.6|10.1|Q1193
    Madhya Pradesh|IN-MP|4.6|10.1|Q1188
    Mizoram|IN-MZ|4.6|10.1|Q1502
    Nagaland|IN-NL|4.6|10.1|Q1599
    Odisha|IN-OR|4.6|10.1|Q22048
    Punjab|IN-PB|4.6|10.1|Q22424
    Puducherry|IN-PY|4.6|10.1|Q66743
    Rajasthan|IN-RJ|4.6|10.1|Q1437
    Sikkim|IN-SK|4.6|10.1|Q1505
    Telangana|IN-TG|4.6|10.1|Q677037
    Tamil Nadu|IN-TN|4.6|10.1|Q1445
    Tripura|IN-TR|4.6|10.1|Q1363
    Uttar Pradesh|IN-UP|4.6|10.1|Q1498
    Uttarakhand|IN-UT|4.6|10.1|Q1499
    West Bengal|IN-WB|4.6|10.1|Q1356
    Eastern Cape|ZA-EC|4.6|10.1|Q130840
    Free State|ZA-FS|4.6|10.1|Q160284
    Gauteng|ZA-GT|4.6|10.1|Q133083
    Limpopo|ZA-LP|4.6|10.1|Q134907
    Mpumalanga|ZA-MP|4.6|10.1|Q132410
    Northern Cape|ZA-NC|4.6|10.1|Q132418
    KwaZulu-Natal|ZA-NL|4.6|10.1|Q81725
    North West|ZA-NW|4.6|10.1|Q165956
    Western Cape|ZA-WC|4.6|10.1|Q127167
    Aceh|ID-AC|5|10.1|Q1823
    Bali|ID-BA|5|10.1|Q3125978
    Bangka-Belitung|ID-BB|5|10.1|Q1866
    Bengkulu|ID-BE|5|10.1|Q1890
    Banten|ID-BT|5|10.1|Q3540
    Gorontalo|ID-GO|5|10.1|Q5067
    Jambi|ID-JA|5|10.1|Q2051
    Jawa Barat|ID-JB|5|10.1|Q3724
    Jawa Timur|ID-JI|5|10.1|Q3586
    Jakarta Raya|ID-JK|5|10.1|Q3630
    Jawa Tengah|ID-JT|5|10.1|Q3557
    Kalimantan Barat|ID-KB|5|10.1|Q3916
    Kalimantan Timur|ID-KI|5|10.1|Q3899
    Kepulauan Riau|ID-KR|5|10.1|Q2223
    Kalimantan Selatan|ID-KS|5|10.1|Q3906
    Kalimantan Tengah|ID-KT|5|10.1|Q3891
    Lampung|ID-LA|5|10.1|Q2110
    Maluku|ID-MA|5|10.1|Q5093
    Maluku Utara|ID-MU|5|10.1|Q5094
    Nusa Tenggara Barat|ID-NB|5|10.1|Q5062
    Nusa Tenggara Timur|ID-NT|5|10.1|Q5061
    Papua|ID-PA|5|10.1|Q5095
    Papua Barat|ID-PB|5|10.1|Q5096
    Riau|ID-RI|5|10.1|Q2175
    Sulawesi Utara|ID-SA|5|10.1|Q5068
    Sumatera Barat|ID-SB|5|10.1|Q2772
    Sulawesi Tenggara|ID-SG|5|10.1|Q5075
    Sulawesi Selatan|ID-SN|5|10.1|Q5078
    Sulawesi Barat|ID-SR|5|10.1|Q5082
    Sumatera Selatan|ID-SS|5|10.1|Q2271
    Sulawesi Tengah|ID-ST|5|10.1|Q5065
    Sumatera Utara|ID-SU|5|10.1|Q2140
    Yogyakarta|ID-YO|5|10.1|Q3741
    Baluchistan|PK-BA|5|10.5|Q163239
    Northern Areas|PK-GB|5|10.5|Q200697
    F.C.T.|PK-IS|5|10.5|Q848613
    Azad Kashmir|PK-JK|5|10.5|Q200130
    K.P.|PK-KP|5|10.5|Q183314
    Punjab|PK-PB|5|10.5|Q4478
    Sind|PK-SD|5|10.5|Q37211
    F.A.T.A.|PK-TA|5|10.5|Q208270
    Adygey|RU-AD|5|10.2|Q3734
    Gorno-Altay|RU-AL|5|10.2|Q5971
    Altay|RU-ALT|5|10.2|Q5971
    Amur|RU-AMU|5|10.2|Q6886
    Arkhangel'sk|RU-ARK|5|10.2|Q1875
    Astrakhan'|RU-AST|5|10.2|Q3941
    Bashkortostan|RU-BA|5|10.2|Q5710
    Belgorod|RU-BEL|5|10.2|Q3329
    Bryansk|RU-BRY|5|10.2|Q2810
    Buryat|RU-BU|5|10.2|Q6809
    Chechnya|RU-CE|5|10.2|Q5187
    Chelyabinsk|RU-CHE|5|10.2|Q5714
    Chukchi Autonomous Okrug|RU-CHU|5|10.2|Q7984
    Chuvash|RU-CU|5|10.2|Q5466
    Dagestan|RU-DA|5|10.2|Q5118
    Ingush|RU-IN|5|10.2|Q5219
    Irkutsk|RU-IRK|5|10.2|Q6585
    Ivanovo|RU-IVA|5|10.2|Q2654
    Kamchatka|RU-KAM|5|10.2|Q7948
    Kabardin-Balkar|RU-KB|5|10.2|Q5267
    Karachay-Cherkess|RU-KC|5|10.2|Q5328
    Krasnodar|RU-KDA|5|10.2|Q3680
    Kemerovo|RU-KEM|5|10.2|Q6076
    Kaliningrad|RU-KGD|5|10.2|Q1749
    Kurgan|RU-KGN|5|10.2|Q5741
    Khabarovsk|RU-KHA|5|10.2|Q7788
    Khanty-Mansiy|RU-KHM|5|10.2|Q6320
    Kirov|RU-KIR|5|10.2|Q5387
    Khakass|RU-KK|5|10.2|Q6543
    Kalmyk|RU-KL|5|10.2|Q3953
    Kaluga|RU-KLU|5|10.2|Q2842
    Komi|RU-KO|5|10.2|Q2073
    Kostroma|RU-KOS|5|10.2|Q2596
    Karelia|RU-KR|5|10.2|Q1914
    Kursk|RU-KRS|5|10.2|Q3178
    Krasnoyarsk|RU-KYA|5|10.2|Q6563
    Leningrad|RU-LEN|5|10.2|Q2191
    Lipetsk|RU-LIP|5|10.2|Q3510
    Maga Buryatdan|RU-MAG|5|10.2|Q7971
    Mariy-El|RU-ME|5|10.2|Q5446
    Mordovia|RU-MO|5|10.2|Q5340
    Moskva|RU-MOS|5|9|Q649
    Moskovskaya|RU-MOW|5|9|Q1697
    Murmansk|RU-MUR|5|10.2|Q1759
    Nenets|RU-NEN|5|10.2|Q2164
    Novgorod|RU-NGR|5|10.2|Q2240
    Nizhegorod|RU-NIZ|5|10.2|Q2246
    Novosibirsk|RU-NVS|5|10.2|Q5851
    Omsk|RU-OMS|5|10.2|Q5835
    Orenburg|RU-ORE|5|10.2|Q5338
    Orel|RU-ORL|5|10.2|Q3129
    Perm'|RU-PER|5|10.2|Q5400
    Penza|RU-PNZ|5|10.2|Q5545
    Primor'ye|RU-PRI|5|10.2|Q4341
    Pskov|RU-PSK|5|10.2|Q2218
    Rostov|RU-ROS|5|10.2|Q3573
    Ryazan'|RU-RYA|5|10.2|Q2753
    Sakha (Yakutia)|RU-SA|5|10.2|Q6605
    Sakhalin|RU-SAK|5|10.2|Q7797
    Samara|RU-SAM|5|10.2|Q1727
    Saratov|RU-SAR|5|10.2|Q5334
    North Ossetia|RU-SE|5|10.2|Q5237
    Smolensk|RU-SMO|5|10.2|Q2347
    City of St. Petersburg|RU-SPE|5|9|Q656
    Stavropol'|RU-STA|5|10.2|Q5207
    Sverdlovsk|RU-SVE|5|10.2|Q5462
    Tatarstan|RU-TA|5|10.2|Q5481
    Tambov|RU-TAM|5|10.2|Q3550
    Tomsk|RU-TOM|5|10.2|Q5884
    Tula|RU-TUL|5|10.2|Q2792
    Tver'|RU-TVE|5|10.2|Q2292
    Tuva|RU-TY|5|10.2|Q960
    Tyumen'|RU-TYU|5|10.2|Q5824
    Udmurt|RU-UD|5|10.2|Q5422
    Ul'yanovsk|RU-ULY|5|10.2|Q5634
    Volgograd|RU-VGG|5|10.2|Q3819
    Vladimir|RU-VLA|5|10.2|Q2702
    Vologda|RU-VLG|5|10.2|Q2015
    Voronezh|RU-VOR|5|10.2|Q3447
    Yamal-Nenets|RU-YAN|5|10.2|Q6407
    Yaroslavl'|RU-YAR|5|10.2|Q2448
    Yevrey|RU-YEV|5|10.2|Q7730
    Chita|RU-ZAB|5|10.2|Q6838
    Sevastopol|UA-40|5|11|Q7525
    Crimea|UA-43|5|11|Q756294
    Salta|AR-A|6|11|Q44803
    Buenos Aires|AR-B|6|11|Q44754
    Ciudad de Buenos Aires|AR-C|6|11|Q1486
    San Luis|AR-D|6|11|Q44818
    Entre Ríos|AR-E|6|11|Q44762
    La Rioja|AR-F|6|11|Q44796
    Santiago del Estero|AR-G|6|11|Q44827
    Chaco|AR-H|6|11|Q44757
    San Juan|AR-J|6|11|Q44805
    Catamarca|AR-K|6|11|Q44756
    La Pampa|AR-L|6|11|Q44795
    Mendoza|AR-M|6|11|Q44797
    Misiones|AR-N|6|11|Q44798
    Formosa|AR-P|6|11|Q44761
    Neuquén|AR-Q|6|11|Q44800
    Río Negro|AR-R|6|11|Q44802
    Santa Fe|AR-S|6|11|Q44823
    Tucumán|AR-T|6|11|Q44829
    Chubut|AR-U|6|11|Q45007
    Tierra del Fuego|AR-V|6|11|Q44832
    Corrientes|AR-W|6|11|Q44758
    Córdoba|AR-X|6|11|Q44759
    Jujuy|AR-Y|6|11|Q44764
    Santa Cruz|AR-Z|6|11|Q44821
    Aisén del General Carlos Ibáñez del Campo|CL-AI|6|11|Q2181
    Antofagasta|CL-AN|6|11|Q2118
    Arica y Parinacota|CL-AP|6|11|Q2109
    La Araucanía|CL-AR|6|11|Q2176
    Atacama|CL-AT|6|11|Q2120
    Bío-Bío|CL-BI|6|11|Q2170
    Coquimbo|CL-CO|6|11|Q2121
    Libertador General Bernardo O'Higgins|CL-LI|6|11|Q2133
    Los Lagos|CL-LL|6|11|Q2178
    Los Ríos|CL-LR|6|11|Q2177
    Magallanes y Antártica Chilena|CL-MA|6|11|Q2189
    Maule|CL-ML|6|11|Q2166
    Ñuble|CL-NB|6|11|Q24076693
    Región Metropolitana de Santiago|CL-RM|6|11|Q2131
    Tarapacá|CL-TA|6|11|Q2114
    Valparaíso|CL-VS|6|11|Q219458
    Reykjavík|IS-0|6|11|Q1764
    Höfuðborgarsvæði|IS-1|6|11|Q203304
    Suðurnes|IS-2|6|11|Q212768
    Vesturland|IS-3|6|11|Q221791
    Vestfirðir|IS-4|6|11|Q727267
    Norðurland vestra|IS-5|6|11|Q210866
    Norðurland eystra|IS-6|6|11|Q241551
    Austurland|IS-7|6|11|Q220663
    Suðurland|IS-8|6|11|Q204796
    Brandenburg|DE-BB|6.6|11|Q1208
    Berlin|DE-BE|6.6|11|Q64
    Baden-Württemberg|DE-BW|6.6|11|Q985
    Bayern|DE-BY|6.6|11|Q980
    Bremen|DE-HB|6.6|11|Q1209
    Hessen|DE-HE|6.6|11|Q1199
    Hamburg|DE-HH|6.6|11|Q1055
    Mecklenburg-Vorpommern|DE-MV|6.6|11|Q1196
    Niedersachsen|DE-NI|6.6|11|Q1197
    Nordrhein-Westfalen|DE-NW|6.6|11|Q1198
    Rheinland-Pfalz|DE-RP|6.6|11|Q1200
    Schleswig-Holstein|DE-SH|6.6|11|Q1194
    Saarland|DE-SL|6.6|11|Q1201
    Sachsen|DE-SN|6.6|11|Q1202
    Sachsen-Anhalt|DE-ST|6.6|11|Q1206
    Thüringen|DE-TH|6.6|11|Q1205
    Aguascalientes|MX-AGU|6.6|11|Q79952
    Baja California|MX-BCN|6.6|11|Q58731
    Baja California Sur|MX-BCS|6.6|11|Q46508
    Campeche|MX-CAM|6.6|11|Q80908
    Chihuahua|MX-CHH|6.6|11|Q655
    Chiapas|MX-CHP|6.6|11|Q60123
    Coahuila|MX-COA|6.6|11|Q53079
    Colima|MX-COL|6.6|11|Q61309
    Distrito Federal|MX-DIF|6.6|11|Q1489
    Durango|MX-DUR|6.6|11|Q79918
    Guerrero|MX-GRO|6.6|11|Q60158
    Guanajuato|MX-GUA|6.6|11|Q46475
    Hidalgo|MX-HID|6.6|11|Q80903
    Jalisco|MX-JAL|6.6|11|Q13160
    México|MX-MEX|6.6|11|Q82112
    Michoacán|MX-MIC|6.6|11|Q79861
    Morelos|MX-MOR|6.6|11|Q66117
    Nayarit|MX-NAY|6.6|11|Q79920
    Nuevo León|MX-NLE|6.6|11|Q15282
    Oaxaca|MX-OAX|6.6|11|Q34110
    Puebla|MX-PUE|6.6|11|Q79923
    Querétaro|MX-QUE|6.6|11|Q79754
    Quintana Roo|MX-ROO|6.6|11|Q80245
    Sinaloa|MX-SIN|6.6|11|Q80252
    San Luis Potosí|MX-SLP|6.6|11|Q78980
    Sonora|MX-SON|6.6|11|Q46422
    Tabasco|MX-TAB|6.6|11|Q80914
    Tamaulipas|MX-TAM|6.6|11|Q80007
    Tlaxcala|MX-TLA|6.6|11|Q82681
    Veracruz|MX-VER|6.6|11|Q60130
    Yucatán|MX-YUC|6.6|11|Q60176
    Zacatecas|MX-ZAC|6.6|11|Q80269
    Niassa|MZ-A|6.6|11|Q622799
    Manica|MZ-B|6.6|11|Q622792
    Gaza|MZ-G|6.6|11|Q466526
    Inhambane|MZ-I|6.6|11|Q466547
    Maputo|MZ-L|6.6|11|Q3889
    Maputo|MZ-L|6.6|11|Q379658
    Nampula|MZ-N|6.6|11|Q622794
    Cabo Delgado|MZ-P|6.6|11|Q466538
    Zambezia|MZ-Q|6.6|11|Q622803
    Sofala|MZ-S|6.6|11|Q622801
    Tete|MZ-T|6.6|11|Q605787
    Agadez|NE-1|6.6|11|Q389944
    Diffa|NE-2|6.6|11|Q1053302
    Dosso|NE-3|6.6|11|Q850055
    Maradi|NE-4|6.6|11|Q850036
    Tahoua|NE-5|6.6|11|Q871083
    Tillabéri|NE-6|6.6|11|Q861914
    Zinder|NE-7|6.6|11|Q204367
    Niamey|NE-8|6.6|11|Q3674
    Abia|NG-AB|6.6|11|Q320852
    Adamawa|NG-AD|6.6|11|Q337514
    Akwa Ibom|NG-AK|6.6|11|Q424388
    Anambra|NG-AN|6.6|11|Q117714
    Bauchi|NG-BA|6.6|11|Q503936
    Benue|NG-BE|6.6|11|Q429908
    Borno|NG-BO|6.6|11|Q130626
    Bayelsa|NG-BY|6.6|11|Q532153
    Cross River|NG-CR|6.6|11|Q739676
    Delta|NG-DE|6.6|11|Q503910
    Ebonyi|NG-EB|6.6|11|Q506808
    Edo|NG-ED|6.6|11|Q682704
    Ekiti|NG-EK|6.6|11|Q534917
    Enugu|NG-EN|6.6|11|Q3817714
    Federal Capital Territory|NG-FC|6.6|11|Q509300
    Gombe|NG-GO|6.6|11|Q376241
    Imo|NG-IM|6.6|11|Q842939
    Jigawa|NG-JI|6.6|11|Q682691
    Kaduna|NG-KD|6.6|11|Q878284
    Kebbi|NG-KE|6.6|11|Q748523
    Kano|NG-KN|6.6|11|Q682571
    Kogi|NG-KO|6.6|11|Q387745
    Katsina|NG-KT|6.6|11|Q657821
    Kwara|NG-KW|6.6|11|Q464953
    Lagos|NG-LA|6.6|11|Q815913
    Nassarawa|NG-NA|6.6|11|Q836645
    Niger|NG-NI|6.6|11|Q503932
    Ogun|NG-OG|6.6|11|Q836657
    Ondo|NG-ON|6.6|11|Q836654
    Osun|NG-OS|6.6|11|Q682721
    Oyo|NG-OY|6.6|11|Q503905
    Plateau|NG-PL|6.6|11|Q503928
    Rivers|NG-RI|6.6|11|Q503923
    Sokoto|NG-SO|6.6|11|Q503941
    Taraba|NG-TA|6.6|11|Q463959
    Yobe|NG-YO|6.6|11|Q682777
    Zamfara|NG-ZA|6.6|11|Q145793
    Amazonas|PE-AMA|6.6|11|Q201162
    Áncash|PE-ANC|6.6|11|Q205089
    Apurímac|PE-APU|6.6|11|Q208185
    Arequipa|PE-ARE|6.6|11|Q205068
    Ayacucho|PE-AYA|6.6|11|Q205112
    Cajamarca|PE-CAJ|6.6|11|Q205078
    Callao|PE-CAL|6.6|11|Q2634400
    Cusco|PE-CUS|6.6|11|Q205057
    Huánuco|PE-HUC|6.6|11|Q215221
    Huancavelica|PE-HUV|6.6|11|Q505220
    Ica|PE-ICA|6.6|11|Q208186
    Junín|PE-JUN|6.6|11|Q207973
    La Libertad|PE-LAL|6.6|11|Q205126
    Lambayeque|PE-LAM|6.6|11|Q210061
    Lima|PE-LIM|6.6|11|Q579240
    Lima Province|PE-LIM|6.6|11|Q579240
    Loreto|PE-LOR|6.6|11|Q200938
    Madre de Dios|PE-MDD|6.6|11|Q210896
    Moquegua|PE-MOQ|6.6|11|Q208182
    Pasco|PE-PAS|6.6|11|Q211208
    Piura|PE-PIU|6.6|11|Q208183
    Puno|PE-PUN|6.6|11|Q205104
    San Martín|PE-SAM|6.6|11|Q211793
    Tacna|PE-TAC|6.6|11|Q207413
    Tumbes|PE-TUM|6.6|11|Q209597
    Ucayali|PE-UCA|6.6|11|Q207424
    Ar Riyad|SA-01|6.6|11|Q1249255
    Makkah|SA-02|6.6|11|Q234167
    Al Madinah|SA-03|6.6|11|Q236027
    Ash Sharqiyah|SA-04|6.6|11|Q953508
    Al Quassim|SA-05|6.6|11|Q1105411
    Ha'il|SA-06|6.6|11|Q243656
    Tabuk|SA-07|6.6|11|Q1315953
    Al Hudud ash Shamaliyah|SA-08|6.6|11|Q201781
    Jizan|SA-09|6.6|11|Q269973
    Najran|SA-10|6.6|11|Q464718
    Al Bahah|SA-11|6.6|11|Q852774
    Al Jawf|SA-12|6.6|11|Q1471266
    `Asir|SA-14|6.6|11|Q779855
    Østfold|NO-01|6.7|11|Q50614
    Akershus|NO-02|6.7|11|Q50615
    Oslo|NO-03|6.7|11|Q585
    Hedmark|NO-04|6.7|11|Q50616
    Oppland|NO-05|6.7|11|Q50617
    Buskerud|NO-06|6.7|11|Q50618
    Vestfold|NO-07|6.7|11|Q50619
    Telemark|NO-08|6.7|11|Q2254
    Aust-Agder|NO-09|6.7|11|Q50621
    Vest-Agder|NO-10|6.7|11|Q50623
    Rogaland|NO-11|6.7|11|Q50624
    Hordaland|NO-12|6.7|11|Q50625
    Sogn og Fjordane|NO-14|6.7|11|Q50626
    Møre og Romsdal|NO-15|6.7|11|Q50627
    Sør-Trøndelag|NO-16|6.7|11|Q50628
    Nord-Trøndelag|NO-17|6.7|11|Q50629
    Nordland|NO-18|6.7|11|Q50630
    Troms|NO-19|6.7|11|Q50631
    Finnmark|NO-20|6.7|11|Q50632
    Lower Silesian|PL-DS|6.7|11|Q54150
    Kuyavian-Pomeranian|PL-KP|6.7|11|Q54153
    Lubusz|PL-LB|6.7|11|Q54157
    Łódź|PL-LD|6.7|11|Q54158
    Lublin|PL-LU|6.7|11|Q54155
    Lesser Poland|PL-MA|6.7|11|Q54159
    Masovian|PL-MZ|6.7|11|Q54169
    Opole|PL-OP|6.7|11|Q54171
    Podlachian|PL-PD|6.7|11|Q54177
    Subcarpathian|PL-PK|6.7|11|Q54175
    Pomeranian|PL-PM|6.7|11|Q54180
    Świętokrzyskie|PL-SK|6.7|11|Q54183
    Silesian|PL-SL|6.7|11|Q54181
    Warmian-Masurian|PL-WN|6.7|11|Q54184
    Greater Poland|PL-WP|6.7|11|Q54187
    West Pomeranian|PL-ZP|6.7|11|Q54188
    Stockholm|SE-AB|6.7|11|Q104231
    Västerbotten|SE-AC|6.7|11|Q104877
    Norrbotten|SE-BD|6.7|11|Q103686
    Uppsala|SE-C|6.7|11|Q104926
    Södermanland|SE-D|6.7|11|Q106915
    Östergötland|SE-E|6.7|11|Q104940
    Jönköping|SE-F|6.7|11|Q103672
    Kronoberg|SE-G|6.7|11|Q104746
    Kalmar|SE-H|6.7|11|Q103707
    Gotland|SE-I|6.7|11|Q103738
    Blekinge|SE-K|6.7|11|Q102377
    Skåne|SE-M|6.7|11|Q103659
    Halland|SE-N|6.7|11|Q103691
    Västra Götaland|SE-O|6.7|11|Q103093
    Värmland|SE-S|6.7|11|Q106789
    Orebro|SE-T|6.7|11|Q104257
    Västmanland|SE-U|6.7|11|Q105075
    Dalarna|SE-W|6.7|11|Q103732
    Gävleborg|SE-X|6.7|11|Q103699
    Västernorrland|SE-Y|6.7|11|Q104891
    Jämtland|SE-Z|6.7|11|Q103679
    Vinnytsya|UA-05|6.7|11|Q166709
    Volyn|UA-07|6.7|11|Q167859
    Luhans'k|UA-09|6.7|11|Q171965
    Dnipropetrovs'k|UA-12|6.7|11|Q170672
    Donets'k|UA-14|6.7|11|Q2012050
    Zhytomyr|UA-18|6.7|11|Q40637
    Transcarpathia|UA-21|6.7|11|Q170213
    Zaporizhzhya|UA-23|6.7|11|Q171334
    Ivano-Frankivs'k|UA-26|6.7|11|Q178269
    Kiev City|UA-30|6.7|11|Q1899
    Kiev|UA-32|6.7|11|Q1899
    Kirovohrad|UA-35|6.7|11|Q180981
    L'viv|UA-46|6.7|11|Q164193
    Mykolayiv|UA-48|6.7|11|Q181633
    Odessa|UA-51|6.7|11|Q171852
    Poltava|UA-53|6.7|11|Q169501
    Rivne|UA-56|6.7|11|Q174187
    Sumy|UA-59|6.7|11|Q170446
    Ternopil'|UA-61|6.7|11|Q173407
    Kharkiv|UA-63|6.7|11|Q170666
    Kherson|UA-65|6.7|11|Q163271
    Khmel'nyts'kyy|UA-68|6.7|11|Q171331
    Cherkasy|UA-71|6.7|11|Q161808
    Chernihiv|UA-74|6.7|11|Q167874
    Chernivtsi|UA-77|6.7|11|Q168856
    Hokkaidō|JP-01|7|11|Q35581
    Aomori|JP-02|7|11|Q71699
    Iwate|JP-03|7|11|Q48326
    Miyagi|JP-04|7|11|Q47896
    Akita|JP-05|7|11|Q81863
    Yamagata|JP-06|7|11|Q125863
    Fukushima|JP-07|7|11|Q71707
    Ibaraki|JP-08|7|11|Q83273
    Tochigi|JP-09|7|11|Q44843
    Gunma|JP-10|7|11|Q129499
    Saitama|JP-11|7|11|Q128186
    Chiba|JP-12|7|11|Q80011
    Tokyo|JP-13|7|11|Q1490
    Kanagawa|JP-14|7|11|Q127513
    Niigata|JP-15|7|11|Q132705
    Toyama|JP-16|7|11|Q132929
    Ishikawa|JP-17|7|11|Q131281
    Fukui|JP-18|7|11|Q133879
    Yamanashi|JP-19|7|11|Q132720
    Nagano|JP-20|7|11|Q127877
    Gifu|JP-21|7|11|Q131277
    Shizuoka|JP-22|7|11|Q131320
    Aichi|JP-23|7|11|Q80434
    Mie|JP-24|7|11|Q128196
    Shiga|JP-25|7|11|Q131358
    Kyōto|JP-26|7|11|Q120730
    Ōsaka|JP-27|7|11|Q122723
    Hyōgo|JP-28|7|11|Q130290
    Nara|JP-29|7|11|Q131287
    Wakayama|JP-30|7|11|Q131314
    Tottori|JP-31|7|11|Q133935
    Shimane|JP-32|7|11|Q132751
    Okayama|JP-33|7|11|Q132936
    Hiroshima|JP-34|7|11|Q617375
    Yamaguchi|JP-35|7|11|Q127264
    Tokushima|JP-36|7|11|Q160734
    Kagawa|JP-37|7|11|Q161454
    Ehime|JP-38|7|11|Q123376
    Kōchi|JP-39|7|11|Q134093
    Fukuoka|JP-40|7|11|Q123258
    Saga|JP-41|7|11|Q160420
    Nagasaki|JP-42|7|11|Q169376
    Kumamoto|JP-43|7|11|Q130308
    Ōita|JP-44|7|11|Q133924
    Miyazaki|JP-45|7|11|Q130300
    Kagoshima|JP-46|7|11|Q15701
    Okinawa|JP-47|7|11|Q766445
    Auckland|NZ-AUK|7|11|Q726917
    Bay of Plenty|NZ-BOP|7|11|Q2192924
    Canterbury|NZ-CAN|7|11|Q657004
    Gisborne District|NZ-GIS|7|11|Q140246
    Hawke's Bay|NZ-HKB|7|11|Q251825
    Marlborough District|NZ-MBH|7|11|Q140083
    Manawatu-Wanganui|NZ-MWT|7|11|Q139907
    Nelson City|NZ-NSN|7|11|Q206687
    Northland|NZ-NTL|7|11|Q59596
    Otago|NZ-OTA|7|11|Q692912
    Southland|NZ-STL|7|11|Q864971
    Tasman District|NZ-TAS|7|11|Q666142
    Taranaki|NZ-TKI|7|11|Q140207
    Wellington|NZ-WGN|7|11|Q856010
    Waikato|NZ-WKO|7|11|Q139918
    West Coast|NZ-WTC|7|11|Q541468
    Adana|TR-01|7|11|Q40549
    Adiyaman|TR-02|7|11|Q43924
    Afyonkarahisar|TR-03|7|11|Q45220
    Agri|TR-04|7|11|Q80051
    Amasya|TR-05|7|11|Q80036
    Ankara|TR-06|7|11|Q2297724
    Antalya|TR-07|7|11|Q40249
    Artvin|TR-08|7|11|Q43745
    Aydin|TR-09|7|11|Q79846
    Balikesir|TR-10|7|11|Q47117
    Bilecik|TR-11|7|11|Q46763
    Bingöl|TR-12|7|11|Q79760
    Bitlis|TR-13|7|11|Q83239
    Bolu|TR-14|7|11|Q82089
    Burdur|TR-15|7|11|Q80088
    Bursa|TR-16|7|11|Q43690
    Çanakkale|TR-17|7|11|Q47813
    Çankiri|TR-18|7|11|Q272662
    Çorum|TR-19|7|11|Q272947
    Denizli|TR-20|7|11|Q82096
    Diyarbakir|TR-21|7|11|Q83081
    Edirne|TR-22|7|11|Q83102
    Elazig|TR-23|7|11|Q483091
    Erzincan|TR-24|7|11|Q483173
    Erzurum|TR-25|7|11|Q376797
    Eskisehir|TR-26|7|11|Q483053
    Gaziantep|TR-27|7|11|Q483154
    Giresun|TR-28|7|11|Q482779
    Gümüshane|TR-29|7|11|Q482788
    Hakkari|TR-30|7|11|Q93209
    Hatay|TR-31|7|11|Q83274
    Isparta|TR-32|7|11|Q268043
    Mersin|TR-33|7|11|Q132637
    Istanbul|TR-34|7|11|Q534799
    Izmir|TR-35|7|11|Q344490
    Kars|TR-36|7|11|Q83077
    Kastamonu|TR-37|7|11|Q483191
    Kayseri|TR-38|7|11|Q483472
    Kirklareli|TR-39|7|11|Q131597
    Kirsehir|TR-40|7|11|Q134187
    Kocaeli|TR-41|7|11|Q83965
    Konya|TR-42|7|11|Q81551
    Kütahya|TR-43|7|11|Q126874
    Malatya|TR-44|7|11|Q131384
    Manisa|TR-45|7|11|Q130553
    K. Maras|TR-46|7|11|Q482834
    Mardin|TR-47|7|11|Q131293
    Mugla|TR-48|7|11|Q123934
    Mus|TR-49|7|11|Q131387
    Nevsehir|TR-50|7|11|Q430693
    Nigde|TR-51|7|11|Q155219
    Ordu|TR-52|7|11|Q483180
    Rize|TR-53|7|11|Q483481
    Sakarya|TR-54|7|11|Q83069
    Samsun|TR-55|7|11|Q483040
    Siirt|TR-56|7|11|Q482825
    Sinop|TR-57|7|11|Q134413
    Sivas|TR-58|7|11|Q483100
    Tekirdag|TR-59|7|11|Q129387
    Tokat|TR-60|7|11|Q483195
    Trabzon|TR-61|7|11|Q388995
    Tunceli|TR-62|7|11|Q620742
    Sanliurfa|TR-63|7|11|Q388469
    Usak|TR-64|7|11|Q483078
    Van|TR-65|7|11|Q80550
    Yozgat|TR-66|7|11|Q75445
    Zinguldak|TR-67|7|11|Q219956
    Aksaray|TR-68|7|11|Q83073
    Bayburt|TR-69|7|11|Q483063
    Karaman|TR-70|7|11|Q482975
    Kinkkale|TR-71|7|11|Q484392
    Batman|TR-72|7|11|Q80370
    Sirnak|TR-73|7|11|Q647378
    Bartın|TR-74|7|11|Q83342
    Ardahan|TR-75|7|11|Q79840
    Iğdir|TR-76|7|11|Q125506
    Yalova|TR-77|7|11|Q483083
    Karabük|TR-78|7|11|Q483168
    Kilis|TR-79|7|11|Q128978
    Osmaniye|TR-80|7|11|Q281206
    Düzce|TR-81|7|11|Q432391
    Burgenland|AT-1|7.7|11|Q43210
    Kärnten|AT-2|7.7|11|Q37985
    Niederösterreich|AT-3|7.7|11|Q42497
    Oberösterreich|AT-4|7.7|11|Q41967
    Salzburg|AT-5|7.7|11|Q43325
    Steiermark|AT-6|7.7|11|Q41358
    Tirol|AT-7|7.7|11|Q42880
    Vorarlberg|AT-8|7.7|11|Q38981
    Nordjylland|DK-81|7.7|11|Q26067
    Midtjylland|DK-82|7.7|11|Q26586
    Syddanmark|DK-83|7.7|11|Q26061
    Hovedstaden|DK-84|7.7|11|Q26073
    Sjaælland|DK-85|7.7|11|Q26589
    Alicante|ES-A|7.7|11|Q54936
    Albacete|ES-AB|7.7|11|Q54889
    Almería|ES-AL|7.7|11|Q81802
    Ávila|ES-AV|7.7|11|Q55288
    Barcelona|ES-B|7.7|11|Q81949
    Badajoz|ES-BA|7.7|11|Q81803
    Bizkaia|ES-BI|7.7|11|Q93366
    Burgos|ES-BU|7.7|11|Q55271
    La Coruña|ES-C|7.7|11|Q82119
    Cádiz|ES-CA|7.7|11|Q81978
    Cáceres|ES-CC|7.7|11|Q81977
    Ceuta|ES-CE|7.7|11|Q5823
    Córdoba|ES-CO|7.7|11|Q81972
    Ciudad Real|ES-CR|7.7|11|Q54932
    Castellón|ES-CS|7.7|11|Q54942
    Cuenca|ES-CU|7.7|11|Q54888
    Las Palmas|ES-GC|7.7|11|Q95080
    Gerona|ES-GI|7.7|11|Q7194
    Granada|ES-GR|7.7|11|Q82142
    Guadalajara|ES-GU|7.7|11|Q54925
    Huelva|ES-H|7.7|11|Q95015
    Huesca|ES-HU|7.7|11|Q55182
    Jaén|ES-J|7.7|11|Q95025
    Lérida|ES-L|7.7|11|Q13904
    León|ES-LE|7.7|11|Q71140
    La Rioja|ES-LO|7.7|11|Q5727
    Lugo|ES-LU|7.7|11|Q95027
    Madrid|ES-M|7.7|11|Q5756
    Málaga|ES-MA|7.7|11|Q95028
    Melilla|ES-ML|7.7|11|Q5831
    Murcia|ES-MU|7.7|11|Q5772
    Navarra|ES-NA|7.7|11|Q4018
    Asturias|ES-O|7.7|11|Q3934
    Orense|ES-OR|7.7|11|Q95038
    Palencia|ES-P|7.7|11|Q55269
    Baleares|ES-PM|7.7|11|Q5765
    Pontevedra|ES-PO|7.7|11|Q95086
    Cantabria|ES-S|7.7|11|Q3946
    Salamanca|ES-SA|7.7|11|Q71080
    Sevilla|ES-SE|7.7|11|Q95088
    Segovia|ES-SG|7.7|11|Q55283
    Soria|ES-SO|7.7|11|Q55276
    Gipuzkoa|ES-SS|7.7|11|Q95010
    Tarragona|ES-T|7.7|11|Q98392
    Teruel|ES-TE|7.7|11|Q54955
    Santa Cruz de Tenerife|ES-TF|7.7|11|Q99976
    Toledo|ES-TO|7.7|11|Q54929
    Valencia|ES-V|7.7|11|Q54939
    Valladolid|ES-VA|7.7|11|Q71097
    Álava|ES-VI|7.7|11|Q81801
    Zaragoza|ES-Z|7.7|11|Q55180
    Zamora|ES-ZA|7.7|11|Q71113
    Ayion Oros|GR-69|7.7|11|Q130321
    Anatoliki Makedonia kai Thraki|GR-A|7.7|11|Q171314
    Attiki|GR-A1|7.7|11|Q758056
    Kentriki Makedonia|GR-B|7.7|11|Q17152
    Dytiki Makedonia|GR-C|7.7|11|Q165408
    Ipeiros|GR-D|7.7|11|Q180484
    Thessalia|GR-E|7.7|11|Q166919
    Ionioi Nisoi|GR-F|7.7|11|Q1147674
    Dytiki Ellada|GR-G|7.7|11|Q170291
    Stereá Elláda|GR-H|7.7|11|Q199580
    Peloponnisos|GR-J|7.7|11|Q202484
    Voreio Aigaio|GR-K|7.7|11|Q173620
    Notio Aigaio|GR-L|7.7|11|Q173616
    Kriti|GR-M|7.7|11|Q34374
    Ain|FR-01|8|11|Q3083
    Aisne|FR-02|8|11|Q3093
    Allier|FR-03|8|11|Q3113
    Alpes-de-Haute-Provence|FR-04|8|11|Q3131
    Hautes-Alpes|FR-05|8|11|Q3125
    Alpes-Maritimes|FR-06|8|11|Q3139
    Ardèche|FR-07|8|11|Q3148
    Ardennes|FR-08|8|11|Q3164
    Ariège|FR-09|8|11|Q3184
    Aube|FR-10|8|11|Q3194
    Aude|FR-11|8|11|Q3207
    Aveyron|FR-12|8|11|Q3216
    Bouches-du-Rhône|FR-13|8|11|Q3240
    Calvados|FR-14|8|11|Q3249
    Cantal|FR-15|8|11|Q3259
    Charente|FR-16|8|11|Q3266
    Charente-Maritime|FR-17|8|11|Q3278
    Cher|FR-18|8|11|Q3286
    Corrèze|FR-19|8|11|Q3326
    Côte-d'Or|FR-21|8|11|Q3342
    Côtes-d'Armor|FR-22|8|11|Q3349
    Creuse|FR-23|8|11|Q3353
    Dordogne|FR-24|8|11|Q3357
    Doubs|FR-25|8|11|Q3361
    Drôme|FR-26|8|11|Q3364
    Eure|FR-27|8|11|Q3372
    Eure-et-Loir|FR-28|8|11|Q3377
    Finistère|FR-29|8|11|Q3389
    Corse-du-Sud|FR-2A|8|11|Q3336
    Haute-Corse|FR-2B|8|11|Q3334
    Gard|FR-30|8|11|Q12515
    Haute-Garonne|FR-31|8|11|Q12538
    Gers|FR-32|8|11|Q12517
    Gironde|FR-33|8|11|Q12526
    Hérault|FR-34|8|11|Q12545
    Ille-et-Vilaine|FR-35|8|11|Q12549
    Indre|FR-36|8|11|Q12553
    Indre-et-Loire|FR-37|8|11|Q12556
    Isère|FR-38|8|11|Q12559
    Jura|FR-39|8|11|Q3120
    Landes|FR-40|8|11|Q12563
    Loir-et-Cher|FR-41|8|11|Q12564
    Loire|FR-42|8|11|Q12569
    Haute-Loire|FR-43|8|11|Q12572
    Loire-Atlantique|FR-44|8|11|Q3068
    Loiret|FR-45|8|11|Q12574
    Lot|FR-46|8|11|Q12576
    Lot-et-Garonne|FR-47|8|11|Q12578
    Lozère|FR-48|8|11|Q12580
    Maine-et-Loire|FR-49|8|11|Q12584
    Manche|FR-50|8|11|Q12589
    Marne|FR-51|8|11|Q12594
    Haute-Marne|FR-52|8|11|Q12607
    Mayenne|FR-53|8|11|Q12620
    Meurthe-et-Moselle|FR-54|8|11|Q12626
    Meuse|FR-55|8|11|Q12631
    Morbihan|FR-56|8|11|Q12642
    Moselle|FR-57|8|11|Q12652
    Nièvre|FR-58|8|11|Q12657
    Nord|FR-59|8|11|Q12661
    Oise|FR-60|8|11|Q12675
    Orne|FR-61|8|11|Q12679
    Pas-de-Calais|FR-62|8|11|Q12689
    Puy-de-Dôme|FR-63|8|11|Q12694
    Pyrénées-Atlantiques|FR-64|8|11|Q12703
    Hautes-Pyrénées|FR-65|8|11|Q12700
    Pyrénées-Orientales|FR-66|8|11|Q12709
    Bas-Rhin|FR-67|8|11|Q12717
    Haute-Rhin|FR-68|8|11|Q12722
    Rhône|FR-69|8|11|Q46130
    Haute-Saône|FR-70|8|11|Q12730
    Saône-et-Loire|FR-71|8|11|Q12736
    Sarthe|FR-72|8|11|Q12740
    Savoie|FR-73|8|11|Q12745
    Haute-Savoie|FR-74|8|11|Q12751
    Seine-Maritime|FR-76|8|11|Q12758
    Seien-et-Marne|FR-77|8|11|Q12753
    Yvelines|FR-78|8|11|Q12820
    Deux-Sèvres|FR-79|8|11|Q12765
    Somme|FR-80|8|11|Q12770
    Tarn|FR-81|8|11|Q12772
    Tarn-et-Garonne|FR-82|8|11|Q12779
    Var|FR-83|8|11|Q12789
    Vaucluse|FR-84|8|11|Q12792
    Vendée|FR-85|8|11|Q12798
    Vienne|FR-86|8|11|Q12804
    Haute-Vienne|FR-87|8|11|Q12808
    Vosges|FR-88|8|11|Q3105
    Yonne|FR-89|8|11|Q12816
    Territoire de Belfort|FR-90|8|11|Q12782
    Essonne|FR-91|8|11|Q3368
    Val-d'Oise|FR-95|8|11|Q12784
    Guyane française|FR-GF|8|11|Q3769
    Guadeloupe|FR-GP|8|11|Q17012
    Martinique|FR-MQ|8|11|Q17054
    La Réunion|FR-RE|8|11|Q17070
    Mayotte|FR-YT|8|11|Q17063
    """;

  public record RegionInfo(String name, String regionIsoCode, double minZoom, double maxZoom, String wikidata) {}

  private static final HashMap<String, RegionInfo> regionInfoByISO;
  private static final HashMap<String, RegionInfo> regionInfoByWikidata;
  static RegionInfo unknownInfo = new RegionInfo("UNKNOWN_REGION", "XX", 8.0, 11.0, "QXXX");

  static {
    regionInfoByISO = new HashMap<>();
    Scanner s = new Scanner(data);
    while (s.hasNextLine()) {
      String line = s.nextLine();
      String[] parts = line.split("\\|");
      regionInfoByISO.put(parts[1],
        new RegionInfo(parts[0], parts[1], Double.parseDouble(parts[2]) - 1.0, Double.parseDouble(parts[3]) - 1.0,
          parts[4]));
    }
  }

  static {
    regionInfoByWikidata = new HashMap<>();
    Scanner s = new Scanner(data);
    while (s.hasNextLine()) {
      String line = s.nextLine();
      String[] parts = line.split("\\|");
      regionInfoByWikidata.put(parts[4],
        new RegionInfo(parts[0], parts[1], Double.parseDouble(parts[2]) - 1.0, Double.parseDouble(parts[3]) - 1.0,
          parts[4]));
    }
  }

  public static RegionInfos.RegionInfo getByISO(SourceFeature sf) {
    // ISO codes aren't always included in extracts, oddly, so introspect which tags are
    //sf.tags().forEach((key, value) -> System.out.printf("%s: %s\n", key, value));

    var isoCode = sf.hasTag("ISO3166-2") ? sf.getString("ISO3166-2") : (sf.hasTag("region_code_iso3166_2") ?
      sf.getString("region_code_iso3166_2") : (sf.hasTag("ISO3166-2") ? sf.getString("ISO3166-2") : "XX-XX"));
    if (regionInfoByISO.containsKey(isoCode)) {
      return regionInfoByISO.get(isoCode);
    }
    return unknownInfo;
  }

  public static RegionInfos.RegionInfo getByWikidata(SourceFeature sf) {
    var wikidata = sf.hasTag("wikidata") ? sf.getString("wikidata") : "QXXX";
    if (regionInfoByWikidata.containsKey(wikidata)) {
      return regionInfoByWikidata.get(wikidata);
    }
    return unknownInfo;
  }

}
