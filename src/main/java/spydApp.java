import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class spydApp extends Application {

    private SpotifyAPIClient spotifyAPIClient;
    private YoutubeAPIClient youtubeAPIClient;
    private YTMP3Converter ytmp3Converter;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        spotifyAPIClient = new SpotifyAPIClient(Utilities.getSpotifyToken());
        youtubeAPIClient = new YoutubeAPIClient(Utilities.getYTToken());
        ytmp3Converter = new ytmp3ccConverter();
        Stage mainstage = getMainstage();
        mainstage.show();
    }

    private Stage getMainstage() {
        TextField playlistID = new TextField("https://open.spotify.com/playlist/2ZERChLsZSC0bqra91OoC4");
        TextField downloadPath = new TextField("C:\\Users\\flori\\OneDrive\\code\\Java\\spyd\\testdownloads");
        Button convert = new Button("convert");
        Button explorer = new Button("...");
        HBox convertBox = new HBox(5, playlistID, convert);
        HBox downloadBox = new HBox(5, downloadPath, explorer);
        Label l = new Label("enter your playlist id or link:");
        Label l1 = new Label("enter your download path:");
        VBox vb = new VBox(5, l, convertBox, l1, downloadBox);
        DirectoryChooser dc = new DirectoryChooser();
        Stage stage = new Stage();

        playlistID.setPrefWidth(325);
        convert.setPrefWidth(75);
        downloadPath.setPrefWidth(375);
        explorer.setPrefWidth(25);
        vb.setPadding(new Insets(5));

        convert.setOnAction(a -> {
            Matcher m = Pattern.compile(".*/(.*)$").matcher(playlistID.getText());
            String path = downloadPath.getText();
            if (new File(path).exists()) {
                try {
                    if (m.find()) convert(m.group(1), downloadPath.getText());
                    else convert(playlistID.getText(), downloadPath.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                showErrorMessage("invalid path");
            }
        });
        explorer.setOnAction(a -> {
            File file = dc.showDialog(stage);
            if (file != null) {
                downloadPath.setText(file.getAbsolutePath());
            }
        });

        stage.setScene(new Scene(vb));
        return stage;
    }

    private void showErrorMessage(String errormsg) {
        Stage s = new Stage();
        Button b = new Button("ok");
        VBox vb = VBoxFactory.defaultVBox();

        vb.getChildren().addAll(new Label("ERROR: " + errormsg), b);
        vb.setMinWidth(200);
        vb.setMinHeight(70);
        vb.setAlignment(Pos.CENTER);

        b.setLayoutX(0.5);
        b.setOnAction(e -> s.close());

        s.initModality(Modality.APPLICATION_MODAL);
        s.setScene(new Scene(vb));
        s.showAndWait();
    }

    private void convert(String playListID, String downloadPath) throws Exception {
        List<Song> songs = spotifyAPIClient.playListAPICall(playListID);
        HashMap<Song, List<SongMapping.SearchResult>> results = youtubeAPIClient.searchSongList(songs);
        List<SongMapping> mappings = SongMapping.mapSearchResultsToSongs(results);
        Stage editingStage = getEditingStage(mappings, downloadPath);
        editingStage.show();
    }

    private Stage getEditingStage(List<SongMapping> mappings, String downloadPath) {
        Stage stage = new Stage();
        TableView<FXMappingWrapper> t = createTable(mappings);
        Button convert = new Button("convert with these choices");
        convert.setOnAction(e -> {
            stage.close();
            ytmp3Converter.downloadLinksToMp3(mappings
                    .stream()
                    .map(SongMapping::getBestMatchLink)
                    .collect(Collectors.toList())
                    , downloadPath);
        });
        convert.setLayoutX(0.5);
        VBox vb = new VBox(5, t, convert);
        vb.setPadding(new Insets(5));
        Scene sc = new Scene(vb, t.getPrefWidth(), vb.getPrefHeight());
        stage.setScene(sc);
        return stage;
    }

    private static TableView<FXMappingWrapper> createTable(List<SongMapping> mappings) {

        TableColumn<FXMappingWrapper, String> songname = new TableColumn<>("Song");
        TableColumn<FXMappingWrapper, String> bestMatchName = new TableColumn<>("Match");
        TableColumn<FXMappingWrapper, Number> bestMatchScore = new TableColumn<>("Score");
        TableColumn<FXMappingWrapper, Hyperlink> edit = new TableColumn<>("");

        songname.setCellValueFactory(new PropertyValueFactory<>("songname"));
        bestMatchName.setCellValueFactory(p -> p.getValue().bestMatchName);
        bestMatchScore.setCellValueFactory(p -> p.getValue().bestMatchScore);
        edit.setCellValueFactory(new PropertyValueFactory<>("edit"));

        TableView<FXMappingWrapper> table = new TableView<>();
        table.getColumns().addAll(songname, bestMatchName, bestMatchScore, edit);
        table.setItems(wrapMappings(mappings));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefWidth(700);

        return table;
    }

    private static ObservableList<FXMappingWrapper> wrapMappings(List<SongMapping> mappings) {
        return FXCollections.observableList(mappings.stream().map(FXMappingWrapper::new).collect(Collectors.toList()));
    }

    public static class FXMappingWrapper {
        private SongMapping sm;
        private Hyperlink edit;
        private Hyperlink delete;
        private String songname;
        private SimpleIntegerProperty bestMatchScore;
        private SimpleStringProperty bestMatchName;

        private FXMappingWrapper(SongMapping sm) {
            this.sm = sm;
            edit = new Hyperlink("edit");
            edit.setOnAction(e -> sm.showEditorInterface());
            songname = sm.getSong().getQueryString();
            bestMatchName = sm.bestMatchNameProperty();
            bestMatchScore = sm.bestMatchScoreProperty();
        }

        public Hyperlink getEdit() {
            return edit;
        }

        public Hyperlink getDelete() {
            return delete;
        }

        public String getSongname() {
            return songname;
        }
    }

}
