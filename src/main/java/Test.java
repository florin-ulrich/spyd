import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) throws Exception{
        FileInputStream fis = new FileInputStream("C:\\Users\\flori\\OneDrive\\code\\Java\\spyd\\finalmappings\\20190818_2139.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        HashMap<Song, String> songs = (HashMap<Song, String>) ois.readObject();
        songs.keySet().forEach(System.out::println);
        songs.values().forEach(System.out::println);
    }
}
