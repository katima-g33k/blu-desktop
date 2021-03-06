package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import utility.Dialog;

/**
 * Cette classe controller prend en charge le panneau de gauche.
 * C'est avec celle-ci que l'ont charge chacun des panneau de droite
 *
 * @author Marc
 */
public class WindowController extends Controller {
  @FXML private VBox sideMenu;
  @FXML private Pane window;
  @FXML private Pane mainPanel;

  @FXML private Button btnSearch;
  @FXML private Button btnItemForm;
  @FXML private Button btnMemberForm;
  @FXML private Button btnAdmin;

  @FXML private Button btnBack;

  @FXML private MenuItem exit;
  @FXML private MenuItem settings;
  @FXML private MenuItem about;

  @Override
  public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
    double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
    double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();

    sideMenu.setPrefSize(screenWidth * .15, screenHeight);
    mainPanel.setPrefSize(screenWidth * .85, screenHeight);
    setMainPanel(mainPanel);
    setWindow(window);

    _setText();
    _eventHandlers();
    loadMainPanel("layout/search.fxml");
  }

  private void _eventHandlers() {
    btnSearch.setOnAction(event -> loadMainPanel("layout/search.fxml"));
    btnItemForm.setOnAction(event -> loadMainPanel("layout/itemForm.fxml"));
    btnMemberForm.setOnAction(event -> loadMainPanel("layout/memberForm.fxml"));
    btnAdmin.setOnAction(event -> loadMainPanel("layout/admin.fxml"));

    btnBack.setOnAction(event -> {
      // TODO: Back button
      // if (viewStack.size() > 1) {
      //   View view = viewStack.pop();

      //   while (view.getController() == controller) {
      //     view = viewStack.pop();
      //   }

      //   controller = view.getController();
      //   mainPanel.getChildren().clear();
      //   mainPanel.getChildren().add(view.getPane());
      //   btnBack.setVisible(viewStack.size() > 1);
      // }
    });

    exit.setOnAction(event -> Platform.exit());
    about.setOnAction(event -> Dialog.information("About", "https://github.com/katima-g33k/blu-desktop"));
    settings.setOnAction(event -> {
      try {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/settings.fxml"));
        Parent parent = fxmlLoader.load();
        Stage stage = new Stage();
        Scene scene = new Scene(parent);

        scene.getStylesheets().addAll("css/window.css");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Paramètres");
        stage.setWidth(500);
        stage.setHeight(350);
        stage.setScene(scene);
        stage.show();
      } catch(Exception e) {
        e.printStackTrace();
      }
    });
  }

  private void _setText() {
    initI18n();

    // TODO: i18n
    // btnSearch.setText(i18n.getString("menu.search"));
    // btnItemForm.setText(i18n.getString("menu.item"));
    // btnMemberForm.setText(i18n.getString("menu.member"));
    // btnAdmin.setText(i18n.getString("menu.admin"));
  }
}