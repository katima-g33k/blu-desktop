package handler;

import api.APIConnector;
import model.member.*;
import java.util.ArrayList;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Permet de reprendre des ResulSet et de les transformer en objet et de
 * transférer des objets de type Member à des requêtes de BD
 *
 * @author Jessy Lachapelle
 * @since 29/10/2015
 * @version 0.3
 */
@SuppressWarnings({"unused", "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection"})
public class MemberHandler {
  private ArrayList<Member> members;
  private Member member;

  /**
   * Constructeur par défaut
   */
  public MemberHandler() {
    this.member = new Member();
    this.members = new ArrayList<>();
  }

  public void setMember(int no) {
    member = selectMember(no);
  }

  public void setMember(Member member) {
    this.member = member;
  }

  public Member getMember(int no) {
    selectMember(no);
    return member;
  }

  public Member getMember() {
    return member;
  }

  public Member selectMember(int no) {
    Member member = new Member();
    JSONObject json = new JSONObject();
    JSONObject data = new JSONObject();

    try {
      data.put("no", no);

      json.put("function", "select");
      json.put("object", "member");
      json.put("data", data);

      JSONObject memberData = APIConnector.call(json).getJSONObject("data");
      member.fromJSON(memberData);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return member;
  }

  /**
   * Search for members with corresponding query
   * @param search The search query
   * @param deactivated True if want to search in deactivated accounts
   * @return A List of members
   */
  public ArrayList<Member> searchMembers(String search, boolean deactivated) {
    ArrayList<Member> members = new ArrayList<>();
    JSONObject json = new JSONObject();
    JSONObject data = new JSONObject();

    try {
      data.put("search", search);

      if (deactivated) {
        data.put("deactivated", true);
      }

      json.put("function", "search");
      json.put("object", "member");
      json.put("data", data);

      JSONArray memberArray = APIConnector.call(json).getJSONArray("data");

      for(int i = 0; i < memberArray.length(); i++) {
        members.add(new Member(memberArray.getJSONObject(i)));
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return members;
  }

  public Member insertMember(Member member) {
    JSONObject json = new JSONObject();

    try {
      json.put("object", "member");
      json.put("function", "insert");
      json.put("data", member.toJSON());

      JSONObject response = APIConnector.call(json);
      JSONObject data = response.getJSONObject("data");

      member.fromJSON(data);
    } catch (JSONException e) {
      e.printStackTrace();
      return null;
    }

    return member;
  }

  public Member updateMember(Member member) {
    JSONObject json = new JSONObject();

    try {
      json.put("object", "member");
      json.put("function", "update");
      json.put("data", member.toJSON());

      JSONObject response = APIConnector.call(json);
      JSONObject data = response.getJSONObject("data");

      member.fromJSON(data);
    } catch (JSONException e) {
      e.printStackTrace();
      return null;
    }

    return member;
  }

  public boolean deleteMember() {
    JSONObject json = new JSONObject();
    JSONObject data = new JSONObject();

    try {
      data.put("no", member.getNo());
      json.put("function", "delete");
      json.put("object", "member");
      json.put("data", data);

      JSONObject res = APIConnector.call(json);
      data = res.getJSONObject("data");

      return data.has("code") && data.getInt("code") == 200;
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return false;
  }

  public boolean renewAccount() {
    JSONObject json = new JSONObject();
    JSONObject data = new JSONObject();

    try {
      data.put("no", member.getNo());
      json.put("function", "renew");
      json.put("object", "member");
      json.put("data", data);

      JSONObject res = APIConnector.call(json);
      data = res.getJSONObject("data");

      if (data.has("code") && data.getInt("code") == 200) {
        member.getAccount().setLastActivity(new Date());
        return true;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return false;
  }

  public boolean addComment(String comment) {
    JSONObject json = new JSONObject();
    JSONObject data = new JSONObject();

    try {
      data.put("no", member.getNo());
      data.put("comment", comment);

      json.put("function", "insert_comment");
      json.put("object", "member");
      json.put("data", data);

      JSONObject res = APIConnector.call(json);
      data = res.getJSONObject("data");

      if (data.has("id")) {
        member.getAccount().addComment(new Comment(data.getInt("id"), comment));
        return true;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return false;
  }

  public boolean updateComment(int id, String comment) {
    JSONObject json = new JSONObject();
    JSONObject data = new JSONObject();

    try {
      data.put("id", id);
      data.put("comment", comment);

      json.put("function", "update");
      json.put("object", "comment");
      json.put("data", data);

      JSONObject res = APIConnector.call(json);
      data = res.getJSONObject("data");

      if (data.has("code") && data.getInt("code") == 200) {
        member.getAccount().editComment(id, comment, "");
        return true;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return false;
  }

  public boolean deleteComment(int id) {
    JSONObject json = new JSONObject();
    JSONObject data = new JSONObject();

    try {
      data.put("id", id);
      json.put("function", "delete");
      json.put("object", "comment");
      json.put("data", data);

      JSONObject res = APIConnector.call(json);
      data = res.getJSONObject("data");

      if (data.has("code") && data.getInt("code") == 200) {
        member.getAccount().removeComment(id);
        return true;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return false;
  }

  public boolean addTransaction(int copyId, String type) {
    TransactionHandler transactionHandler = new TransactionHandler();

    if (transactionHandler.insert(member.getNo(), copyId, type)) {
      member.getAccount().getCopy(copyId).addTransaction(type);
      return true;
    }

    return false;
  }

  public boolean addTransaction(int copyId, String type, int memberNo) {
    TransactionHandler transactionHandler = new TransactionHandler();

    if (transactionHandler.insert(memberNo, copyId, type)) {
      member.getAccount().getCopy(copyId).addTransaction(type);
      return true;
    }

    return false;
  }

  public boolean addTransaction(int[] copies, String type) {
    TransactionHandler transactionHandler = new TransactionHandler();

    if (transactionHandler.insert(member.getNo(), copies, type)) {
      for (int copy : copies) {
        member.getAccount().getCopy(copy).addTransaction(type);
      }

      return true;
    }

    return false;
  }

  public boolean pay() {
    JSONObject req = new JSONObject();
    JSONObject data = new JSONObject();

    try {
      data.put("no", member.getNo());
      req.put("function", "pay");
      req.put("object", "member");
      req.put("data", data);

      JSONObject res = APIConnector.call(req);
      data = res.getJSONObject("data");

      if (data.has("code") && data.getInt("code") == 200) {
        member.getAccount().pay();
        return true;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return false;
  }

  public boolean updateCopyPrice(int copyId, double price) {
    CopyHandler copyHandler = new CopyHandler();

    if (copyHandler.updateCopyPrice(copyId, price)) {
      member.getAccount().getCopy(copyId).setPrice(price);
      return true;
    }

    return false;
  }

  public boolean deleteCopy(int copyId) {
    CopyHandler copyHandler = new CopyHandler();

    if (copyHandler.deleteCopy(copyId)) {
      member.getAccount().removeCopy(copyId);
      return true;
    }

    return false;
  }

  public boolean cancelSell(int copyId) {
    TransactionHandler transactionHandler = new TransactionHandler();

    if (transactionHandler.delete(copyId, "SELL")) {
      member.getAccount().getCopy(copyId).removeTransaction("SELL");
      member.getAccount().getCopy(copyId).removeTransaction("SELL_PARENT");
      return true;
    }

    return false;
  }

  public boolean exist(int no) {
    JSONObject json = new JSONObject();
    JSONObject data = new JSONObject();

    try {
      data.put("no", no);
      json.put("function", "exist");
      json.put("object", "member");
      json.put("data", data);

      JSONObject response = APIConnector.call(json).getJSONObject("data");

      return response.has("code") && response.getInt("code") == 200;
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return false;
  }
}
