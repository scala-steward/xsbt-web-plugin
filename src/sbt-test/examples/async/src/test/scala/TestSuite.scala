import org.scalatest._

class TestSuite extends FunSuite with Matchers {

  test("add a message") {
    Request("GET", "http://localhost:8080/", Map.empty, None) shouldBe
      Response(
        200,
        Map("Content-Type" -> "text/html;charset=utf-8"),
        """|<html>
           |  <body>
           |    <h1>Hello, world!</h1>
           |  </body>
           |</html>""".stripMargin
      )
  }
}
