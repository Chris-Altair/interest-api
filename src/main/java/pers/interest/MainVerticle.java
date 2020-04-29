package pers.interest;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import pers.interest.yamibo.YamiboCartoonCrawlerVerticle;
import pers.interest.yamibo.YamiboFavoriteVerticle;
import pers.interest.yamibo.YamiboLoginVerticle;

public class MainVerticle extends AbstractVerticle {
  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(MainVerticle.class.getName());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Vertx vertx = Vertx.vertx();
    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("file")
      .setFormat("json")
      .setConfig(new JsonObject()
        .put("path", "yamibo.json")
      );
    ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(store));
    Future<JsonObject> future = retriever.getConfig();
    future.onSuccess(result -> {
      DeploymentOptions options = new DeploymentOptions().setConfig(result);
      vertx.deployVerticle(new YamiboCartoonCrawlerVerticle(), options)
        .onSuccess(sucess -> startPromise.complete())
        .onFailure(Throwable::printStackTrace);
      vertx.deployVerticle(new YamiboFavoriteVerticle(), options)
        .onSuccess(sucess -> startPromise.complete())
        .onFailure(Throwable::printStackTrace);
      vertx.deployVerticle(new YamiboLoginVerticle(), options)
        .onSuccess(sucess -> startPromise.complete())
        .onFailure(Throwable::printStackTrace);
    });
  }
}
