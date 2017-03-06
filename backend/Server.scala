import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import sangria.execution.deferred.DeferredResolver
import sangria.parser.QueryParser
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.marshalling.sprayJson._
import spray.json._

import scala.util.{Failure, Success}

import sangria.renderer.SchemaRenderer

import slick.jdbc.H2Profile.api._
import scala.concurrent.ExecutionContext.Implicits.global

object Server extends App {
  // Create fake DB data
  val db = Database.forConfig("h2test")

  val accounts = TableQuery[Accounts]
  val transactions = TableQuery[Transactions]

  val setup = DBIO.seq(
    (accounts.schema ++ transactions.schema).create,
    accounts ++= Seq(
      Account(1, "Checking Account"),
      Account(2, "Savings Account")
    ),
    transactions ++= Seq(
      Transaction(1, "MBTA", BigDecimal("22.5"), 1),
      Transaction(2, "Uber", BigDecimal("10.23"), 1),
      Transaction(3, "Interests", BigDecimal("0.34"), 2)
    )
  )

  val setupFuture = db.run(setup)

  // END Create fake DB data

  implicit val system = ActorSystem("sangria-server")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  val route: Route =
    (post & path("graphql")) {
      entity(as[JsValue]) { requestJson ⇒ {
        val JsObject(fields) = requestJson

        val JsString(query) = fields("query")

        val operation = fields.get("operationName") collect {
          case JsString(op) ⇒ op
        }

        val vars = fields.get("variables") match {
          case Some(obj: JsObject) ⇒ obj
          case _ ⇒ JsObject.empty
        }

        QueryParser.parse(query) match {
          // query parsed successfully, time to execute it!
          case Success(queryAst) ⇒
            complete(Executor.execute(AccountSchema.schema, queryAst, new AccountRepo, variables = vars, operationName = operation)
              .map(OK → _)
              .recover {
                case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
                case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
              }
          )

          // can't parse GraphQL query, return error
          case Failure(error) ⇒
            complete(BadRequest, JsObject("error" → JsString(error.getMessage)))
        }
      }}
    } ~
    get {
      getFromResource("graphiql.html")
    }

  println(SchemaRenderer.renderSchema(AccountSchema.schema))

  Http().bindAndHandle(route, "0.0.0.0", 8080)
}
