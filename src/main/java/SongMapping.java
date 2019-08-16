import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SongMapping {

    private Song song;
    private List<SearchResult> potentials;
    private SearchResult bestMatch;

    // JsonArray results = parser.parse(searchResultResponseBody).getAsJsonObject().get("items").getAsJsonArray();
    // List<SearchResult> firstFive = SearchResult.getFirstFive(results, s);

    private static class SearchResult {
        String link;
        String title;
        int score;

        private void calcScore(Song s) {
            String songname = s.getName().replaceAll("\\s", "").toLowerCase();
            String songartist = s.getFirstArtist().replaceAll("\\s", "").toLowerCase();
            String titleSimple = title.replaceAll("\\s", "").toLowerCase();
            if (titleSimple.contains(songname)) score++;
            if (titleSimple.contains(songartist)) score++;
            boolean liveInTitle = songname.contains("live") || songartist.contains("live");
            if (titleSimple.contains("live") && !liveInTitle) score = 0;
        }
    }

    public static List<SongMapping> mapSearchResultsToSongs(HashMap<Song, String> responses) {
        List<SongMapping> list = new ArrayList<>();
        for (Map.Entry<Song, String> entry : responses.entrySet()) {
            list.add(mapFromResponse(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    private static SongMapping mapFromResponse(Song s, String response) {
        SongMapping srm = new SongMapping();
        srm.song = s;
        srm.potentials = parseResultsFromResponse(response);
        srm.findBestMatch(s);
        return srm;
    }

    private static List<SearchResult> parseResultsFromResponse(String response) {
        JsonParser parser = new JsonParser();
        JsonArray results = parser.parse(response).getAsJsonObject().get("items").getAsJsonArray();
        List<SearchResult> list = new ArrayList<>();
        for (int i = 0; i < results.size() && list.size() < 5; i++) {
            JsonObject result = results.get(i).getAsJsonObject();
            SearchResult sr = new SearchResult();
            sr.link = "https://www.youtube.com/watch?v="
                    + result.getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
            sr.title = result.get("snippet").getAsJsonObject().get("title").getAsString();
            list.add(sr);
        }
        return list;
    }

    private void findBestMatch(Song s) {
        bestMatch = potentials.get(0);
        for (SearchResult sr:
             potentials) {
            sr.calcScore(s);
            if (sr.score > bestMatch.score) bestMatch = sr;
        }
    }

    private void openEditorInterface(){

    }


}
