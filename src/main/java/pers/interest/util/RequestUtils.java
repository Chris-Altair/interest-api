package pers.interest.util;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class RequestUtils {
  /**
   * 从yml文件中生成get请求头
   * @param reqHeadJson
   * @return
   */
  public static MultiMap generateGetRequestHead(JsonObject reqHeadJson) {
    MultiMap reqGetHead = MultiMap.caseInsensitiveMultiMap();
    reqGetHead
      .add("accept", reqHeadJson.getString("accept"))
      .add("accept-language", reqHeadJson.getString("accept-language"))
      .add("referer", reqHeadJson.getString("referer"))
      .add("upgrade-insecure-requests", reqHeadJson.getInteger("upgrade-insecure-requests").toString())
      .add("user-agent", reqHeadJson.getString("user-agent"));
    return reqGetHead;
  }

  /**
   * 从yml文件中生成post请求头
   * @param reqHeadJson
   * @return
   */
  public static MultiMap generatePostRequestHead(JsonObject reqHeadJson) {
    MultiMap reqPostHead = MultiMap.caseInsensitiveMultiMap();
    reqPostHead
      .add("accept", reqHeadJson.getString("accept"))
      .add("accept-language", reqHeadJson.getString("accept-language"))
      .add("content-type", reqHeadJson.getString("content-type"))
      .add("origin", reqHeadJson.getString("origin"))
      .add("referer", reqHeadJson.getString("referer"))
      .add("upgrade-insecure-requests", reqHeadJson.getInteger("upgrade-insecure-requests").toString())
      .add("user-agent", reqHeadJson.getString("user-agent"));
    return reqPostHead;
  }

  /**
   * 解析cookie列表根据name获取cookie的value，返回name=value;的形式
   */
  public static String getLegalCookie(List<String> cookies, String name) {
    String legalCookie = null;
    for (String ck : cookies) {
      if (ck.contains(name)) {
        legalCookie = ck.split(" ")[0];
        break;
      }
    }
    return legalCookie;
  }

  /**
   * 生成登录post请求
   */
  public static String generateLoginUrl(String loginhash, String serverUrl) {
    StringBuilder loginUrl = new StringBuilder(serverUrl)
      .append("/member.php?mod=").append("logging")
      .append("&action=").append("login")
      .append("&loginsubmit=").append("yes")
      .append("&frommessage")
      .append("&loginhash=").append(loginhash)
      .append("&inajax=1");
    return loginUrl.toString();
  }

  /**
   * 构造登录post请求的表单
   */
  public static MultiMap generateLoginForm(String username, String password, String formhash, String referer) {
    MultiMap form = MultiMap.caseInsensitiveMultiMap()
      .add("formhash", formhash)
      .add("referer", referer)
      .add("loginfield", "username")
      .add("username", username) //
      .add("password", password) // 还没看是什么规则。。。
      .add("questionid", "0")
      .add("answer", "");
    return form;
  }
}
