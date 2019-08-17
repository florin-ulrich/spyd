import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.openqa.selenium.json.Json;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    private static String tokenPath = "C:\\Users\\flori\\OneDrive\\code\\Java\\spyd\\tokens";

    public static String getYTToken() {
        JsonParser parser = new JsonParser();
        try {
            String contents = new String(Files.readAllBytes(Paths.get(tokenPath)));
            return parser.parse(contents).getAsJsonObject().get("youtube").getAsString();
        } catch (IOException e) {
            System.out.println("couldnt find YT - token");
            System.exit(0);
        }
        return null;
    }

    public static String getNextYTToken(int index) {
        JsonParser parser = new JsonParser();
        System.out.println("token index: " + index);
        try {
            String contents = new String(Files.readAllBytes(Paths.get(tokenPath)));
            return parser.parse(contents).getAsJsonObject().get("youtube" + index).getAsString();
        } catch (IOException e) {
            System.out.println("couldnt find YT - token");
            System.exit(0);
        }
        return null;
    }

    public static String getSpotifyToken() {
        JsonParser parser = new JsonParser();
        try {
            String contents = new String(Files.readAllBytes(Paths.get(tokenPath)));
            return parser.parse(contents).getAsJsonObject().get("spotify").getAsString();
        } catch (IOException e) {
            System.out.println("couldnt find YT - token");
            System.exit(0);
        }
        return null;
    }

    public static void convertFromFile(String filepath) throws Exception{
        List<String> links = new ArrayList<>();
        BufferedReader br = Files.newBufferedReader(Paths.get(filepath));
        Pattern p = Pattern.compile("https://(.*?)\\)");
        while (br.ready()) {
            String line = br.readLine();
            Matcher m = p.matcher(line);
            m.find();
            links.add(m.group(1));
        }
        links.forEach(System.out::println);
        YTMP3Converter converter = new ytmp3ccConverter();
        converter.downloadLinksToMp3(links, "C:\\Users\\flori\\OneDrive\\spotifylieder");
    }

    public static void openWebPage(String url) {
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String escapeHTML(String s) {
        return s.replaceAll("&quot;", "\"");
    }

    public static void main(String[] args) throws Exception{
        System.out.println(getNextYTToken(0));
        System.out.println(getNextYTToken(1));
        System.out.println(getNextYTToken(2));
    }
}
