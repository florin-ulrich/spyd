
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.StringJoiner;


// unified gui to handle the process of converting playlist to mp3s
// contains (subject to change):
//      input field for playlist id
//      button to convert playlist (greyed out during process)
//      resource container that models the state of the app
//      log text area logging every step
//      textfield that documents what is being done currently
//      pop ups to initiate next steps
//      option to convert from serialized link file

public class spydGUI extends Application {

    public static void main(String[] args) {
        launch();
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(getGUI());
        primaryStage.show();
    }

    private Label playListIDInL;
    private TextField playListIDInTF;
    private Label destinationFolderL;
    private TextField destinationFolderTF;
    private Button convert;
    private Label progLabel;
    private ProgressBox pb;
    private Label currentItemL;
    private TextField currentItemTF;
    private VBox vb;



    public spydGUI() {
        playListIDInL = new Label("enter the playlist id that you would like to have converted");
        playListIDInTF = new TextField();
        convert = new Button("convert");
        convert.setOnAction(e -> {
            pb.clear();
            currentItemTF.setText("");
            convert.setDisable(true);
            convertPlaylist(playListIDInTF.getText());
        });
        progLabel = new Label("progress");
        pb = new ProgressBox();
        currentItemL = new Label("currently doing");
        currentItemTF = new TextField();
        vb = new VBox(5, playListIDInL, playListIDInTF, convert, currentItemL, currentItemTF, progLabel, pb.sp);
        vb.setPrefWidth(600);
        vb.setPadding(new Insets(5));
    }

    private class ProgressBox {
        private StringJoiner content;
        private TextArea ta;
        private ScrollPane sp;

        private ProgressBox() {
            this.content = new StringJoiner("\n");
            this.ta = new TextArea();
            this.sp = new ScrollPane(ta);
            sp.setFitToWidth(true);
        }

        private void clear() {
            content = new StringJoiner("\n");
            ta.setText("");
        }
    }

    private Scene getGUI() {
        return new Scene(vb);
    }

    private void convertPlaylist(String playListID) {

    }

    private static void exceptionPopUp(Exception e) {
        Stage stage = new Stage();
        Label l = new Label("Error encountered: " + e.getMessage());
        Button b = new Button("ok");
        b.setOnAction(a -> stage.close());
        stage.setScene(new Scene(new VBox(l ,b)));
        stage.showAndWait();
    }

}
