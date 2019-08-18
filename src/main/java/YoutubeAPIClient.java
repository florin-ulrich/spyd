import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import okhttp3.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
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
        JsonArray responseJarr = parser.parse(response).getAsJsonObject().get("items").getAsJsonArray();
        ObservableList<SongMapping.SearchResult> searchResults = FXCollections.observableArrayList();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < responseJarr.size(); i++) {
            JsonObject result = responseJarr.get(i).getAsJsonObject();
            String id = result.getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
            String link = "https://www.youtube.com/watch?v=" + id;
            String title = Utilities.escapeHTML(result.get("snippet").getAsJsonObject().get("title").getAsString());
            ids.add(id);
            searchResults.add(new SongMapping.SearchResult(link, title));
        }
        List<Integer> durations = getDurations(ids);
        for (int i = 0; i < searchResults.size(); i++) {
            searchResults.get(i).setDuration(durations.get(i));
        }
        return searchResults;
    }

    private List<Integer> getDurations(List<String> ids) throws Exception {
        StringJoiner sj = new StringJoiner(",");
        ids.forEach(sj::add);
        String url = "https://www.googleapis.com/youtube/v3/videos?part=contentDetails&id="
                + sj.toString()
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
            JsonArray items =  jo.get("items").getAsJsonArray();
            return parseDurations(items);
        }
        else {
            System.out.println(responsetext);
            APIKey = Utilities.getNextYTToken(tokenIndex++);
            return getDurations(ids);
        }
    }

    private static Pattern hourPattern = Pattern.compile("(\\d*)H");
    private static Pattern minutePattern = Pattern.compile("(\\d*)M");
    private static Pattern secondPattern = Pattern.compile("(\\d*)S");

    private List<Integer> parseDurations(JsonArray items) {
        List<Integer> durations = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            String duration = items
                    .get(i)
                    .getAsJsonObject()
                    .get("contentDetails")
                    .getAsJsonObject()
                    .get("duration")
                    .getAsString();
            int j = 0;
            Matcher hours = hourPattern.matcher(duration);
            Matcher minutes = minutePattern.matcher(duration);
            Matcher seconds = secondPattern.matcher(duration);
            if (hours.find()) j += Integer.parseInt(hours.group(1)) * 3600;
            if (minutes.find()) j += Integer.parseInt(minutes.group(1)) * 60;
            if (seconds.find()) j += Integer.parseInt(seconds.group(1));
            durations.add(j);
        }
        return durations;
    }

    public static void main(String[] args) throws Exception{
        YoutubeAPIClient yapi = new YoutubeAPIClient(Utilities.getYTToken());
        ArrayList<String> asdasd = new ArrayList<>();
        asdasd.add("nefDQvrJusc");
        asdasd.add("9hVD2XcXSZ0");
        yapi.getDurations(asdasd).forEach(System.out::println);
    }



}
