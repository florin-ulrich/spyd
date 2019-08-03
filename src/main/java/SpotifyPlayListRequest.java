import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

// parses song names from the html returned by spotify playlist pages
// succeeded by implementation that instead uses spotifys web api

public class SpotifyPlayListRequest {

    private String currentResponseBody;
    private String accessToken;
    private OkHttpClient client;
    private List<Song> songs;
    private String playListID;
    private GUI gui;

    public SpotifyPlayListRequest(OkHttpClient client, String playListID) {
        this.client = client;
        songs = new ArrayList<>();
        this.playListID = playListID;
    }

    public SpotifyPlayListRequest(OkHttpClient client, String playListID, Stage stage) {
        this.client = client;
        songs = new ArrayList<>();
        this.playListID = playListID;
        gui = new GUI(stage);
    }

    public void executeRequest() throws Exception{
        gui.updateProgress("getting token...");
        accessToken = getToken();
        gui.updateProgress("token received");
        gui.updateProgress("getting playlist songs for playlist: " + playListID + "...");
        getPlayList();
    }

    public Scene getGUI() {
        return gui.createScene();
    }

    private class GUI {
        private VBox vb;
        private TextArea progressTextArea;
        private ScrollPane progressScrollPane;
        private StringJoiner progressTextAreaContent;

        private Button convertToMP3;

        private GUI(Stage stage) {
            progressTextArea = new TextArea();
            progressScrollPane = new ScrollPane(progressTextArea);
            progressTextAreaContent = new StringJoiner("\n");

            convertToMP3 = new Button("convert this playlist");
            convertToMP3.setDisable(true);
            convertToMP3.setOnAction(e -> { //TODO: BAD
                YoutubeSongSearch yss = new YoutubeSongSearch(songs, client);
                stage.setScene(yss.getGUI());
                stage.show();
                try {
                    yss.executeSearch();
                } catch (Exception ex) {
                    //le lenny face
                }
            });

            vb = new VBox(10, progressScrollPane, convertToMP3);
            vb.setPadding(new Insets(5));
            vb.setPrefWidth(600);
            vb.setPrefHeight(400);
        }

        private Scene createScene() {
            return new Scene(vb);
        }

        private void updateProgress(String text) {
            progressTextAreaContent.add(text);
            progressTextArea.setText(progressTextAreaContent.toString());
        }
    }

    private String getToken() throws Exception{

        RequestBody rb = new FormBody.Builder().add("grant_type", "client_credentials").build();
        Request request = new Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .post(rb)
                .addHeader("Authorization", "Basic " + Utilities.getSpotifyToken()).build();
        Call call = client.newCall(request);
        Response response = call.execute();
        String responseText = response.body().string();
        response.close();
        JsonParser parser = new JsonParser();
        return parser.parse(responseText).getAsJsonObject().get("access_token").getAsString();
    }

    private void getPlayList() throws Exception{
        gui.updateProgress("making first api request...");
        callAPIAndGetBody("https://api.spotify.com/v1/playlists/" + playListID + "/tracks");
        parseSongsFromResponse();
        getRemainingSongs();
        gui.updateProgress("done");
        gui.convertToMP3.setDisable(false);
    }

    private void callAPIAndGetBody(String url) throws Exception{
        Request playlist = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken).build();
        Call call = client.newCall(playlist);
        Response response = call.execute();
        currentResponseBody = response.body().string();
    }

    private void getRemainingSongs() throws Exception{
        String next = parseNextURL();
        while(!next.equals("null")) {
            gui.updateProgress("getting more songs...");
            callAPIAndGetBody(next);
            parseSongsFromResponse();
            next = parseNextURL();
        }
        serializeSongs();
    }

    private void serializeSongs() throws Exception{
        FileOutputStream fileOut = new FileOutputStream("C:\\Users\\flori\\OneDrive\\code\\Java\\spyd\\playlists\\" + playListID + ".ser");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(songs);
        out.close();
        fileOut.close();
    }

    private String parseNextURL() {
        JsonParser parser = new JsonParser();
        JsonElement jo = parser.parse(currentResponseBody).getAsJsonObject().get("next");
        if (jo.isJsonNull()) return "null";
        else return jo.getAsString();
    }

    public SpotifyPlayListRequest(String responseBody) {
        this.currentResponseBody = responseBody;
    }

    public List<Song> getSongList() {
        return songs;
    }

    private void parseSongsFromResponse() {
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(currentResponseBody);
        JsonObject jso = jsonTree.getAsJsonObject();
        JsonArray jarr = jso.get("items").getAsJsonArray();
        for (int i = 0; i < jarr.size(); i++) {
            JsonObject track = jarr.get(i).getAsJsonObject().get("track").getAsJsonObject();
            String songname = track.get("name").getAsString();
            JsonArray artistsJson = track.get("artists").getAsJsonArray();
            List<String> artists = new ArrayList<>();
            for (int j = 0; j < artistsJson.size(); j++) {
                artists.add(artistsJson.get(j).getAsJsonObject().get("name").getAsString());
            }
            Song s = new Song(artists, songname);
            gui.updateProgress("found song " + s.getName() + " by " + s.getFirstArtist());
            songs.add(s);
        }
    }

    public static void main(String[] args) throws Exception {
        OkHttpClient client = new OkHttpClient();
        SpotifyPlayListRequest splr = new SpotifyPlayListRequest(client, "3bKdSIKRdubNVTHn4uJ4Ge");
        splr.getSongList().forEach(e -> System.out.println(e.getQueryString()));
        Test.main(new String[]{});
    }
}
