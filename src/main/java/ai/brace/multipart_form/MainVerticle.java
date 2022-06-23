package ai.brace.multipart_form;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.openapi.RouterBuilder;

import java.util.HashSet;
import java.util.Set;

public class MainVerticle extends AbstractVerticle
{

  @Override
  public void start(Promise<Void> startPromise)
  {
    final HttpServer server = vertx.createHttpServer();
    makeRouterBuilder(vertx).map(server::requestHandler).compose(handler -> handler.listen(8888)).onSuccess(ignored -> {
      startPromise.complete();
      System.out.println("HTTP server started on port 8888");
    }).onFailure(startPromise::fail);
  }

  public static void handleMultipart(RoutingContext routingContext)
  {
    var attrs = routingContext.request().formAttributes();
    var files = routingContext.fileUploads();

    final var resp = routingContext.response().putHeader("content-type", "text/plain");

    final boolean attrsCheck = attrs.contains("uploadName") && attrs.get("uploadName").equals("files.txt");
    final boolean filesCheck = files.size() == 1;

    if (attrsCheck && filesCheck)
    {
      resp.end("success");
    }
    else
    {
      resp.setStatusCode(500).end("error");
    }
  }

  public static void handleGet(RoutingContext routingContext)
  {
    routingContext.response().putHeader("content-type", "text/plain").end("success!");
  }

  private Future<Router> makeRouterBuilder(Vertx vertx)
  {
    return RouterBuilder.create(vertx, "openapi.yaml").map(builder -> {

      final Set<String> headers = new HashSet<>(6);
      headers.add("Access-Control-Allow-Origin");
      headers.add("Access-Control-Allow-Credentials");
      headers.add("origin");
      headers.add("Content-Type");
      headers.add("accept");
      headers.add("Authorization");

      final Set<HttpMethod> methods =
        Set.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS, HttpMethod.HEAD, HttpMethod.PUT, HttpMethod.DELETE);

      builder.rootHandler(CorsHandler.create("http://localhost")
                                     .allowCredentials(true)
                                     .allowedMethods(methods)
                                     .allowedHeaders(headers));

      builder.operation("post/multipart").handler(MainVerticle::handleMultipart);
      builder.operation("simple").handler(MainVerticle::handleGet);

      return builder.createRouter();
    });
  }
}
