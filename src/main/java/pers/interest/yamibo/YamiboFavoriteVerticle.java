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
import pers.interest.util.RequestUtils;

import java.util.List;

/**
 * 抓取收藏的小说和漫画的url
 *
 * @author Amadeus
 */
public class YamiboFavoriteVerticle extends AbstractVerticle {
  private final static Logger LOGGER = LoggerFactory.getLogger(YamiboFavoriteVerticle.class);
  private WebClient client;
  private MultiMap reqGetHead;

  private void init() {
    JsonObject requestHeadJson = config().getJsonObject("request-head");
    // 初始化get请求头
    this.reqGetHead = RequestUtils.generateGetRequestHead(requestHeadJson);
    this.client = WebClient.create(vertx);
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    /** 初始化 */
    init();
    EventBus eventBus = vertx.eventBus();
    eventBus.consumer("yamibo.favorite", message -> {
      String cookie = (String) message.body();
      this.reqGetHead.add("cookie", cookie);
      String favoriteMainUrl = config().getString("favorite-page-url");
//      LOGGER.debug("favoriteHtmlUrl = "+favoriteHtmlUrl);
      Future<HttpResponse<Buffer>> favoriteFuture = client.getAbs(favoriteMainUrl)
        .putHeaders(reqGetHead)
        .send();
      favoriteFuture.onSuccess(this::crawlerFavoritePageUrl).onFailure(Throwable::printStackTrace);
    });
  }

  public void crawlerFavoritePageUrl(HttpResponse<Buffer> response) {
    String favoriteMainUrl = config().getString("favorite-page-url");
    String favoriteMainHtml = response.bodyAsString("GBK");
    String serverUrl = config().getString("server");
    List<String> favoritePageUrls = HtmlUtils.getFavoritePages(favoriteMainHtml, favoriteMainUrl);
    // 从所有的收藏页中提取收藏的url
    for (String pageUrl : favoritePageUrls) {
      Future<HttpResponse<Buffer>> future = client.getAbs(pageUrl).putHeaders(this.reqGetHead).send();
      future.onSuccess(res -> {
        String favoritePageHtml = res.bodyAsString("GBK");
        List<String> favoriteUrls = HtmlUtils.getFavoriteUrls(favoritePageHtml, serverUrl);
        for (String favUrl : favoriteUrls) {
          client.getAbs(favUrl).putHeaders(this.reqGetHead).send()
            .onSuccess(this::sendCrawlerBus).onFailure(Throwable::printStackTrace);
        }
      });
    }
  }

  public void sendCrawlerBus(HttpResponse<Buffer> response) {
    String html = response.bodyAsString("GBK");
    String onlyLandlordUrl;
    /** 暂时不考虑文学区的爬虫，因为有新站 */
//            if (html.contains("文學區")) {
//              onlyLandlordUrl = HtmlUtils.getOnlyLandlordUrl(html, serverUrl);
//              System.out.println("novel = " + onlyLandlordUrl);
//            }
    if (html.contains("中文百合漫画区")) {
      String serverUrl = config().getString("server");
      onlyLandlordUrl = HtmlUtils.getOnlyLandlordUrl(html, serverUrl);
      LOGGER.info("cartoon = " + onlyLandlordUrl);
      JsonObject params = new JsonObject().put("cookie", this.reqGetHead.get("cookie")).put("url", onlyLandlordUrl);
      vertx.eventBus().send("yamibo.cartoon.crawler", params);
    }
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    super.stop(stopPromise);
  }
}


//response -> {
//
//  String favoritePageHtml = response.bodyAsString("GBK");
//  List<String> favoriteUrls = HtmlUtils.getFavoriteUrls(favoritePageHtml, serverUrl);
//  for (String favUrl : favoriteUrls) {
//  client.getAbs(favUrl).putHeaders(this.reqGetHead).send().onSuccess(res -> {
//  String html = res.bodyAsString("GBK");
//  String onlyLandlordUrl;
//  /** 暂时不考虑文学区的爬虫，因为有新站 */
////            if (html.contains("文學區")) {
////              onlyLandlordUrl = HtmlUtils.getOnlyLandlordUrl(html, serverUrl);
////              System.out.println("novel = " + onlyLandlordUrl);
////            }
//  if (html.contains("中文百合漫画区")) {
//  onlyLandlordUrl = HtmlUtils.getOnlyLandlordUrl(html, serverUrl);
//  LOGGER.info("cartoon = " + onlyLandlordUrl);
//  JsonObject params = new JsonObject().put("url", onlyLandlordUrl);
//  eventBus.send("yamibo.cartoon.crawler", params);
//  }
//  }).onFailure(Throwable::printStackTrace);
//  }
//
//  }
