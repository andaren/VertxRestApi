package com.andaren.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.rxjava.ext.auth.AuthProvider;

import static java.awt.Color.red;

/**
 * Created by andaren on 2017/1/3.
 * Used for :
 * modifiy  :
 */
/*public class AuthUser extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        // 全部请求都有的处理
        // 1.处理Cookie
        router.route().handler(CookieHandler.create());
        // 2.处理Session
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        // 3.处理用户登录状态保存
        router.route().handler(UserSessionHandler.create(authProvider));

        AuthHandler redirectAuthHandler = RedirectAuthHandler.create(authProvider);

        // All requests to paths starting with '/private/' will be protected
        router.route("/private*//*").handler(redirectAuthHandler);

        // Handle the actual login
        router.route("/login").handler(FormLoginHandler.create(authProvider));

        // Set a static server to serve static resources, e.g. the login page
        router.route().handler(StaticHandler.create());

        router.route("/someotherpath").handler(routingContext -> {
            // This will be public access - no login required
        });

        router.route("/private/somepath").handler(routingContext -> {

            // This will require a login

            // This will have the value true
            boolean isAuthenticated = routingContext.user() != null;

        });


    }
}*/
