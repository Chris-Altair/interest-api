package pers.interest.cipher;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import pers.interest.util.RSAUtils;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author amadeus
 */
public class CipherVerticle extends AbstractVerticle {
    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(CipherVerticle.class.getName());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        ConfigStoreOptions store = new ConfigStoreOptions()
                .setType("file")
                .setFormat("json")
                .setConfig(new JsonObject()
                        .put("path", "rsa_key.json")
                );
        ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(store));
        Future<JsonObject> future = retriever.getConfig();
        future.onSuccess(result -> {
            String priKeyEncode = result.getString("private_key");
            String pubKeyEncode = result.getString("public_key");
            PrivateKey privateKey = RSAUtils.getPriKey(pubKeyEncode);
            PublicKey publicKey = RSAUtils.getPubKey(priKeyEncode);

            Router router = Router.router(vertx);
            router.get("/rsa/encrypt").handler(context -> {
                String plaintext = context.request().getParam("plaintext");

                context.response().end(RSAUtils.encrypt(plaintext, privateKey));
            });
            router.get("/rsa/decrypt").handler(context -> {
                String plaintext = context.request().getParam("ciphertext");
                context.response().end(RSAUtils.decrypt(plaintext, publicKey));
            });
            vertx.createHttpServer().requestHandler(router).listen(8888);

        }).onFailure(Throwable::printStackTrace);
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        super.stop(stopPromise);
    }
}
