package com.protomaps.basemap.text;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.awt.Font;
import java.awt.FontFormatException;

public class FontRegistry {

  public class FontBundle {
    public String name;
    public String version;
    public Font font;
    public HashMap<String, Integer> encoding;

    public FontBundle(String name, String version, Font font, HashMap<String, Integer> encoding) {
      this.name = name;
      this.version = version;
      this.font = font;
      this.encoding = encoding;
    }
  }

  HashMap<String, FontBundle> registry;
  String pgfEncodingRepoHash;

  public FontRegistry(String pgfEncodingRepoHash) {
    this.registry = new HashMap<String, FontBundle>();
    this.pgfEncodingRepoHash = pgfEncodingRepoHash;
  }

  private static Font readFont(String zipFilePath, String fileNameInZip) {
    Font font = null;

    try (ZipFile zipFile = new ZipFile(zipFilePath)) {
        ZipEntry zipEntry = zipFile.getEntry(fileNameInZip);

        if (zipEntry != null) {
            try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
              font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            }
        } else {
            System.out.println("readFont(): File " + fileNameInZip + " not found in the ZIP archive " + zipFilePath);
            System.exit(1);
        }
    } catch (IOException | FontFormatException e) {
        e.printStackTrace();
        System.exit(1);
    }

    return font;
  }

  public static String getGlyphKey(int index, int xOffset, int yOffset, int xAdvance, int yAdvance) {
    return Integer.toString(index) + "|" +
      Integer.toString(xOffset) + "|" +
      Integer.toString(yOffset) + "|" +
      Integer.toString(xAdvance) + "|" +
      Integer.toString(yAdvance);
  }

  private static HashMap<String, Integer> readEncoding(String zipFilePath, String fileNameInZip) {
    HashMap<String, Integer> encoding = new HashMap<String, Integer>();

    try (ZipFile zipFile = new ZipFile(zipFilePath)) {
        ZipEntry zipEntry = zipFile.getEntry(fileNameInZip);

        if (zipEntry != null) {
            try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
              BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
              reader.readLine(); // skip header
              String line = reader.readLine();
              while (line != null) {
                String[] parts = line.split(",");

                int index = Integer.parseInt(parts[0].trim());
                int xOffset = Integer.parseInt(parts[1].trim());
                int yOffset = Integer.parseInt(parts[2].trim());
                int xAdvance = Integer.parseInt(parts[3].trim());
                int yAdvance = Integer.parseInt(parts[4].trim());
                int codepoint = Integer.parseInt(parts[5].trim());

                String key = getGlyphKey(index, xOffset, yOffset, xAdvance, yAdvance);
                encoding.put(key, codepoint);
                line = reader.readLine();
              }
            }
        } else {
            System.out.println("readEncoding(): File " + fileNameInZip + " not found in the ZIP archive " + zipFilePath);
            System.exit(1);
        }
    } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
    }

    return encoding;
  }

  public void loadFontBundle(String name, String version, String script) {
    String zipFilePath = "data/sources/pgf-encoding.zip";

    String fontFileNameInZip = "wipfli-pgf-encoding-" + pgfEncodingRepoHash + "/fonts/" + name + ".ttf";
    Font font = readFont(zipFilePath, fontFileNameInZip);

    String encodingFileNameInZip = "wipfli-pgf-encoding-" + pgfEncodingRepoHash + "/encoding/" + name + "-v" + version + ".csv";
    HashMap<String, Integer> encoding = readEncoding(zipFilePath, encodingFileNameInZip);

    FontBundle fontBundle = new FontBundle(name, version, font, encoding);

    registry.put(script, fontBundle);
  }

  public String getName(String script) {
    FontBundle fontBundle = registry.get(script);
    if (fontBundle == null) {
      // TODO: handle error
      System.exit(1);
    }
    return fontBundle.name;
  }

  public String getVersion(String script) {
    FontBundle fontBundle = registry.get(script);
    if (fontBundle == null) {
      // TODO: handle error
      System.exit(1);
    }
    return fontBundle.version;
  }

  public Font getFont(String script) {
    FontBundle fontBundle = registry.get(script);
    if (fontBundle == null) {
      // TODO: handle error
      System.exit(1);
    }
    return fontBundle.font;
  }

  public HashMap<String, Integer> getEncoding(String script) {
    FontBundle fontBundle = registry.get(script);
    if (fontBundle == null) {
      // TODO: handle error
      System.exit(1);
    }
    return fontBundle.encoding;
  }

  public List<String> getScripts() {
    return new ArrayList<>(registry.keySet());
  }

  public static void main(String[] args) {

    String pgfEncodingRepoHash = "e9c03fb";
    FontRegistry fontRegistry = new FontRegistry(pgfEncodingRepoHash);

    String name = "NotoSansDevanagari-Regular";
    String version = "1";
    String script = "Devanagari";

    fontRegistry.loadFontBundle(name, version, script);

    System.out.println(fontRegistry.getName(script));
    System.out.println(fontRegistry.getVersion(script));
    System.out.println(fontRegistry.getFont(script));
    System.out.println(fontRegistry.getEncoding(script));
    System.out.println(fontRegistry.getScripts());

  }

}
