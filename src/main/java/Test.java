import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) throws Exception{
        SpotifyAPIClient spotify = new SpotifyAPIClient(Utilities.getSpotifyToken());
        YoutubeAPIClient youtube = new YoutubeAPIClient(Utilities.getYTToken());
        List<Song> songs = spotify.playListAPICall("2nxIdLzC12Zwt7vxLo1rKU");
        HashMap<Song, List<SongMapping.SearchResult>> results = youtube.searchSongList(songs);
        List<SongMapping> mappings = SongMapping.mapSearchResultsToSongs(results);
        mappings.forEach(SongMapping::printComponents);
    }
}
