package com.protomaps.basemap.text;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.awt.Font;
import java.awt.FontFormatException;

public class FontRegistry {

  private class FontBundle {
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

  private HashMap<String, FontBundle> registry;
  private static FontRegistry instance;

  private FontRegistry() {
    this.registry = new HashMap<String, FontBundle>();
  }

  public static synchronized FontRegistry getInstance() {
    if (instance == null) {
      instance = new FontRegistry();
    }
    return instance;
  }

  private static String getTopLevelFolderName(String zipFilePath) throws IOException {
    try (ZipFile zipFile = new ZipFile(zipFilePath)) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = entry.getName();

            if (entry.isDirectory() && entryName.endsWith("/")) {
                int slashIndex = entryName.indexOf('/');
                if (slashIndex == entryName.length() - 1) {
                    return entryName.substring(0, slashIndex);
                }
            }
        }
    }
    return null;
  }

  private static Font readFont(String zipFilePath, String name) {
    Font font = null;

    try (ZipFile zipFile = new ZipFile(zipFilePath)) {
        String topLevelFolder = getTopLevelFolderName(zipFilePath);
        String fileNameInZip = topLevelFolder + "/fonts/" + name + ".ttf";
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

  private static HashMap<String, Integer> readEncoding(String zipFilePath, String name, String version) {
    HashMap<String, Integer> encoding = new HashMap<String, Integer>();

    try (ZipFile zipFile = new ZipFile(zipFilePath)) {
        String topLevelFolder = getTopLevelFolderName(zipFilePath);
        String fileNameInZip = topLevelFolder + "/encoding/" + name + "-v" + version + ".csv";
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

  public synchronized void loadFontBundle(String name, String version, String script) {
    String zipFilePath = "data/sources/pgf-encoding.zip";

    Font font = readFont(zipFilePath, name);

    HashMap<String, Integer> encoding = readEncoding(zipFilePath, name, version);

    FontBundle fontBundle = new FontBundle(name, version, font, encoding);

    registry.put(script, fontBundle);
  }

  public String getName(String script) {
    FontBundle fontBundle = registry.get(script);
    if (fontBundle == null) {
      return null;
    }
    return fontBundle.name;
  }

  public String getVersion(String script) {
    FontBundle fontBundle = registry.get(script);
    if (fontBundle == null) {
      return null;
    }
    return fontBundle.version;
  }

  public Font getFont(String script) {
    FontBundle fontBundle = registry.get(script);
    if (fontBundle == null) {
      return null;
    }
    return fontBundle.font;
  }

  public HashMap<String, Integer> getEncoding(String script) {
    FontBundle fontBundle = registry.get(script);
    if (fontBundle == null) {
      return null;
    }
    return fontBundle.encoding;
  }

  public List<String> getScripts() {
    return new ArrayList<>(registry.keySet());
  }

  public static void main(String[] args) {

    FontRegistry fontRegistry = FontRegistry.getInstance();

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
