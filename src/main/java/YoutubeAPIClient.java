import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.*;

import java.util.ArrayList;
import java.util.List;

public class YoutubeAPIClient {

    private List<String> links;
    private List<Song> songs;
    private OkHttpClient client;
    private String searchResultResponseBody;
    private List<spydGUI.ProgressListener> progressListeners;

    //https://www.googleapis.com/youtube/v3/search?part=snippet&q=nicki%20minaj&key=[YOUR_API_KEY]

    public YoutubeAPIClient(List<Song> songs, OkHttpClient client) {
        this.songs = songs;
        this.client = client;
        links = new ArrayList<>();
        progressListeners = new ArrayList<>();
    }

    public void executeSearch() throws Exception {
        findLinks();
    }

    private void findLinks() throws Exception {
        for (Song s :
                songs) {
            findBestMatch(s);
        }
    }

    public void attachProgressListener(spydGUI.ProgressListener progressListener) {
        progressListeners.add(progressListener);
    }

    public List<String> getLinks() {
        return links;
    }

    private void findBestMatch(Song s) throws Exception {
        progressListeners.forEach(e -> e.updateCurrentItem("looking for " + s.getQueryString()));
        callAPIAndGetBody(s.getQueryString());
        JsonParser parser = new JsonParser();
        JsonArray results = parser.parse(searchResultResponseBody).getAsJsonObject().get("items").getAsJsonArray();
        List<SearchResult> firstFive = SearchResult.getFirstFive(results, s);
        if (!foundMatch(firstFive)) {
            SearchResult curated = getLinkFromUserInput(firstFive, s);
            progressListeners.forEach(e -> e.updateProgress("found match for " + s.getQueryString() + " from user input -> " + curated.details()));
            links.add(getYTLinkFromID(curated.id));
        }
    }

    private SearchResult getLinkFromUserInput(List<SearchResult> results, Song s) {
        Stage stage = new Stage();
        VBox vb = new VBox();
        Label l = new Label("which of these most accurately matches the following song: "
                + s.getName()
                + " by "
                + s.getFirstArtist()
                + "?");
        Button b = new Button("use this result");
        b.setOnAction(e -> stage.close());
        ComboBox<SearchResult> cb = new ComboBox<>();
        cb.getItems().addAll(results);
        cb.getSelectionModel().selectFirst();
        vb.getChildren().addAll(l, cb, b);
        stage.setScene(new Scene(vb));
        stage.showAndWait();
        return cb.getValue();
    }

    private boolean foundMatch(List<SearchResult> results) {
        return findGoodMatch(results);
    }

    private boolean findGoodMatch(List<SearchResult> results) {
        for (int i = 0; i < results.size(); i++) {
            SearchResult sr = results.get(i);
            if (sr.score == 2) {
                links.add(getYTLinkFromID(sr.id));
                progressListeners.forEach(e -> e.updateProgress("found good match for " + sr.searchtext + " -> " + sr.details()));
                return true;
            }
        }
        return findOkMatch(results);
    }

    private boolean findOkMatch(List<SearchResult> results) {
        for (int i = 0; i < results.size(); i++) {
            SearchResult sr = results.get(i);
            if (sr.score == 1) {
                links.add(getYTLinkFromID(sr.id));
                progressListeners.forEach(e -> e.updateProgress("found ok match for " + sr.searchtext + " -> " + sr.details()));
                return true;
            }
        }
        return false;
    }

    private static String getYTLinkFromID(String id) {
        return "https://www.youtube.com/watch?v=" + id;
    }

    private void callAPIAndGetBody(String songQueryString) throws Exception {
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&q="
                + songQueryString
                + "&key="
                + Utilities.getYTToken();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        searchResultResponseBody = response.body().string();
    }

    public static void main(String[] args) throws Exception {
        OkHttpClient client = new OkHttpClient();
        List<Song> songs = new SpotifyAPIClient(client, "1NiNbhreT71fKfJudjuKaP").getSongList();
        YoutubeAPIClient ytss = new YoutubeAPIClient(songs, client);
        ytss.getLinks().forEach(System.out::println);
    }

    private static class SearchResult {
        private String title;
        private int score;
        private String id;
        private String searchtext;

        private SearchResult(String title, String songname, String songartist, String id, String searchtext) {
            this.title = title;
            this.id = id;
            this.searchtext = searchtext;
            calculateScore(songname, songartist);
        }

        private void calculateScore(String songname, String songartist) {
            songname = songname.replaceAll("\\s", "").toLowerCase();
            songartist = songartist.replaceAll("\\s", "").toLowerCase();
            String titleSimple = title.replaceAll("\\s", "").toLowerCase();
            if (titleSimple.contains(songname)) score++;
            if (titleSimple.contains(songartist)) score++;
            boolean liveInTitle = songname.contains("live") || songartist.contains("live");
            if (titleSimple.contains("live") && !liveInTitle) score = 0;
        }

        private static List<SearchResult> getFirstFive(JsonArray results, Song s) {
            List<SearchResult> list = new ArrayList<>();
            String songname = s.getName();
            String songartist = s.getFirstArtist();
            for (int i = 0; i < results.size() && list.size() < 5; i++) {
                JsonObject result = results.get(i).getAsJsonObject();
                JsonObject resultInfo = result.getAsJsonObject().get("id").getAsJsonObject();
                String id = resultInfo.get("videoId").getAsString();
                String title = result.get("snippet").getAsJsonObject().get("title").getAsString();
                list.add(new SearchResult(title, songname, songartist, id, s.getQueryString()));
            }
            return list;
        }

        private String details() {
            return title + " (" + YoutubeAPIClient.getYTLinkFromID(id) + ")";
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
