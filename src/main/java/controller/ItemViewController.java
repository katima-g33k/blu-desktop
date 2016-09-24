package controller;

import handler.ItemHandler;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import model.item.Book;
import model.item.Copy;
import model.item.Item;
import model.item.Storage;
import utilitiy.Dialog;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Controller de la fenêtre d'une fiche d'item
 *
 * @author Jessy Lachapelle
 * @since 29/11/2015
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess", "unchecked"})
public class ItemViewController extends Controller {
  private ItemHandler itemHandler;

  @FXML private Label lblTitle;
  @FXML private Label lblDescription;
  @FXML private Label lblAuthor;
  @FXML private Label lblEdition;
  @FXML private Label lblEditor;
  @FXML private Label lblPublication;
  @FXML private Label lblEan13;
  @FXML private Label lblComment;
  @FXML private Button btnAddComment;
  @FXML private Label lblStatus;
  @FXML private Label lblCategory;
  @FXML private Label lblSubject;
  @FXML private Button btnStorage;
  @FXML private Label lblStorage;
  @FXML private Button btnReserve;

  @FXML private TableView<?> tblItemStatistics;

  @FXML private Button btnDisplayReservations;
  @FXML private TableView<Copy> tblReservations;
  @FXML private TableColumn<Copy, String> colReservationMember;
  @FXML private TableColumn<Copy, String> colReservationSeller;
  @FXML private TableColumn<Copy, String> colReservationAdded;
  @FXML private TableColumn<Copy, String> colReservationDate;
  @FXML private TableColumn<Copy, String> colReservationPrice;

  @FXML private Button btnDisplayAvailable;
  @FXML private TableView<Copy> tblAvailable;
  @FXML private TableColumn<Copy, String> colAvailableSeller;
  @FXML private TableColumn<Copy, String> colAvailableAdded;
  @FXML private TableColumn<Copy, String> colAvailablePrice;

  @FXML private Button btnSold;
  @FXML private TableView<Copy> tblSold;
  @FXML private TableColumn<Copy, String> colSoldSeller;
  @FXML private TableColumn<Copy, String> colSoldAdded;
  @FXML private TableColumn<Copy, String> colSoldDateSold;
  @FXML private TableColumn<Copy, String> colSoldPrice;

  @FXML private Button btnPaid;
  @FXML private TableView<Copy> tblPaid;
  @FXML private TableColumn<Copy, String> colPaidSeller;
  @FXML private TableColumn<Copy, String> colPaidAdded;
  @FXML private TableColumn<Copy, String> colPaidDateSold;
  @FXML private TableColumn<Copy, String> colPaidDatePaid;
  @FXML private TableColumn<Copy, String> colPaidPrice;

  @FXML private Button btnUpdate;
  @FXML private Button btnDelete;
  @FXML private Button btnMerge;

