import okhttp3.*;

import java.util.HashMap;
import java.util.List;

public class YoutubeAPIClient {

    private String APIKey;
    private OkHttpClient client;

    public YoutubeAPIClient(String APIKey) {
        this.APIKey = APIKey;
        this.client = new OkHttpClient();
    }

    public HashMap<Song, String> searchSongList(List<Song> songs) throws Exception {
        HashMap<Song, String> hm = new HashMap<>();
        for (Song s:
             songs) {
            hm.put(s, searchListAPICall(s));
        }
        return hm;
    }

    private String searchListAPICall(Song s) throws Exception {
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&q="
                + s.getQueryString()
                + "&key="
                + APIKey;
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return response.body().string();
    }
}
