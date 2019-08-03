import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;

public class Main extends Application {

    //1NiNbhreT71fKfJudjuKaP

    public static void main(String[] args) {
        launch();
    }

    public void start(Stage primaryStage) throws Exception {
        TextField tf = new TextField("enter your spotify playlist id here");
        Button b = new Button("convert this playlist");
        b.setOnAction(e -> convertPlaylist(tf.getText(), primaryStage));
        VBox vb = new VBox(tf, b);
        vb.setPrefWidth(600);
        vb.setPrefHeight(400);
        vb.setSpacing(10);
        primaryStage.setScene(new Scene(vb));
        primaryStage.show();
    }

    private static void convertPlaylist(String playListID, Stage stage) {
        try {
            OkHttpClient client = new OkHttpClient();
            SpotifyPlayListRequest splr = new SpotifyPlayListRequest(client, playListID, stage);
            stage.setScene(splr.getGUI());
            splr.executeRequest();
            stage.show();
        } catch (Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }


}
