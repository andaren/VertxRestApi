import com.andaren.web.Hello;
import com.andaren.web.entity.Whisky;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by andaren on 2017/1/5.
 * Used for : 单元测试：
 *            说明：文档中有集成测试部分，到那时我跳过了。
 *            集成测试方法地址：http://vertx.io/blog/unit-and-integration-tests/
 * modifiy  :
 */
@RunWith(VertxUnitRunner.class)
public class RestTest {
    private Vertx vertx;

    private Integer port;

    @Before
    public void setUp(TestContext context) throws IOException {
        // 寻找一个随机端口
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
        // 部署一个Verticle
        vertx = Vertx.vertx();
        DeploymentOptions options = new DeploymentOptions();
        options.setConfig(new JsonObject().put("http.port", port));
        vertx.deployVerticle(Hello.class.getName(), options, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testAppGetIndex(TestContext context) {
        // 用于操作异步结果
        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/assets/index.html", response -> {
            context.assertTrue(response.headers().get("content-type").contains("text/html"));
            context.assertEquals(response.statusCode(), 200);
           response.handler(body -> {
              context.assertTrue(body.toString().contains("<title>My Whisky Collection</title>"));
               async.complete();
           });
        });
    }

    @Test
    public void testPostAddWhisky(TestContext context) {
        final Async async = context.async();
        final String json = Json.encodePrettily(new Whisky("Andaren", "BJ"));
        final String length = Integer.toString(json.length());
        vertx.createHttpClient().post(port, "localhost", "/api/whiskies")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", length)

                .handler(resp -> {
                    context.assertEquals(resp.statusCode(), 201);
                    context.assertTrue(resp.headers().get("content-type").contains("application/json"));
                   resp.handler(body -> {
                        Whisky whisky = Json.decodeValue(body.toString(), Whisky.class);
                       context.assertEquals(whisky.getName(), "Andaren");
                       async.complete();
                   });
                })
                // You cannot write data if you don’t have a response handler configured
                // 也就是说：write这里就开始执行连接了，所以在write之前要配置好handler之类的参数
                .write(json)
                // 知道调用end()才开始发送post请求
                .end();
    }

    @Test
    public void testDelete(TestContext context) {
        final Async async = context.async();
        String id = "100";
        vertx.createHttpClient().delete(port, "localhost", "/api/whiskies/" + id)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 204);
                    async.complete();
                })
                .end();
    }
}
