import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
}
