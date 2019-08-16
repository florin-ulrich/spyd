import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SpotifyAPIClient {

    private String currentResponseBody;
    private String accessToken;
    private OkHttpClient client;
    private List<Song> songs;
    private String playListID;
    private List<spydGUI.ProgressListener> progressListeners;

    public SpotifyAPIClient(OkHttpClient client, String playListID) {
        this.client = client;
        songs = new ArrayList<>();
        this.playListID = playListID;
        this.progressListeners = new ArrayList<>();
        this.progressListeners = new ArrayList<>();
    }

    public void attachProgressListener(spydGUI.ProgressListener progressListener) {
        progressListeners.add(progressListener);
    }

    public void executeRequest() throws Exception {
        progressListeners.forEach(e -> e.updateProgress("starting Spotify request . . . . . ."));
        accessToken = getToken();
        progressListeners.forEach(e -> e.updateProgress("token received"));
        getPlayList();
    }

    private String getToken() throws Exception {
        progressListeners.forEach(e -> e.updateCurrentItem("token"));
        progressListeners.forEach(e -> e.updateProgress("getting token"));
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

    private void getPlayList() throws Exception {
        progressListeners.forEach(e -> e.updateCurrentItem("getting playlist tracks"));
        progressListeners.forEach(e -> e.updateProgress("making first api request"));
        callAPIAndGetBody("https://api.spotify.com/v1/playlists/" + playListID + "/tracks");
        parseSongsFromResponse();
        getRemainingSongs();
        progressListeners.forEach(e -> e.updateProgress("finished requests"));
    }

    private void callAPIAndGetBody(String url) throws Exception {
        Request playlist = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken).build();
        Call call = client.newCall(playlist);
        Response response = call.execute();
        currentResponseBody = response.body().string();
    }

    private void getRemainingSongs() throws Exception {
        String next = parseNextURL();
        while (!next.equals("null")) {
            progressListeners.forEach(e -> e.updateProgress("getting more songs"));
            callAPIAndGetBody(next);
            parseSongsFromResponse();
            next = parseNextURL();
        }
        serializeSongs();
    }

    private void serializeSongs() throws Exception {
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
            progressListeners.forEach(e -> e.updateProgress("found song " + s.getName() + " by " + s.getFirstArtist()));
            songs.add(s);
        }
    }

    public static void main(String[] args) throws Exception {
        OkHttpClient client = new OkHttpClient();
        SpotifyAPIClient splr = new SpotifyAPIClient(client, "3bKdSIKRdubNVTHn4uJ4Ge");
        splr.getSongList().forEach(e -> System.out.println(e.getQueryString()));
    }
}
