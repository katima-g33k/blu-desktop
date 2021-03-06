package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Labeled;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.Node;

import utility.I18N;

/**
 *
 * @author Jessy Lachapelle
 */
public class Controller implements javafx.fxml.Initializable {
  protected static Pane window;
  private static Pane mainPanel;
  @SuppressWarnings("WeakerAccess")
  protected I18N i18n;

  @Override
  public void initialize(URL location, ResourceBundle resources) {}

  protected Pane getMainPanel() {
    return mainPanel;
  }

  protected TableRow getTableRow(Node node) {
    if (node instanceof TableRow) {
      return (TableRow) node;
    }

    if (node.getParent() instanceof TableRow) {
      return (TableRow) node.getParent();
    }

    return new TableRow();
  }

  protected void initI18n() {
    i18n = new I18N();
  }

  @SuppressWarnings("unused")
  protected void initI18n(String lang) {
    i18n = new I18N(lang);
  }

  @SuppressWarnings({"unused", "WeakerAccess"})
  protected void initText(Pane pane) {
    for(Node node : pane.getChildren()) {
      if (node instanceof  Labeled && node.getStyleClass().contains("i18n")) {
        Labeled labeled = ((Labeled) node);
        String key = labeled.getText();
        labeled.setText(i18n.getString(key));
      } else if(node instanceof TableView) {
        TableView table = (TableView) node;
        for (int i = 0; i < table.getColumns().size(); i++) {
          TableColumn column = ((TableColumn) table.getColumns().get(i));
          String key = column.getText();
          column.setText(i18n.getString(key));
        }
      } else if (node instanceof Pane) {
        initText((Pane) node);
      }
    }
  }

  protected boolean isDoubleClick(MouseEvent event) {
    return event.isPrimaryButtonDown() && event.getClickCount() == 2;
  }

  protected boolean isRightClick(MouseEvent event) {
    return event.getButton() == MouseButton.SECONDARY;
  }

  protected Controller loadMainPanel(String resource) {
    return loadPanel(mainPanel, resource);
  }

  protected Controller loadPanel(Pane parent, String resource) {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(WindowController.class.getClassLoader().getResource(resource));
    parent.getChildren().clear();

    try {
      // TODO: Back button
      // Pane pane = loader.load();
      // viewStack.push(pane, loader.getController());
      // btnBack.setVisible(viewStack.size() > 1);

      parent.getChildren().add(loader.load());
      // TODO: i18n
      // initText(parent);
      return loader.getController();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  @SuppressWarnings("unused")
  protected void setI18n(I18N i18n) {
    this.i18n = i18n;
  }

  protected void setMainPanel(Pane pane) {
    mainPanel = mainPanel == null ? pane : mainPanel;
  }

  protected void setWindow(Pane pane) {
    window = window == null ? pane : window;
  }
}
