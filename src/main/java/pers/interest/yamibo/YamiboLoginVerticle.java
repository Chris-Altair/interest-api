package pers.interest.yamibo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import pers.interest.util.HtmlUtils;
import pers.interest.util.MD5Utils;
import pers.interest.util.RequestUtils;

import java.util.List;

/**
 * 模拟登录Verticle
 */
public class YamiboLoginVerticle extends AbstractVerticle {
  private WebClient client;
  private MultiMap reqGetHead;
  private MultiMap reqPostHead;

  public void init() {
    JsonObject requestHeadJson = config().getJsonObject("request-head");
    // 初始化get请求头
    this.reqGetHead = RequestUtils.generateGetRequestHead(requestHeadJson);
    // 初始化post请求头
    this.reqPostHead = RequestUtils.generatePostRequestHead(requestHeadJson);
    /** 本来想用session，但是vertx的session使用之后的登录post会报非法字符 */
    // , new WebClientOptions().setSsl(true).setVerifyHost(false).setTrustAll(false)
    this.client = WebClient.create(vertx);
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    /** 初始化 */
    init();
    /** 获取yml配置 */
    String serverUrl = config().getString("server");
    /** 请求登录页面，提取cookie和页面的formhash、loginhash */
    Future<HttpResponse<Buffer>> future = client.getAbs(serverUrl + "/member.php")
      .putHeaders(this.reqGetHead)
      .addQueryParam("mod", "logging")
      .addQueryParam("action", "login")
      .send();
    future.onSuccess(this::login).onFailure(Throwable::printStackTrace);
  }

  /**
   * 模拟登陆，拿到auth和saltkey，并将其发布给其他Verticle
   *
   * @param response
   */
  private void login(HttpResponse<Buffer> response) {
    /** 获取cookie */
    List<String> loginCookies = response.cookies();
    final String saltkeyCookie = RequestUtils.getLegalCookie(loginCookies, "_saltkey");
    final String phpsessidCookie = RequestUtils.getLegalCookie(loginCookies, "PHPSESSID");
    for (String ck : loginCookies) {
      System.out.println("loginCookie = " + ck);
    }
    String html = response.bodyAsString("GBK");
    String formhash = HtmlUtils.getFormhash(html);
    String loginhash = HtmlUtils.getLoginhash(html);
    if (null != formhash && null != loginhash && null != saltkeyCookie && null != phpsessidCookie) {
      System.out.println("loginhash = " + loginhash);
      System.out.println("formhash = " + formhash);
      /** 中文的用户名我弄了一天也没成功
       *  从登陆post请求观察传是用户名的gbk编码，但用form添加用户名的gbk编码或中文或Unicode编码都登陆提示用户名错误
       *  所以还是建议使用英文名
       * */
      final String username = config().getString("username");
      final String password = config().getString("password");
      final String serverUrl = config().getString("server");
      /** 构造登录请求 */
      String loginUrl = RequestUtils.generateLoginUrl(loginhash, serverUrl);
      /** 构造登录表单，密码经过MD5加密 */
      MultiMap form = RequestUtils.generateLoginForm(username, MD5Utils.MD5Encode(password), formhash, reqPostHead.get("referer"));

      /** 模拟登录 */
      Future<HttpResponse<Buffer>> loginFuture = client
        // 必须用abs且必须拼装成https形式，否则会301
        .postAbs(loginUrl)
        .putHeaders(this.reqPostHead)
        /** 不要用HttpRequest<T> putHeader(String var1, Iterable<String> var2)这个方法设置cookie！！！
         * 这里cookie只需phpsessid和saltkey即可
         */
        .putHeader("cookie", saltkeyCookie + phpsessidCookie)
        .sendForm(form);
      loginFuture.onSuccess(loginResponse -> {
        List<String> loginSuccessCookie = loginResponse.cookies();
        final String authCookie = RequestUtils.getLegalCookie(loginSuccessCookie, "_auth");
        for (String ck : loginSuccessCookie) {
          System.out.println("loginSuccessCookie = " + ck);
        }
        String loginResponsehtml = loginResponse.bodyAsString("GBK");
        System.out.println(loginResponsehtml);
        /** 为什么使用登录成功的响应cookie还是提示尚未登录？分析cookie后发现了response cookie中没有saltkey
         * 经测试发现cookie中至少含有saltkey和auth才可成功
         * Discuz是通过cookie里的auth来验证登录的，登录成功会通过username、password、saltkey和auth_key（后台）计算出auth,set到cookie上
         * 后续请求通过cookie里的saltkey和auth和后台的auth_key来校验用户是否合法，所以后续请求cookie只需saltkey和auth即可成功
         * */
        if(loginResponsehtml.contains("succee")) {
          EventBus eventBus = vertx.eventBus();
          final String cookie = authCookie + saltkeyCookie;
          /** 拿到通行的cookie后通过eventBus将cookie发布到FavoriteVerticle分析收藏 */
          eventBus.send("yamibo.favorite", cookie);
        }
      }).onFailure(Throwable::printStackTrace);
    }
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    super.stop(stopPromise);
  }
}
