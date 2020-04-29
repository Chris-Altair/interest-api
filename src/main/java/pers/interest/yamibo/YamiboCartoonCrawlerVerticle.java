package pers.interest.yamibo;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import pers.interest.util.HtmlUtils;
import pers.interest.util.RequestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片爬虫Verticle
 *
 * @author Amadeus
 */
public class YamiboCartoonCrawlerVerticle extends AbstractVerticle {
  private MultiMap reqGetHead;
  private WebClient client;

  private void init() {
    JsonObject requestHeadJson = config().getJsonObject("request-head");
    // 初始化get请求头
    this.reqGetHead = RequestUtils.generateGetRequestHead(requestHeadJson);
    // 该Verticle使用1个client，否则爬取图片会出现Connection reset
    this.client = WebClient.create(vertx, new WebClientOptions().setConnectTimeout(5000));
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    /** 初始化请求头 */
    init();
    /** 获取请求头 */
    EventBus eventBus = vertx.eventBus();
    eventBus.consumer("yamibo.cartoon.crawler", message -> {
      JsonObject json = (JsonObject) message.body();
      Future<HttpResponse<Buffer>> future = client.getAbs(json.getString("url"))
        .putHeaders(this.reqGetHead)
        .putHeader("cookie", json.getString("cookie"))
        .send();
      future.onSuccess(this::crawler).onFailure(Throwable::printStackTrace);
    });
  }

  private void crawler(HttpResponse<Buffer> response) {
    String html = response.bodyAsString("GBK");
    String serverUrl = config().getString("server");
    String title = HtmlUtils.getTitle(html);
    System.out.println("title = " + title);
    List<String> images = HtmlUtils.getImageUrls(html, serverUrl);
    /** 本地漫画存储路径 */
    String cartoonPath = config().getString("cartoon-path");
    String specificCartoonPath = new StringBuilder(cartoonPath).append("/").append(title).append("/").toString();
    vertx.fileSystem().exists(specificCartoonPath).onSuccess(isExist -> {
      if (!isExist) {
        JsonObject json = new JsonObject()
          .put("imageDirectory", specificCartoonPath)
          .put("imageUrls", new JsonArray(images));
        Future<Void> mkdirsFuture = vertx.fileSystem().mkdirs(specificCartoonPath);
        /** future.map(V v)方法可以传值 */
        mkdirsFuture.map(json).onSuccess(this::downloadImages)
          .onFailure(Throwable::printStackTrace);
      }
    }).onFailure(Throwable::printStackTrace);
  }

  private void downloadImages(JsonObject json) {
    String imageDirectory = json.getString("imageDirectory");
    System.out.println("文件下载路径 = " + imageDirectory);
    JsonArray imageUrls = json.getJsonArray("imageUrls");
    // 获取所有下载的future列表
    List<Future> downloadFutures = new ArrayList<>();
    for (int i = 0; i < imageUrls.size(); i++) {
      String imageUrl = imageUrls.getString(i);
      String imageName = new StringBuilder(String.valueOf(i)).append(".jpg").toString();
      String imagePath = new StringBuilder(imageDirectory).append(imageName).toString();
      Future<HttpResponse<Buffer>> sendFuture = client.getAbs(imageUrl).send();
      // compose顺序组合，只有上一个future成功后compose内的handler才会执行，并且只有都成功最后才返回成功
      Future<Void> downloadFuture = sendFuture.compose(response -> {
        Future<Void> writeFileFuture = vertx.fileSystem().writeFile(imagePath, response.body());
        return writeFileFuture;
      });
      downloadFuture.onSuccess(v -> {
        System.out.println("抓取成功文件:" + imageName + " url:" + imageUrl);
      }).onFailure(throwable -> System.err.println("抓取失败文件:" + imageName + " url:" + imageUrl + " message:" + throwable.getMessage()));
      downloadFutures.add(downloadFuture);
    }
    // 这里本来是想通过并发合并，所有下载future都成功后才会成功，但实际上没反应
    CompositeFuture.all(downloadFutures).onSuccess(compositeFuture -> {
      // 我很纳闷为什么这个不执行...
      // 如果imageUrls为空会输出？？？
      System.out.println("所有图片均下载成功！");
    }).onFailure(Throwable::printStackTrace);
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    super.stop(stopPromise);
  }
}
