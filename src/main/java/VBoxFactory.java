import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

public class VBoxFactory {

    public static VBox defaultVBox() {
        VBox vb = new VBox(5);
        vb.setPadding(new Insets(5));
        return vb;
    }
}
