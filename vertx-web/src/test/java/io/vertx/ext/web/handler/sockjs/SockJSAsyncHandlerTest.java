package io.vertx.ext.web.handler.sockjs;

import org.junit.Test;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class SockJSAsyncHandlerTest extends SockJSTestBase {

  public SockJSAsyncHandlerTest() {
    // Use two servers so we test with HTTP request/response with load balanced SockJSSession access
    numServers = 2;
  }

  @Override
  protected void addHandlersBeforeSockJSHandler(Router router) {
    router.route().handler(BodyHandler.create());
    // simulate an async handler
    router.route().handler(rtx -> rtx.vertx().executeBlocking(f -> f.complete(true), r -> rtx.next()));
  }

  @Test
  public void testHandleMessageFromXhrTransportWithAsyncHandler() {
    sockJSHandler.socketHandler(socket -> socket.handler(buf -> {
      assertEquals("Hello World", buf.toString());
      testComplete();
    }));

    client.post("/test/400/8ne8e94a/xhr", onSuccess(resp -> {
      assertEquals(200, resp.statusCode());

      client.post("/test/400/8ne8e94a/xhr_send", onSuccess(respSend -> assertEquals(204, respSend.statusCode())))
        .putHeader("content-length", "13")
        .write("\"Hello World\"")
        .end();
    })).end();

    await();
  }
}
