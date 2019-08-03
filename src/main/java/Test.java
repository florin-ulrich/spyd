import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) throws Exception{
        FileInputStream fileIn = new FileInputStream("C:\\Users\\flori\\OneDrive\\code\\Java\\spyd\\playlists\\3bKdSIKRdubNVTHn4uJ4Ge.ser");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        List<Song> l = (List<Song>) in.readObject();
        in.close();
        fileIn.close();
        l.forEach(e -> System.out.println(e.getAllArtists()));
    }
}
