package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.*;

import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import handler.MemberHandler;
import model.member.Member;
import utility.Dialog;

/**
 * La classe qui fait le pont entre la vue(ajoutMembre) et le modèle
 * (gestionnaireMembre)
 *
 * @author Marc
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
public class MemberFormController extends PanelController {
  private final String DEFAULT_STATE = "QC";

  @FXML private Label lblAdd;
  @FXML private VBox commentSection;

  @FXML private TextField txtNo;
  @FXML private TextField txtLastName;
  @FXML private TextField txtFirstName;
  @FXML private TextField txtAddress;
  @FXML private TextField txtZip;
  @FXML private TextField txtCity;
  @FXML private TextField txtEmail;
  @FXML private TextField txtPhoneNumber1;
  @FXML private TextField txtPhoneNumber2;
  @FXML private TextField txtPhoneNote1;
  @FXML private TextField txtPhoneNote2;

  @FXML private TextArea txtComment;

  @FXML private ComboBox<String> cbState;

  @FXML private CheckBox cbGenerateMemberNo;

  @FXML private Button btnSave;
  @FXML private Button btnCancel;

  private TextField[][] txtPhones;
  private MemberHandler memberHandler;
  private boolean insertion;

  @Override
  public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
    txtPhones = new TextField[][]{{txtPhoneNumber1, txtPhoneNote1}, {txtPhoneNumber2, txtPhoneNote2}};

    memberHandler = new MemberHandler();
    _populeState();
    _eventHandlers();

    insertion = true;
    btnCancel.setVisible(false);
  }

  public Member getMember() {
    return memberHandler.getMember();
  }

  public Member loadMember(int memberNo) {
    insertion = true;
    getMember().setNo(memberNo);
    txtNo.setText(Integer.toString(memberNo));

    return getMember();
  }

  Member loadMember(Member member) {
    memberHandler.setMember(member);
    insertion = false;

    lblAdd.setText("Modifier un membre");
    btnSave.setText("Modifier");
    commentSection.setVisible(false);
    txtComment.setVisible(false);
    cbGenerateMemberNo.setSelected(false);
    cbGenerateMemberNo.setVisible(false);
    btnCancel.setVisible(true);

    txtNo.setText(Integer.toString(member.getNo()));
    txtFirstName.setText(member.getFirstName());
    txtLastName.setText(member.getLastName());
    txtAddress.setText(member.getAddress());
    txtZip.setText(member.getZip());
    txtCity.setText(member.getCity());
    txtEmail.setText(member.getEmail());
    cbState.setValue(member.getStateCode());

    for (int i = 0; i < member.getPhone().length; i++) {
      if (member.getPhone(i) != null && member.getPhone(i).getNumber() != null) {
        txtPhones[i][0].setText(member.getPhone(i).getNumber());
        txtPhones[i][1].setText(member.getPhone(i).getNote());
      }
    }

    return getMember();
  }

  Member saveMember() {
    JSONObject form = _toJSON();

    if (insertion && form.length() > 0) {
      return memberHandler.insertMember(form);
    } else if (form.length() > 0) {
      return memberHandler.updateMember(form);
    }

    return getMember();
  }

  private boolean _canSave() {
    TextField[] mandatory = { txtFirstName, txtLastName, txtEmail };

    for (TextField textField : mandatory) {
      if (textField.getText().isEmpty()) {
        return false;
      }
    }

    return !txtNo.getText().isEmpty() || cbGenerateMemberNo.isSelected();
  }


  /**
   * Rajout les choix de cbState au CbBox
   */
  private void _populeState() {
    cbState.setItems(FXCollections.observableArrayList(memberHandler.getStates()));
    cbState.setValue(DEFAULT_STATE);
  }

  private JSONObject _toJSON() {
    JSONObject form = new JSONObject();
    JSONArray phones = new JSONArray();

    try {
      if (cbGenerateMemberNo.isSelected()) {
        form.put("no", _getRandomMemberNo());
      } else if (insertion || Integer.parseInt(txtNo.getText()) != getMember().getNo()) {
        String no = txtNo.getText().length() == 7 ? "20" + txtNo.getText() : txtNo.getText();
        form.put("no", no);
      }

      if (!txtLastName.getText().equals(getMember().getLastName())) {
        form.put("last_name", capitalize(txtLastName.getText()));
      }

      if (!txtFirstName.getText().equals(getMember().getFirstName())) {
        form.put("first_name", capitalize(txtFirstName.getText()));
      }

      if (!txtAddress.getText().equals(getMember().getAddress())) {
        form.put("address", txtAddress.getText());
      }

      if (!txtZip.getText().equals(getMember().getZip())) {
        form.put("zip", txtZip.getText().replaceAll(" ", "").toUpperCase());
      }

      if (!txtCity.getText().equals(getMember().getCity()) || !cbState.getValue().equals(getMember().getStateCode())) {
        JSONObject city = new JSONObject();
        JSONObject state = new JSONObject();

        state.put("code", cbState.getValue());
        city.put("name", capitalize(txtCity.getText()));
        city.put("state", state);
        form.put("city", city);
      }

      if (!txtEmail.getText().equals(getMember().getEmail())) {
        form.put("email", txtEmail.getText());
      }

      if (!txtComment.getText().isEmpty()) {
        JSONObject account = new JSONObject();
        JSONArray commentArray = new JSONArray();
        JSONObject comment = new JSONObject();

        comment.put("comment", txtComment.getText());
        commentArray.put(comment);
        account.put("comment", commentArray);
        form.put("account", account);
      }

      for (int i = 0; i < txtPhones.length; i++) {
        if (!txtPhones[i][0].getText().isEmpty() &&
            (!txtPhones[i][0].getText().equals(getMember().getPhone(i).getNumber()) ||
             !txtPhones[i][1].getText().equals(getMember().getPhone(i).getNote()))) {
          JSONObject phone = new JSONObject();

          if (getMember().getPhone(i).getId() != 0) {
            phone.put("id", getMember().getPhone(i).getId());
          }

          phone.put("number", txtPhones[i][0].getText().replaceAll("[^\\d]", ""));
          phone.put("note", txtPhones[i][1].getText());

          phones.put(phone);
        } else if (txtPhones[i][0].getText().isEmpty() && getMember().getPhone(i).getId() > 0) {
          JSONObject phone = new JSONObject();
          phone.put("id", getMember().getPhone(i).getId());
          phones.put(phone);
        }
      }

      if (phones.length() > 0) {
        form.put("phone", phones);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return form;
  }

  private int _getRandomMemberNo() {
    int no = (int) (Math.random() * (189999999 - 180000000 + 1) + 180000000);

    if (memberHandler.exist(no)) {
      return _getRandomMemberNo();
    }

    return no;
  }

  private void _eventHandlers() {
    btnCancel.setOnAction(e -> ((MemberViewController) loadMainPanel("layout/memberView.fxml")).loadMember(memberHandler.getMember()));

    btnSave.setOnAction(e -> {
      if (_canSave()) {
        Member member = saveMember();

        if (member != null) {
          ((MemberViewController) loadMainPanel("layout/memberView.fxml")).loadMember(member);
        } else {
          Dialog.information("Une erreur est survenue");
        }
      } else {
        Dialog.information("Assurez-vous d'avoir bien rempli tous les champs obligatoires avant d'enregistrer");
      }
    });

    cbGenerateMemberNo.setOnAction(e -> txtNo.setDisable(cbGenerateMemberNo.isSelected()));

    txtNo.setOnKeyReleased(e -> cbGenerateMemberNo.setDisable(!txtNo.getText().isEmpty()));
  }

  private String capitalize(String string) {
    if (string == null || string.isEmpty()) {
      return "";
    }

    return string.substring(0, 1).toUpperCase() + string.substring(1);
  }

  @Override
  protected void handleScan(String code, boolean isItem) {}
}
