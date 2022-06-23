package ai.brace.multipart_form;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle
{
  @BeforeAll
  static void deploy_verticle(Vertx vertx, VertxTestContext testContext)
  {
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void simple_test(Vertx vertx, VertxTestContext testContext)
  {
    WebClient client = WebClient.create(vertx);
    client.get(8888, "localhost", "/simple").send().map(result -> {
      assert result.statusCode() == 200;
      assert result.bodyAsString().equals("success!");
      return this;
    }).onComplete(testContext.succeedingThenComplete());
  }

  @Test
  void multipart_test(Vertx vertx, VertxTestContext testContext)
  {
    WebClient client = WebClient.create(vertx);
    Buffer buffer = Buffer.buffer("test payload");
    MultipartForm multipartForm = MultipartForm.create();
    multipartForm.attribute("uploadName", "files.txt");
    multipartForm.binaryFileUpload("test1", "test1.txt", buffer, "text");
    client.post(8888, "localhost", "/multipart").sendMultipartForm(multipartForm).map(result -> {
      assert result.statusCode() == 200;
      assert result.bodyAsString().equals("success");
      return this;
    }).onComplete(testContext.succeedingThenComplete());
  }
}
