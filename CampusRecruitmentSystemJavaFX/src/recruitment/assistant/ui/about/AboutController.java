package recruitment.assistant.ui.about;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import recruitment.assistant.alert.AlertMaker;
import recruitment.assistant.util.RecruitmentAssistantUtil;

public class AboutController implements Initializable {

    private static final String LINKED_IN = "https://in.linkedin.com/in/sankalpdayal5";
    private static final String FACEBOOK = "http://facebook.com/sankalpdayal00";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AlertMaker.showTrayMessage(String.format("Hello %s!", System.getProperty("user.name")), "Thanks for visiting VIT Placement");
    }

    private void loadWebpage(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e1) {
            e1.printStackTrace();
            handleWebpageLoadException(url);
        }
    }

    private void handleWebpageLoadException(String url) {
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load(url);
        Stage stage = new Stage();
        Scene scene = new Scene(new StackPane(browser));
        stage.setScene(scene);
        stage.setTitle("Genuine Coder");
        stage.show();
        RecruitmentAssistantUtil.setStageIcon(stage);
    }

    @FXML
    private void loadLinkedIN(ActionEvent event) {
        loadWebpage(LINKED_IN);
    }

    @FXML
    private void loadFacecompany(ActionEvent event) {
        loadWebpage(FACEBOOK);
    }
}
