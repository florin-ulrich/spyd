import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;

import java.util.List;

public class Main extends Application {

    //1NiNbhreT71fKfJudjuKaP

    public static void main(String[] args) {
        launch();
    }

    public void start(Stage primaryStage) throws Exception {
        TextField tf = new TextField("enter your spotify playlist id here");
        Button b = new Button("convert this playlist");
        b.setOnAction(e -> convertPlaylist(tf.getText()));
        VBox vb = new VBox(tf, b);
        vb.setPrefWidth(300);
        vb.setSpacing(10);
        primaryStage.setScene(new Scene(vb));
        primaryStage.show();
    }

    private static void convertPlaylist(String playListID) {
        try {
            OkHttpClient client = new OkHttpClient();
            List<Song> songs = new SpotifyPlayListRequest(client, playListID).getSongList();
            YoutubeSongSearch ytss = new YoutubeSongSearch(songs, client);
            ytss.getLinks().forEach(System.out::println);
        } catch (Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }


}
