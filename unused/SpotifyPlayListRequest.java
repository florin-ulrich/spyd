import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// parses song names from the html returned by spotify playlist pages
// succeeded by implementation that instead uses spotifys web api

public class SpotifyPlayListRequest {

    // https://api.spotify.com/v1/playlists/{playlist_id}/tracks
    private static Pattern lineContainsSongInfo = Pattern.compile(".*top-align\\strack-name-wrapper.*");
    private static Pattern songNameAndArtist = Pattern.compile("track-name\"\\sdir=\"auto\">(.*?)</span>.*?<span dir=\"auto\">(.*?)</span>");

    private static String auth = "ZWQ1YzhlODVkNTRiNGUwMWE2ZTNhMGM5MGQyY2Q4M2E6MWJlOGFkNDRiNTIzNGVlNjgzYzQ3Yjk3YWQxNGVmMTA=";

    private String responseBody;

    public SpotifyPlayListRequest(OkHttpClient client, String playListID) {
        Request request = new Request.Builder()
                .url("https://open.spotify.com/playlist/"  + playListID)
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            responseBody = response.body().string();
            replaceBadChars();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public SpotifyPlayListRequest(String responseBody) {
        this.responseBody = responseBody;
        replaceBadChars();
    }

    public static void main(String[] args) throws Exception{
        BufferedReader br = Files.newBufferedReader(Paths.get("C:\\Users\\flori\\OneDrive\\code\\Java\\spyd\\fullplaylistbody"));
        StringBuilder sb = new StringBuilder();
        while (br.ready()) {
            sb.append(br.readLine() + "\n");
        }
        SpotifyPlayListRequest sp = new SpotifyPlayListRequest(sb.toString());
        System.out.println(sp.responseBody);
        sp.getPlayListSongNames().forEach(e -> System.out.println(e.plainString()));
    }

    public List<Song> getPlayListSongNames() {
        List<Song> list = new ArrayList<Song>();
        Scanner s = new Scanner(responseBody);
        while (s.hasNext()) {
            String line = s.nextLine();
            if (lineContainsSongInfo.matcher(line).matches()) {
                addSongNameAndTitle(list, line);
            }
        }
        return list;
    }

    private static void addSongNameAndTitle(List<Song> list, String line) {
        Matcher m = songNameAndArtist.matcher(line);
        if (m.find()) {
            list.add(new Song(m.group(2), m.group(1)));
        }
        else throw new AssertionError("couldnt match supposedly matching line");
    }

    private void replaceBadChars() {
        responseBody = responseBody.replaceAll("&#039;", "'");
    }
}
