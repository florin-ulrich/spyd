import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

// parses song names from the html returned by spotify playlist pages
// succeeded by implementation that instead uses spotifys web api

public class SpotifyPlayListRequest {

    private String currentResponseBody;
    private String accessToken;
    private OkHttpClient client;
    private List<Song> songs;
    private String playListID;

    public SpotifyPlayListRequest(OkHttpClient client, String playListID) throws Exception {
        this.client = client;
        accessToken = getToken();
        songs = new ArrayList<>();
        this.playListID = playListID;
        getPlayList();
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
        callAPIAndGetBody("https://api.spotify.com/v1/playlists/" + playListID + "/tracks");
        parseSongsFromResponse();
        getRemainingSongs();
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
            songs.add(new Song(artists, songname));
        }
    }

    public static void main(String[] args) throws Exception {
        OkHttpClient client = new OkHttpClient();
        SpotifyPlayListRequest splr = new SpotifyPlayListRequest(client, "3bKdSIKRdubNVTHn4uJ4Ge");
        splr.getSongList().forEach(e -> System.out.println(e.getQueryString()));
        Test.main(new String[]{});
    }
}
