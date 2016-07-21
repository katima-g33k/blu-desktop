package controller;

import handler.CopyHandler;
import handler.ItemHandler;
import handler.MemberHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import model.item.Book;
import model.item.Copy;
import model.item.Item;
import model.item.Storage;
import model.transaction.Transaction;
import ressources.Dialog;

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
public class ItemViewController extends Controller {

  private Item item;
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
  @FXML private TableColumn<Copy, String> colAvailableAction;

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
     this.item = item;
     statusUp.setVisible(item instanceof Book);
     statusDown.setVisible(item instanceof Book);
     _displayItem();
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
    return new TableView[]{tblReservations, tblAvailable, tblSold, tblPaid};
  }

  public Button getBtnUpdate() {
    return btnUpdate;
  }

  private void _eventHandlers() {
    btnAddComment.setOnAction(event -> {
      String oldComment = lblComment.getText();
      String newComment = Dialog.input("Modifcation du commentaire", "Veuillez entrer le commentaire que vous souhaitez inscrire :", oldComment);

      if(!oldComment.equals(newComment)) {
        if(itemHandler.updateComment(item.getId(), newComment)) {
          lblComment.setText(newComment);
        } else {
          Dialog.information("Une erreur est survenue");
        }
      }
    });

    statusUp.setOnAction(event -> {
      if (((Book) item).getStatus().equals("REMOVED")) {
        if (itemHandler.setStatus(item.getId(), "OUTDATED")) {
          ((Book) item).setRemoved("");
        }
      } else if (((Book) item).getStatus().equals("OUTDATED")) {
        if (itemHandler.setStatus(item.getId(), "VALID")) {
          ((Book) item).setOutdated("");
        }
      }

      lblStatus.setText(((Book) item).getStatus());
    });

    statusDown.setOnAction(event -> {
      if (((Book) item).getStatus().equals("VALID")) {
        if (itemHandler.setStatus(item.getId(), "OUTDATED")) {
          ((Book) item).setOutdated(new Date());
        }
      } else if (((Book) item).getStatus().equals("OUTDATED")) {
        if (itemHandler.setStatus(item.getId(), "REMOVED")) {
          ((Book) item).setRemoved(new Date());
        }
      }

      lblStatus.setText(((Book) item).getStatus());
    });

    btnStorage.setOnAction(event -> {
      String storage = Dialog.input("Caisses de rangement", "Veuillez noter les caisses de rangement séparer par un \";\" :", lblStorage.getText());
      String[] storageUnits = storage.replace(" ", "").split(";");

      if(itemHandler.updateStorage(item.getId(), storageUnits)) {
        ArrayList<Storage> rangements = new ArrayList<>();

        for (String storageUnit : storageUnits) {
          Storage uniteRangement = new Storage();
          uniteRangement.setCode(storageUnit);
        }

        item.setStorage(rangements);
      } else {
        Dialog.information("Une erreur est survenue");
      }
    });

    btnReserve.setOnAction(event -> {
      String input = "";
      int memberNo = 0;
      boolean isMember = false;

      while (!isMember) {
        try {
          input = Dialog.input("Réserver cet item", "Entrez le numéro de l'étudiant qui fait la réservation");
          memberNo = Integer.parseInt(input);

          MemberHandler memberHandler = new MemberHandler();
          isMember = memberHandler.exist(memberNo);
        } catch (NumberFormatException e) {
          if (input.equals("")) {
            return;
          }
        }
      }

      int id = itemHandler.addItemReservation(memberNo, item.getId());

      if (id > 0) {
        Copy e = new Copy();
        Transaction t = new Transaction();

        t.setId(id);
        t.setType("RESERVE");
        t.setDate(new Date());
        e.addTransaction(t);

        item.addReserved(e);
        _displayCopies();
      }
    });

    btnDisplayReservations.setOnAction(event -> tblReservations.setVisible(!tblReservations.isVisible()));

    tblReservations.setOnMouseClicked(event -> {
      Node node = ((Node) event.getTarget()).getParent();
      TableRow row;

      if (node instanceof TableRow) {
        row = (TableRow) node;
      } else {
        row = (TableRow) node.getParent();
      }

      final Copy e = (Copy) row.getItem();

      if (event.getButton() == MouseButton.SECONDARY) {
        final ContextMenu contextMenu = new ContextMenu();

        MenuItem vendre = new MenuItem("Vendre");
        MenuItem annuler = new MenuItem("Annuler réservation");

        if(e.getId() == 0)
          contextMenu.getItems().addAll(annuler);
        else
          contextMenu.getItems().addAll(vendre, annuler);

        row.setContextMenu(contextMenu);

        vendre.setOnAction(event1 -> {
          int id = itemHandler.addTransaction(e.getMember().getNo(), e.getId(), 3);

          if (id != 0) {
            Transaction t = new Transaction();
            t.setId(id);
            t.setType("SELL");
            t.setDate(new Date());

            item.getReserved().remove(e);
            e.addTransaction(t);
            item.getSold().add(e);

            _displayCopies();
          }
        });

        annuler.setOnAction(event12 -> {
          if(e.getId() == 0) {
            itemHandler.deleteReservation(e.getParent().getNo(), item.getId());
            item.getReserved().remove(e);
          } else {
            itemHandler.deleteReservation(e.getId());
            item.getReserved().remove(e);

            for (int noTransaction = 0; noTransaction < e.getAllTransactions().size(); noTransaction++) {
              if (e.getAllTransactions().get(noTransaction).getType().equals("RESERVE")) {
                e.getAllTransactions().remove(noTransaction);
              }
            }
            item.getAvailable().add(e);
          }

          _displayCopies();
        });
      }
    });

    btnDisplayAvailable.setOnAction(event -> tblAvailable.setVisible(!tblAvailable.isVisible()));

    tblAvailable.setOnMouseClicked(event -> {
      Node node = ((Node) event.getTarget()).getParent();
      TableRow row;

      if (node instanceof TableRow) {
        row = (TableRow) node;
      } else {
        row = (TableRow) node.getParent();
      }

      final Copy c = (Copy) row.getItem();

      if (event.getButton() == MouseButton.SECONDARY) {
        final ContextMenu contextMenu = new ContextMenu();

        MenuItem vendre = new MenuItem("Vendre");
        MenuItem vendreParent = new MenuItem("Vendre à 50%");
        MenuItem modifier = new MenuItem("Modifier le prix");
        MenuItem supprimer = new MenuItem("Supprimer");

        contextMenu.getItems().addAll(vendre, vendreParent, modifier, supprimer);
        row.setContextMenu(contextMenu);

        vendre.setOnAction(event13 -> {
          int id = itemHandler.addTransaction(c.getMember().getNo(), c.getId(), 2);

          if (id != 0) {
            Transaction t = new Transaction();
            t.setId(id);
            t.setType("SELL");
            t.setDate(new Date());

            item.getAvailable().remove(c);
            c.addTransaction(t);
            item.getSold().add(c);

            _displayCopies();
          }
        });

        vendreParent.setOnAction(event14 -> {
          int id = itemHandler.addTransaction(c.getMember().getNo(), c.getId(), 3);

          if (id != 0) {
            Transaction t = new Transaction();
            t.setId(id);
            t.setType("SELL_PARENT");
            t.setDate(new Date());

            item.getAvailable().remove(c);
            c.addTransaction(t);
            item.getSold().add(c);

            _displayCopies();
          }
        });

        modifier.setOnAction(event15 -> {
          boolean isDouble = false;
          double price = c.getPrice();

          while (!isDouble) {
            try {
              price = Double.parseDouble(Dialog.input("Modification du price", "Entrez le nouveau montant :", Double.toString(c.getPrice())));
              isDouble = true;
            } catch (NumberFormatException e1) {
              Dialog.information("Vous devez entrer un montant valide");
            }
          }

          CopyHandler copyHandler = new CopyHandler();
          if(copyHandler.updateCopyPrice(c.getId(), price)) {
            item.getAvailable().remove(c);
            c.setPrice(price);
            item.getAvailable().add(c);

            _displayCopies();
          } else {
            Dialog.information("Une erreur est survenue");
          }
        });

        supprimer.setOnAction(event16 -> {
          CopyHandler copyHandler = new CopyHandler();
          if(copyHandler.deleteCopy(c.getId())) {
            item.getAvailable().remove(c);
            _displayCopies();
          } else {
            Dialog.information("Une erreur est survnue");
          }
        });
      }
    });

    btnSold.setOnAction(event -> tblSold.setVisible(!tblSold.isVisible()));

    tblSold.setOnMouseClicked(event -> {
      Node node = ((Node) event.getTarget()).getParent();
      TableRow row;

      if (node instanceof TableRow) {
        row = (TableRow) node;
      } else {
        row = (TableRow) node.getParent();
      }

      final Copy e = (Copy) row.getItem();

      if (event.getButton() == MouseButton.SECONDARY) {
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem annuler = new MenuItem("Annuler la vente");

        contextMenu.getItems().addAll(annuler);
        row.setContextMenu(contextMenu);

        annuler.setOnAction(event17 -> {
          if (itemHandler.cancelSell(e.getId())) {
            item.getSold().remove(e);

            for (int noTransaction = 0; noTransaction < e.getAllTransactions().size(); noTransaction++) {
              if (e.getAllTransactions().get(noTransaction).getType().equals("SELL") || e.getAllTransactions().get(noTransaction).getType().equals("SELL_PARENT")) {
                e.getAllTransactions().remove(noTransaction);
              }
            }

            item.getAvailable().add(e);
            _displayCopies();
          }
        });
      }
    });

    btnPaid.setOnAction(event -> tblPaid.setVisible(!tblPaid.isVisible()));
  }

  private void _dataBinding() {
    colReservationMember.setCellValueFactory(new PropertyValueFactory<>("reservant"));
    colReservationSeller.setCellValueFactory(new PropertyValueFactory<>("vendeur"));
    colReservationAdded.setCellValueFactory(new PropertyValueFactory<>("dateAjout"));
    colReservationDate.setCellValueFactory(new PropertyValueFactory<>("dateReservation"));
    colReservationPrice.setCellValueFactory(new PropertyValueFactory<>("strPrix"));

    colAvailableSeller.setCellValueFactory(new PropertyValueFactory<>("vendeur"));
    colAvailableAdded.setCellValueFactory(new PropertyValueFactory<>("dateAjout"));
    colAvailablePrice.setCellValueFactory(new PropertyValueFactory<>("strPrix"));
    colAvailableAction.setCellValueFactory(new PropertyValueFactory<>("etiquetteVente"));

    colSoldSeller.setCellValueFactory(new PropertyValueFactory<>("vendeur"));
    colSoldAdded.setCellValueFactory(new PropertyValueFactory<>("dateAjout"));
    colSoldDateSold.setCellValueFactory(new PropertyValueFactory<>("dateVente"));
    colSoldPrice.setCellValueFactory(new PropertyValueFactory<>("strPrix"));

    colPaidSeller.setCellValueFactory(new PropertyValueFactory<>("vendeur"));
    colPaidAdded.setCellValueFactory(new PropertyValueFactory<>("dateAjout"));
    colPaidDateSold.setCellValueFactory(new PropertyValueFactory<>("dateVente"));
    colPaidDatePaid.setCellValueFactory(new PropertyValueFactory<>("dateRemise"));
    colPaidPrice.setCellValueFactory(new PropertyValueFactory<>("strPrix"));
  }

  private void _displayItem() {
    lblTitle.setText(item.getName());
    lblSubject.setText(item.getSubject().getName());
    lblCategory.setText(item.getSubject().getCategory());
    lblEan13.setText(item.getEan13());
    lblStorage.setText(item.getStorageString());
    lblComment.setText(item.getDescription());

    if (item instanceof Book) {
      _displayBook();
    } else {
      lblDescription.setText(item.getDescription());
    }

    _displayCopies();
  }

  private void _displayBook() {
    lblPublication.setText(((Book) item).getPublication());
    lblAuthor.setText(((Book) item).getAuthorString());
    lblEditor.setText(((Book) item).getEditor());
    lblEdition.setText(Integer.toString(((Book) item).getEdition()));
    lblStatus.setText(((Book) item).getStatus());
  }

  private void _displayCopies() {
    ObservableList<Copy> reserve = FXCollections.observableArrayList(item.getReserved());
    ObservableList<Copy> aVendre = FXCollections.observableArrayList(item.getAvailable());
    ObservableList<Copy> vendu = FXCollections.observableArrayList(item.getSold());
    ObservableList<Copy> argentRemis = FXCollections.observableArrayList(item.getPaid());

    tblReservations.setItems(reserve);
    tblAvailable.setItems(aVendre);
    tblSold.setItems(vendu);
    tblPaid.setItems(argentRemis);
  }
}