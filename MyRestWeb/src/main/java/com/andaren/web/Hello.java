package com.andaren.web;

import com.andaren.web.entity.Whisky;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.impl.InternetProtocolFamily;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by andaren on 2017/1/3.
 * Used for :
 * modifiy  :
 */
public class Hello extends AbstractVerticle{

    private Map<Integer, Whisky> products = new LinkedHashMap<Integer, Whisky>();

    private JDBCClient jdbc;

    @Override
    public void start(Future<Void> fu) throws Exception {

        // 生成一些威士忌
        createSomeProducts();

        Router router = Router.router(vertx);

        router.route("/").handler(routingContext -> {
            HttpServerResponse resp = routingContext.response();
            resp.putHeader("content-type", "text/html")
                    .end("<h1>Hello from my first Vert.x 3 application</h1>");
        });

        // 显式开启（读取请求 体）
        router.route("/api/whiskies*").handler(BodyHandler.create());

        // Serve static resources from the /assets directory
        router.route("/assets/*").handler(StaticHandler.create("assets"));

        // some rest Api
        router.get("/api/whiskies").handler(this::getAll);
        router.get("/api/whiskies/:id").handler(this::getOne);
        router.post("/api/whiskies").handler(this::addOne);
        router.put("/api/whiskies/:id").handler(this::updateOne);
        router.delete("/api/whiskies/:id").handler(this::deleteOne);
        // Retrieve the port from the configuration,
        // default to 8080.
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8080), result -> {
                    if (result.succeeded()) {
                        fu.complete();
                    } else {
                        fu.fail(result.cause());
                    }
                });
    }

    private void getAll(RoutingContext cx) {
        cx.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(products.values()));
    }

    private void getOne(RoutingContext cx) {
        String id = cx.request().params().get("id");
        if (id == null) {
            cx.response().setStatusCode(400).end();
        } else {
            Integer idAsInteger = Integer.valueOf(id);
            Whisky whisky = products.get(idAsInteger);
            cx.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(whisky));
        }
    }

    private void addOne(RoutingContext cx) {
        final Whisky whisky = Json.decodeValue(cx.getBodyAsString(), Whisky.class);
        products.put(whisky.getId(), whisky);
        cx.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(whisky));
    }

    private void updateOne(RoutingContext cx) {
        JsonObject whiskyJson = cx.getBodyAsJson();
        String id = cx.request().getParam("id");
        if (whiskyJson == null || id == null) {
            cx.response().setStatusCode(400).end();
        } else {
            Integer idAsInteger = Integer.valueOf(id);
            Whisky whisky = products.get(idAsInteger);
            if (whisky == null) {
                cx.response().setStatusCode(404).end();
            } else {
                whisky.setName(whiskyJson.getString("name"));
                whisky.setOrigin(whiskyJson.getString("origin"));
                products.put(idAsInteger, whisky);
                cx.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(whisky));
            }

        }
    }

    private void deleteOne(RoutingContext cx) {
        String id = cx.request().params().get("id");
        if (id == null) {
            cx.response().setStatusCode(400).end();
        } else {
            Integer idAsInteger = Integer.valueOf(id);
            products.remove(idAsInteger);
            cx.response().setStatusCode(204).end();
        }
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        DeploymentOptions option = new DeploymentOptions().setWorker(false);
        vertx.deployVerticle("com.andaren.web.Hello", option);
    }

    public void createSomeProducts() {
        Whisky andaren = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
        products.put(andaren.getId(), andaren);
        Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
        products.put(talisker.getId(), talisker);
    }

}
