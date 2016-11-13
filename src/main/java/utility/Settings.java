package utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Handle app settings
 * @author Jessy Lachapelle
 * @since 2016-10-01
 * @version 1.0
 */
public class Settings {
  private static final String FILE = "settings.json";
  private static JSONObject settings;

  @SuppressWarnings("ConstantConditions")
  public Settings() {
    if (getClass().getClassLoader().getResource(FILE) != null) {
      File file = new File(getClass().getClassLoader().getResource(FILE).getFile());

      try (Scanner scanner = new Scanner(file)) {
        String jsonString = "";

        while (scanner.hasNext()) {
          jsonString += scanner.nextLine();
        }

        settings = new JSONObject(jsonString);
        scanner.close();
      } catch (FileNotFoundException | JSONException e) {
        e.printStackTrace();
        settings = new JSONObject();
      }
    }
  }

  static public String lang() {
    if (settings.has("lang")) {
      return settings.getString("lang");
    }

    String systemLang = Locale.getDefault().toString().split("_")[0];
    JSONArray supportedLang = settings.getJSONArray("supported_lang");
    if (supportedLang.toList().contains(systemLang)) {
      return systemLang;
    }

    return settings.getString("default_lang");
  }

  static public ArrayList<String> supportedLang() {
    JSONArray jsonArray = settings.getJSONArray("supported_lang");
    ArrayList<String> supportedLang = new ArrayList<>();

    for (int i = 0; i < jsonArray.length(); i++) {
      supportedLang.add(jsonArray.getString(i));
    }

    return supportedLang;
  }

  static public void updateLang(String lang) {
    if (supportedLang().contains(lang)) {
      settings.remove("lang");
      settings.put("lang", lang);
      updateSettings();
    }
  }

  static public String scanFirstChar() {
    return settings.getString("scan_first_char");
  }

  static public String scanLastChar() {
    return settings.getString("scan_last_char");
  }

  static public void updateScanChars(String scanFirstChar, String scanLastChar) {
    settings.remove("scan_first_char");
    settings.remove("scan_last_char");

    settings.put("scan_first_char", scanFirstChar);
    settings.put("scan_last_char", scanLastChar);

    if (updateSettings()) {
      Dialog.confirmation("Les paramètres ont été mis à jour");
    } else {
      Dialog.confirmation("Une erreur est survenue lors de la mise à jour des paramètres");
    }
  }

  static public String apiKey() {
    return settings.has("api_key") ? settings.getString("api_key") : "";
  }

  static public String apiUrl() {
    return settings.has("api_url") ? settings.getString("api_url") : "";
  }

  @SuppressWarnings("ConstantConditions")
  static private boolean updateSettings() {
    try {
      File file = new File(Settings.class.getClassLoader().getResource(FILE).getFile());
      FileWriter fileWriter = new FileWriter(file, false);

      fileWriter.write(settings.toString());
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  @SuppressWarnings("deprecation")
  static public String lastSemester() {
    Date today = new Date();
    int year = today.getYear() + 1900;
    return today.getMonth() < 5 ? "Automne " + (year - 1) : "Hiver " + year;
  }

  @SuppressWarnings("deprecation")
  static public Date[] lastSemesterDates() {
    Date[] range = new Date[2];
    range[0] = new Date();
    range[1] = new Date();
    Date today = new Date();
    int year = today.getYear();

    if (today.getMonth() < 5) {
      range[0].setYear(year - 1);
      range[0].setMonth(5);
      range[1].setYear(year - 1);
      range[1].setMonth(11);
    } else {
      range[0].setYear(year);
      range[0].setMonth(0);
      range[1].setYear(year);
      range[1].setMonth(4);
    }

    range[0].setDate(1);
    range[1].setDate(31);
    return range;
  }
}