  @FXML private Button statusUp;
  @FXML private Button statusDown;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    itemHandler = new ItemHandler();
    _eventHandlers();
    _dataBinding();
  }

  public void loadItem(Item item) {
    boolean isBook = item instanceof Book;
    itemHandler.setItem(item);
    statusUp.setVisible(isBook);
    statusDown.setVisible(isBook);
    _displayItem(isBook);
  }

  public void loadItem(int id) {
    loadItem(itemHandler.selectItem(id));
  }

  public void loadItem(String ean13) {
    loadItem(itemHandler.selectItem(ean13));
  }

  public Item getItem() {
    return itemHandler.getItem();
  }

  public TableView[] getCopyTables() {
    return new TableView[]{ tblReservations, tblAvailable, tblSold, tblPaid };
  }

  public Button getBtnUpdate() {
    return btnUpdate;
  }

  private void _eventHandlers() {
    btnAddComment.setOnAction(event -> {
      String title = "Modifcation du commentaire",
             message = "Veuillez entrer le commentaire que vous souhaitez inscrire :",
             oldComment = lblComment.getText(),
             newComment = Dialog.input(title, message, oldComment);

      if (!oldComment.equals(newComment)) {
        if (itemHandler.updateComment(getItem().getId(), newComment)) {
          _displayBook();
        } else {
          Dialog.information("Une erreur est survenue");
        }
      }
    });

    statusUp.setOnAction(event -> {
      if (((Book) getItem()).getStatus().equals("REMOVED")) {
        if (itemHandler.setStatus(getItem().getId(), "OUTDATED")) {
          ((Book) getItem()).setRemoved("");
        }
      } else if (((Book) getItem()).getStatus().equals("OUTDATED")) {
        if (itemHandler.setStatus(getItem().getId(), "VALID")) {
          ((Book) getItem()).setOutdated("");
        }
      }

      lblStatus.setText(((Book) getItem()).getStatus());
    });

    statusDown.setOnAction(event -> {
      if (((Book) getItem()).getStatus().equals("VALID")) {
        if (itemHandler.setStatus(getItem().getId(), "OUTDATED")) {
          ((Book) getItem()).setOutdated(new Date());
        }
      } else if (((Book) getItem()).getStatus().equals("OUTDATED")) {
        if (itemHandler.setStatus(getItem().getId(), "REMOVED")) {
          ((Book) getItem()).setRemoved(new Date());
        }
      }

      lblStatus.setText(((Book) getItem()).getStatus());
    });

    btnStorage.setOnAction(event -> {
      String title = "Caisses de rangement",
             message = "Veuillez noter les caisses de rangement séparer par un \";\" :";
      String input = Dialog.input(title, message, lblStorage.getText());
      String[] storageArray = input.replace(" ", "").split(";");

      if (itemHandler.updateStorage(getItem().getId(), storageArray)) {
        ArrayList<Storage> storage = new ArrayList<>();

        for (String unit : storageArray) {
          Storage storageUnit = new Storage();
          storageUnit.setCode(unit);
        }

        getItem().setStorage(storage);
      } else {
        Dialog.information("Une erreur est survenue");
      }
    });

    btnReserve.setOnAction(event -> {
      // TODO: Handle reservations
//      String input = "";
//      int memberNo = 0;
//      boolean isMember = false;
//
//      while (!isMember) {
//        try {
//          input = Dialog.input("Réserver cet item", "Entrez le numéro de l'étudiant qui fait la réservation");
//          memberNo = Integer.parseInt(input);
//
//          MemberHandler memberHandler = new MemberHandler();
//          isMember = memberHandler.exist(memberNo);
//        } catch (NumberFormatException e) {
//          if (input.equals("")) {
//            return;
//          }
//        }
//      }
//
//      if (itemHandler.addItemReservation(memberNo)) {
//        _displayCopies();
//      }
    });

    btnDisplayReservations.setOnAction(event -> tblReservations.setVisible(!tblReservations.isVisible()));

    tblReservations.setOnMouseClicked(event -> {
      TableRow row = _getTableRow(((Node) event.getTarget()).getParent());
      Copy copy = (Copy) row.getItem();

      if (event.getButton() == MouseButton.SECONDARY) {
        final ContextMenu contextMenu = new ContextMenu();

        MenuItem sell = new MenuItem("Vendre");
        MenuItem update = new MenuItem("Modifier le prix");
        MenuItem cancel = new MenuItem("Annuler réservation");

        if (copy.getId() == 0) {
          contextMenu.getItems().addAll(cancel);
        } else {
          contextMenu.getItems().addAll(sell, update, cancel);
        }

        row.setContextMenu(contextMenu);

        sell.setOnAction(e -> {
          if (itemHandler.addTransaction(copy.getMember().getNo(), copy.getId(), "SELL_PARENT")) {
            _displayCopies();
          }
        });

        update.setOnAction(e -> _updatePrice(copy));

        cancel.setOnAction(e -> {
          // TODO: Handle reservations
//          if (copy.getId() == 0) {
//            itemHandler.deleteReservation(copy.getParent().getNo(), getItem().getId());
//            getItem().getReserved().remove(copy);
//          } else {
//            itemHandler.deleteReservation(copy.getId());
//            getItem().getReserved().remove(copy);
//
//            for (int noTransaction = 0; noTransaction < copy.getAllTransactions().size(); noTransaction++) {
//              if (copy.getAllTransactions().get(noTransaction).getType().equals("RESERVE")) {
//                copy.getAllTransactions().remove(noTransaction);
//              }
//            }
//            getItem().getCopies().add(copy);
//          }
//
//          _displayCopies();
        });
      }
    });

    btnDisplayAvailable.setOnAction(event -> tblAvailable.setVisible(!tblAvailable.isVisible()));

    tblAvailable.setOnMouseClicked(event -> {
      TableRow row = _getTableRow(((Node) event.getTarget()).getParent());
      Copy copy = (Copy) row.getItem();

      if (event.getButton() == MouseButton.SECONDARY) {
        final ContextMenu contextMenu = new ContextMenu();

        MenuItem sell = new MenuItem("Vendre");
        MenuItem sellParent = new MenuItem("Vendre à 50%");
        MenuItem update = new MenuItem("Modifier le prix");
        MenuItem delete = new MenuItem("Supprimer");

        contextMenu.getItems().addAll(sell, sellParent, update, delete);
        row.setContextMenu(contextMenu);

        sell.setOnAction(e -> {
          if (itemHandler.addTransaction(copy.getMember().getNo(), copy.getId(), "SELL")) {
            _displayCopies();
          } else {
            Dialog.information("Une erreur est survenue");
          }
        });

        sellParent.setOnAction(e -> {
          if (itemHandler.addTransaction(copy.getMember().getNo(), copy.getId(), "SELL_PARENT")) {
            _displayCopies();
          } else {
            Dialog.information("Une erreur est survenue");
          }
        });

        update.setOnAction(e -> _updatePrice(copy));

        delete.setOnAction(e -> {
          String message = "Souhaitez-vous vraiment supprimer cet exemplaire appartenant à " + copy.getSeller() + "?";
          if (Dialog.confirmation(message) && itemHandler.deleteCopy(copy.getId())) {
            _displayCopies();
            Dialog.information("L'exemplaire de " + copy.getItem().getName() + " a été supprimé");
          } else {
            Dialog.information("Une erreur est survenue lors de la suppression de l'exemplaire");
          }
        });
      }
    });

    btnSold.setOnAction(event -> tblSold.setVisible(!tblSold.isVisible()));

    tblSold.setOnMouseClicked(event -> {
      TableRow row = _getTableRow(((Node) event.getTarget()).getParent());
      Copy copy = (Copy) row.getItem();

      if (event.getButton() == MouseButton.SECONDARY) {
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem cancel = new MenuItem("Annuler la vente");

        contextMenu.getItems().addAll(cancel);
        row.setContextMenu(contextMenu);

        cancel.setOnAction(e -> {
          if (itemHandler.cancelSell(copy.getId())) {
            _displayCopies();
          } else {
            Dialog.information("Une erreur est survenue lors de l'annulation de la vente");
          }
        });
      }
    });

    btnPaid.setOnAction(event -> tblPaid.setVisible(!tblPaid.isVisible()));

    btnUpdate.setOnAction(event -> ((ItemFormController) loadMainPanel("layout/itemForm.fxml")).loadItem(itemHandler.getItem()));

    for (TableView table : getCopyTables()) {
      table.setOnMousePressed(event -> {
        if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
          TableRow row = _getTableRow(((Node) event.getTarget()).getParent());
          Copy copy = (Copy) row.getItem();

          if (copy != null) {
            ((MemberViewController) loadMainPanel("layout/memberView.fxml")).loadMember(copy.getMember().getNo());
          }
        }
      });
    }
  }

  private void _dataBinding() {
    tblReservations.managedProperty().bind(tblReservations.visibleProperty());
    tblAvailable.managedProperty().bind(tblAvailable.visibleProperty());
    tblSold.managedProperty().bind(tblSold.visibleProperty());
    tblPaid.managedProperty().bind(tblPaid.visibleProperty());

    colReservationMember.setCellValueFactory(new PropertyValueFactory<>("reservee"));
    colReservationSeller.setCellValueFactory(new PropertyValueFactory<>("seller"));
    colReservationAdded.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
    colReservationDate.setCellValueFactory(new PropertyValueFactory<>("dateReserved"));
    colReservationPrice.setCellValueFactory(new PropertyValueFactory<>("priceString"));

    colAvailableSeller.setCellValueFactory(new PropertyValueFactory<>("seller"));
    colAvailableAdded.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
    colAvailablePrice.setCellValueFactory(new PropertyValueFactory<>("priceString"));

    colSoldSeller.setCellValueFactory(new PropertyValueFactory<>("seller"));
    colSoldAdded.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
    colSoldDateSold.setCellValueFactory(new PropertyValueFactory<>("dateSold"));
    colSoldPrice.setCellValueFactory(new PropertyValueFactory<>("priceString"));

    colPaidSeller.setCellValueFactory(new PropertyValueFactory<>("seller"));
    colPaidAdded.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
    colPaidDateSold.setCellValueFactory(new PropertyValueFactory<>("dateSold"));
    colPaidDatePaid.setCellValueFactory(new PropertyValueFactory<>("datePaid"));
    colPaidPrice.setCellValueFactory(new PropertyValueFactory<>("priceString"));

    for (TableView table : getCopyTables()) {
      for (int i = 0; i < table.getColumns().size(); i++) {
        TableColumn tableColumn = (TableColumn) table.getColumns().get(i);
        tableColumn.setCellFactory(column -> new TableCell<Copy, String>() {
          @Override
          protected void updateItem(String data, boolean empty) {
            TableRow<Copy> row = getTableRow();
            boolean deactivated = data != null && row.getItem().getMember().getAccount().isDeactivated();

            super.updateItem(data, empty);
            setText(data != null ? data : "");
            setStyle(deactivated ? "-fx-background-color: grey" : "");
            setTextFill(deactivated ? Color.WHITE : Color.BLACK);
          }
        });
      }
    }
  }

  private void _displayItem(boolean isBook) {
    lblTitle.setText(getItem().getName());
    lblSubject.setText(getItem().getSubject().getName());
    lblCategory.setText(getItem().getSubject().getCategory().getName());
    lblEan13.setText(getItem().getEan13());
    lblStorage.setText(getItem().getStorageString());

    if (isBook) {
      _displayBook();
    } else {
      lblDescription.setText(getItem().getDescription());
    }

    _displayCopies();
  }

  private void _updatePrice(Copy copy) {
    String titre = "Modification du price",
           message = "Entrez le nouveau montant :";
    boolean isDouble = false;
    double price = copy.getPrice();

    while (!isDouble) {
      try {

        price = Double.parseDouble(Dialog.input(titre, message, Double.toString(copy.getPrice())));
        isDouble = true;
      } catch (NumberFormatException e) {
        Dialog.information("Vous devez entrer un montant valide");
      }
    }

    if (itemHandler.updateCopyPrice(copy.getId(), price)) {
      _displayCopies();
    } else {
      Dialog.information("Une erreur est survenue");
    }
  }

  private void _displayBook() {
    lblComment.setText(getItem().getDescription());
    lblPublication.setText(((Book) getItem()).getPublication());
    lblAuthor.setText(((Book) getItem()).getAuthorString());
    lblEditor.setText(((Book) getItem()).getEditor());
    lblEdition.setText(Integer.toString(((Book) getItem()).getEdition()));
    lblStatus.setText(((Book) getItem()).getStatus());
  }

  private void _displayCopies() {
    tblReservations.setItems(FXCollections.observableArrayList(getItem().getReserved()));
    tblAvailable.setItems(FXCollections.observableArrayList(getItem().getAvailable()));
    tblSold.setItems(FXCollections.observableArrayList(getItem().getSold()));
    tblPaid.setItems(FXCollections.observableArrayList(getItem().getPaid()));

    tblReservations.refresh();
    tblAvailable.refresh();
    tblSold.refresh();
    tblPaid.refresh();

    tblReservations.setVisible(!tblReservations.getItems().isEmpty());
    tblAvailable.setVisible(!tblAvailable.getItems().isEmpty());
    tblSold.setVisible(!tblSold.getItems().isEmpty());
    tblPaid.setVisible(!tblPaid.getItems().isEmpty());
  }
}