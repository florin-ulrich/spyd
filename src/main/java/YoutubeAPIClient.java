import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import okhttp3.*;
import org.openqa.selenium.json.Json;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeAPIClient {

    private String APIKey;
    private OkHttpClient client;

    public YoutubeAPIClient(String APIKey) {
        this.APIKey = APIKey;
        this.client = new OkHttpClient();
    }

    public HashMap<Song, List<SongMapping.SearchResult>> searchSongList(List<Song> songs) throws Exception {
        HashMap<Song, List<SongMapping.SearchResult>> hm = new HashMap<>();
        for (Song s:
             songs) {
            hm.put(s, searchListAPICall(s));
        }
        return hm;
    }

    private List<SongMapping.SearchResult> searchListAPICall(Song s) throws Exception {
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&q="
                + s.getQueryString()
                + "&key="
                + APIKey;
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return parseResultsFromResponse(response.body().string());
    }

    private List<SongMapping.SearchResult> parseResultsFromResponse(String response) throws Exception {
        JsonParser parser = new JsonParser();
        JsonArray results = parser.parse(response).getAsJsonObject().get("items").getAsJsonArray();
        ObservableList<SongMapping.SearchResult> list = FXCollections.observableArrayList();
        for (int i = 0; i < results.size(); i++) {
            JsonObject result = results.get(i).getAsJsonObject();
            String id = result.getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
            String link = "https://www.youtube.com/watch?v=" + id;
            String title = result.get("snippet").getAsJsonObject().get("title").getAsString();
            int duration = getDuration(id);
            list.add(new SongMapping.SearchResult(link, title, duration));
        }
        return list;
    }

    private static Pattern durationPattern = Pattern.compile("PT(.*)M(.*)S");

    private int getDuration(String id) throws Exception{
        String url = "https://www.googleapis.com/youtube/v3/videos?part=contentDetails&id="
                + id
                + "&key="
                + APIKey;
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        String responsetext = response.body().string();
        JsonParser parser = new JsonParser();
        JsonObject jo = parser.parse(responsetext).getAsJsonObject();
        String duration =  jo
                .get("items")
                .getAsJsonArray()
                .get(0)
                .getAsJsonObject()
                .get("contentDetails")
                .getAsJsonObject()
                .get("duration")
                .getAsString();
        Matcher m = durationPattern.matcher(duration);
        m.find();
        return Integer.parseInt(m.group(1)) * 60 + Integer.parseInt(m.group(2));
    }

    public static void main(String[] args) throws Exception{
        YoutubeAPIClient yapi = new YoutubeAPIClient(Utilities.getYTToken());
        System.out.println(yapi.getDuration("nefDQvrJusc"));
    }

}
