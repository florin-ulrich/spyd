import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import okhttp3.*;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeAPIClient {

    private String APIKey;
    private OkHttpClient client;
    private int tokenIndex;

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
        if (response.code() == 200) {
            String responseText = response.body().string();
            System.out.println(responseText);
            return parseResultsFromResponse(responseText);
        }
        else {
            System.out.println(response.body().string());
            APIKey = Utilities.getNextYTToken(tokenIndex++);
            return searchListAPICall(s);
        }
    }

    private List<SongMapping.SearchResult> parseResultsFromResponse(String response) throws Exception {
        JsonParser parser = new JsonParser();
        JsonArray results = parser.parse(response).getAsJsonObject().get("items").getAsJsonArray();
        ObservableList<SongMapping.SearchResult> list = FXCollections.observableArrayList();
        for (int i = 0; i < results.size(); i++) {
            JsonObject result = results.get(i).getAsJsonObject();
            String id = result.getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
            String link = "https://www.youtube.com/watch?v=" + id;
            String title = Utilities.escapeHTML(result.get("snippet").getAsJsonObject().get("title").getAsString());
            int duration = getDuration(id);
            list.add(new SongMapping.SearchResult(link, title, duration));
        }
        return list;
    }

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
        System.out.println(responsetext);
        if (response.code() == 200) {
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
            return parseDuration(duration);
        }
        else {
            System.out.println(responsetext);
            APIKey = Utilities.getNextYTToken(tokenIndex++);
            return getDuration(id);
        }
    }

    private static Pattern hourPattern = Pattern.compile("(\\d*)H");
    private static Pattern minutePattern = Pattern.compile("(\\d*)M");
    private static Pattern secondPattern = Pattern.compile("(\\d*)S");

    private Integer parseDuration(String s) {
        int i = 0;
        Matcher hours = hourPattern.matcher(s);
        Matcher minutes = minutePattern.matcher(s);
        Matcher seconds = secondPattern.matcher(s);
        if (hours.find()) i += Integer.parseInt(hours.group(1)) * 3600;
        if (minutes.find()) i += Integer.parseInt(minutes.group(1)) * 60;
        if (seconds.find()) i += Integer.parseInt(seconds.group(1));
        return i;
    }

    public static void main(String[] args) throws Exception{
        YoutubeAPIClient yapi = new YoutubeAPIClient(Utilities.getYTToken());
        System.out.println(yapi.getDuration("nefDQvrJusc"));
    }



}
