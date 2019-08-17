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

    public static String getYTToken() {
        try(BufferedReader br = Files.newBufferedReader(Paths.get("C:\\Users\\flori\\OneDrive\\code\\Java\\spyd\\tokens"))) {
            return br.readLine();
        } catch (IOException e) {
            System.out.println("couldnt find YT - token");
        }
        return null;
    }

    public static String getSpotifyToken() {
        try(BufferedReader br = Files.newBufferedReader(Paths.get("C:\\Users\\flori\\OneDrive\\code\\Java\\spyd\\tokens"))) {
            br.readLine();
            return br.readLine();
        } catch (IOException e) {
            System.out.println("couldnt find Spotify - token");
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
        convertFromFile("C:\\Users\\flori\\OneDrive\\code\\Java\\spyd\\usable");
    }
}
