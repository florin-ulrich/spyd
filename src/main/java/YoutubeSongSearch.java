import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class YoutubeSongSearch {

    private List<String> links;
    private List<Song> songs;
    private OkHttpClient client;
    private String searchResultResponseBody;
    private GUI gui;

    //https://www.googleapis.com/youtube/v3/search?part=snippet&q=nicki%20minaj&key=[YOUR_API_KEY]

    public YoutubeSongSearch(List<Song> songs, OkHttpClient client) {
        this.songs = songs;
        this.client = client;
        links = new ArrayList<>();
        gui = new GUI();
    }

    private class GUI {
        private VBox vb;
        private TextField doing;

        private Label progressLabel;
        private TextArea progressTextArea;
        private ScrollPane progressScrollPane;
        private StringJoiner progressTextAreaContent;

        private Button convertToMP3;

        private GUI() {
            doing = new TextField();
            progressLabel = new Label("Progress:");
            progressTextArea = new TextArea();
            progressScrollPane = new ScrollPane(progressTextArea);
            progressTextAreaContent = new StringJoiner("\n");
            convertToMP3 = new Button("convert");
            convertToMP3.setDisable(true);
            vb = new VBox(10, progressLabel, progressScrollPane, convertToMP3);
            vb.setPadding(new Insets(5));
            vb.setPrefWidth(600);
            vb.setPrefHeight(400);
        }

        private Scene createScene() {
            return new Scene(vb);
        }

        private void updateProgressLookingFor(Song s) {
            progressTextAreaContent.add("looking for: \"" + s.getQueryString() + "\"");
            progressTextArea.setText(progressTextAreaContent.toString());
        }

        private void updateProgressFoundAutomaticallyGood(SearchResult sr) {
            progressTextAreaContent.add("found good match for: " + formatInfo(sr));
            progressTextArea.setText(progressTextAreaContent.toString());
        }

        private void updateProgressFoundAutomaticallyOk(SearchResult sr) {
            progressTextAreaContent.add("found ok match for: " + formatInfo(sr));
            progressTextArea.setText(progressTextAreaContent.toString());
        }

        private void updateProgressFoundFromUser(SearchResult sr) {
            progressTextAreaContent.add("found user match for: " + formatInfo(sr));
            progressTextArea.setText(progressTextAreaContent.toString());
        }

        private String formatInfo(SearchResult sr) {
            return searchResultQuery(sr) + " -> " + searchResultContent(sr);
        }

        private String searchResultQuery(SearchResult sr) {
            return "\"" + sr.searchtext + "\"";
        }

        private String searchResultContent(SearchResult sr) {
            return sr.title + "(" + getYTLinkFromID(sr.id) + ")";
        }
    }

    public void executeSearch() throws Exception {
        findLinks();
    }

    public Scene getGUI() {
        return gui.createScene();
    }

    private void findLinks() throws Exception {
        for (Song s :
                songs) {
            findBestMatch(s);
        }
    }

    public List<String> getLinks() {
        return links;
    }

    private void findBestMatch(Song s) throws Exception {
        // get response
        // parse response and get best match
        gui.doing.setText("looking for " + s.getQueryString());
        gui.updateProgressLookingFor(s);
        callAPIAndGetBody(s.getQueryString());
        JsonParser parser = new JsonParser();
        System.out.println(searchResultResponseBody);
        JsonArray results = parser.parse(searchResultResponseBody).getAsJsonObject().get("items").getAsJsonArray();
        List<SearchResult> firstFive = SearchResult.getFirstFive(results, s);
        if (!foundMatch(firstFive)) {
            SearchResult curated = getLinkFromUserInput(firstFive, s);
            gui.updateProgressFoundFromUser(curated);
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
                gui.updateProgressFoundAutomaticallyGood(sr);
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
                gui.updateProgressFoundAutomaticallyOk(sr);
                return true;
            }
        }
        return false;
    }

    private String getYTLinkFromID(String id) {
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
        List<Song> songs = new SpotifyPlayListRequest(client, "1NiNbhreT71fKfJudjuKaP").getSongList();
        YoutubeSongSearch ytss = new YoutubeSongSearch(songs, client);
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

        @Override
        public String toString() {
            return title;
        }
    }
}
