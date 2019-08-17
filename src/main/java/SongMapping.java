import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SongMapping {

    private Song song;
    private List<SearchResult> potentials;
    private SearchResult bestMatch;
    private SimpleStringProperty bestMatchName;
    private SimpleIntegerProperty bestMatchScore;

    // JsonArray results = parser.parse(searchResultResponseBody).getAsJsonObject().get("items").getAsJsonArray();
    // List<SearchResult> firstFive = SearchResult.getFirstFive(results, s);

    public Song getSong() {
        return song;
    }

    public SimpleStringProperty bestMatchNameProperty() {
        return bestMatchName;
    }

    public SimpleIntegerProperty bestMatchScoreProperty() {
        return bestMatchScore;
    }

    public static List<SongMapping> mapSearchResultsToSongs(HashMap<Song, List<SearchResult>> results) {
        List<SongMapping> list = new ArrayList<>();
        for (Map.Entry<Song, List<SearchResult>> entry : results.entrySet()) {
            list.add(mapFromResponse(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    public static class SearchResult {
        private String link;
        private String title;
        private int score;
        private int duration;

        public SearchResult(String link, String title, int duration) {
            this.link = link;
            this.title = title;
            this.duration = duration;
        }

        private void calcScore(Song s) {
            String songname = s
                    .getName()
                    .replaceAll("\\s", "")
                    .toLowerCase()
                    .replaceAll("\\(.*?\\)", ""); // brackets usually only contain useless info
            String songartist = s
                    .getFirstArtist()
                    .replaceAll("\\s", "")
                    .toLowerCase();
            String titleSimple = title
                    .replaceAll("\\s", "")
                    .toLowerCase();
            if (titleSimple.contains(songname)) score++;
            if (titleSimple.contains(songartist)) score++;
            boolean liveInTitle = songname.contains("live") || songartist.contains("live");
            if (Math.abs(s.getDuration() - duration) <= 5) score += 2;
            if (titleSimple.contains("live") && !liveInTitle) score = 0;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private static SongMapping mapFromResponse(Song s, List<SearchResult> results) {
        SongMapping srm = new SongMapping();
        srm.song = s;
        srm.potentials = results;
        srm.findBestMatch(s);
        return srm;
    }

    private void findBestMatch(Song s) {

        for (SearchResult sr :
                potentials) {
            sr.calcScore(s);
            if (bestMatch == null) {
                bestMatch = sr;
                bestMatchName = new SimpleStringProperty(bestMatch.title);
                bestMatchScore = new SimpleIntegerProperty(bestMatch.score);
            } else if (sr.score > bestMatch.score) setBestMatch(sr);
        }
    }

    public void printComponents() {
        System.out.println(song);
        System.out.println("best match: " + bestMatch);
        potentials.forEach(System.out::println);
    }

    public void showEditorInterface() {
        Label songInfo = new Label(String.format("SONG INFO%nname: %s%nartist: %s", song.getName(), song.getFirstArtist()));
        Label selected = new Label(String.format("CURRENTLY SELECTED%n%s", bestMatch));
        Label newChoiceLabel = new Label("choose a better fit");
        Stage stage = new Stage();
        ComboBox<SearchResult> results = new ComboBox<>();
        VBox vb = VBoxFactory.LargeSpacing();
        Scene sc = new Scene(vb);
        Hyperlink showSelected = new Hyperlink(bestMatch.link);
        VBox selectedInfoContainer = new VBox(selected, showSelected);
        VBox newChoiceContainer = new VBox(newChoiceLabel, results);

        vb.getChildren().addAll(songInfo, selectedInfoContainer, newChoiceContainer);
        results.getItems().addAll(potentials);
        results.getSelectionModel().select(bestMatch);

        results.setOnAction(e -> {
            setBestMatch(results.getValue());
            selected.setText(String.format("CURRENTLY SELECTED%n%s", bestMatch));
            showSelected.setText(bestMatch.link);
        });
        showSelected.setOnAction(e -> Utilities.openWebPage(showSelected.getText()));

        stage.setScene(sc);
        stage.showAndWait();
    }

    private void setBestMatch(SearchResult sr) {
        bestMatch = sr;
        bestMatchName.set(sr.title);
        bestMatchScore.set(sr.score);
    }

    public String getBestMatchLink() {
        return bestMatch.link;
    }
}
