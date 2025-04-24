package com.protomaps.basemap.text;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FontRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(FontRegistry.class);

  private record FontBundle(String name, String version, Font font, Map<String, Integer> encoding) {}

  private HashMap<String, FontBundle> registry;
  private static String zipFilePath;
  private static FontRegistry instance;

  private FontRegistry() {
    this.registry = new HashMap<>();
  }

  public static synchronized FontRegistry getInstance() {
    if (instance == null) {
      instance = new FontRegistry();
    }
    return instance;
  }

  private static String getTopLevelFolderName() throws IOException {
    if (zipFilePath == null) {
      return null;
    }

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

  private static Font readFont(String name) {
    Font font = null;

    if (zipFilePath == null) {
      return null;
    }

    try (ZipFile zipFile = new ZipFile(zipFilePath)) {
      String topLevelFolder = getTopLevelFolderName();
      String fileNameInZip = topLevelFolder + "/fonts/" + name + ".ttf";
      ZipEntry zipEntry = zipFile.getEntry(fileNameInZip);

      if (zipEntry != null) {
        try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
          font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
        }
      } else {
        LOGGER.error("readFont(): File {} not found in the ZIP archive {}", fileNameInZip, zipFilePath);
        System.exit(1);
      }
    } catch (IOException | FontFormatException e) {
      LOGGER.error("Error", e);
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

  private static HashMap<String, Integer> readEncoding(String name, String version) {
    HashMap<String, Integer> encoding = new HashMap<>();

    if (zipFilePath == null) {
      return new HashMap<>();
    }

    try (ZipFile zipFile = new ZipFile(zipFilePath)) {
      String topLevelFolder = getTopLevelFolderName();
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
        LOGGER.error("readEncoding(): File {} not found in the ZIP archive {}", fileNameInZip, zipFilePath);
        System.exit(1);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    return encoding;
  }

  public synchronized void setZipFilePath(String zipFilePath_) {
    zipFilePath = zipFilePath_;
  }

  public synchronized void loadFontBundle(String name, String version, String script) {

    if (zipFilePath == null) {
      return;
    }

    Font font = readFont(name);

    HashMap<String, Integer> encoding = readEncoding(name, version);

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

  public Map<String, Integer> getEncoding(String script) {
    FontBundle fontBundle = registry.get(script);
    if (fontBundle == null) {
      return new HashMap<>();
    }
    return fontBundle.encoding;
  }

  public List<String> getScripts() {
    return new ArrayList<>(registry.keySet());
  }
}
