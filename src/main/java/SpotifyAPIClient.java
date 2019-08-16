import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpotifyAPIClient {

    private String clientToken;
    private String accessToken;
    private OkHttpClient client;

    public SpotifyAPIClient(OkHttpClient client, String clientToken) throws Exception{
        this.client = client;
        this.clientToken = clientToken;
        getAccessToken();
    }

    private void getAccessToken() throws Exception {
        RequestBody rb = new FormBody.Builder().add("grant_type", "client_credentials").build();
        Request request = new Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .post(rb)
                .addHeader("Authorization", "Basic " + clientToken)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        String responseText = response.body().string();
        response.close();
        JsonParser parser = new JsonParser();
        accessToken = parser.parse(responseText).getAsJsonObject().get("access_token").getAsString();
    }

    public List<Song> playListAPICall(String playListID) throws Exception{
        String response = callAPIAndGetBody("https://api.spotify.com/v1/playlists/" + playListID + "/tracks");
        List<Song> songs = parseSongsFromResponse(response);
        String nextURL = parseNextURL(response);
        while (!nextURL.equals("null")) {
            response = callAPIAndGetBody(nextURL);
            songs.addAll(parseSongsFromResponse(response));
            nextURL = parseNextURL(response);
        }
        serializeSongs(songs);
        return songs;
    }

    private String callAPIAndGetBody(String url) throws Exception {
        Request playlist = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken).build();
        Call call = client.newCall(playlist);
        Response response = call.execute();
        return response.body().string();
    }

    private void serializeSongs(List<Song> songs) throws Exception {
        FileOutputStream fileOut = new FileOutputStream("C:\\Users\\flori\\OneDrive\\code\\Java\\spyd\\playlists\\"
                + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".ser");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(songs);
        out.close();
        fileOut.close();
    }

    private String parseNextURL(String response) {
        JsonParser parser = new JsonParser();
        JsonElement jo = parser.parse(response).getAsJsonObject().get("next");
        if (jo.isJsonNull()) return "null";
        else return jo.getAsString();
    }

    private List<Song> parseSongsFromResponse(String response) {
        List<Song> songs = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(response);
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
            songs.add(s);
        }
        return songs;
    }
}
