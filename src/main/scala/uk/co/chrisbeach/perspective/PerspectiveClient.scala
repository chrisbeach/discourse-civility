package uk.co.chrisbeach.perspective

import akka.actor.{ActorSystem, Terminated}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import spray.json._
import uk.co.chrisbeach.perspective.json.PerspectiveCodec._
import uk.co.chrisbeach.perspective.json.PerspectiveRequests.perspectiveRequest
import uk.co.chrisbeach.perspective.json.PerspectiveResponse

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps
import scala.util.Try

/**
  * Client for Google Perspective API.
  *
  * See: https://www.perspectiveapi.com/
  */
class PerspectiveClient(apiKey: String, apiUrl: String) {

  private implicit val system: ActorSystem = ActorSystem()
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val timeout = 5 seconds

  def analyse(comment: String): Future[PerspectiveResponse] = {
    val body = request(comment)
    Http().singleRequest(
      HttpRequest(POST, s"$apiUrl/comments:analyze?key=$apiKey", entity = body)
    ).flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.toStrict(timeout)
          .map { _.data.utf8String.parseJson.convertTo[PerspectiveResponse] }
      case HttpResponse(code, _, entity, _) =>
        entity.toStrict(timeout)
          .map { _.data.utf8String }
          .map { message => throw new Exception(s"Request failed. Code: $code. Message: $message Payload: \n$body") }
    }
  }

  private def request(comment: String): RequestEntity =
    HttpEntity(`application/json`, perspectiveRequest(comment).toJson.compactPrint)

  def shutdown(): Option[Try[Terminated]] = system.terminate().value
}

object PerspectiveClient {
  /**
    * The stated limit is 1000 requests per 100 seconds. Smooth it out by doing the following:
    */
  val allowedBatchSize: Int = 100
  val rateLimitRenewalPeriod: FiniteDuration = 10 seconds
  val analysisTimeout: FiniteDuration = 5 seconds

  def estimatedAnalysisDuration(commentCount: Int): FiniteDuration =
    commentCount / allowedBatchSize * rateLimitRenewalPeriod

  def apply(apiKey: String, config: Config): PerspectiveClient =
    new PerspectiveClient(apiKey, config.getString("url"))
}