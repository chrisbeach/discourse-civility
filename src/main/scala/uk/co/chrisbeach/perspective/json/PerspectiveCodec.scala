package uk.co.chrisbeach.perspective.json

import spray.json.{DefaultJsonProtocol, JsValue, RootJsonFormat, _}
import uk.co.chrisbeach.perspective.model.{PerspectiveAttribute, PerspectiveAttributes}


object PerspectiveCodec extends DefaultJsonProtocol {
  implicit val requestFormat: RootJsonFormat[PerspectiveRequest] =
    jsonFormat3(PerspectiveRequest)

  implicit object PerspectiveResponseFormat extends RootJsonFormat[PerspectiveResponse] {
    def write(c: PerspectiveResponse) = ???

    def read(value: JsValue): PerspectiveResponse =
      value match {
        case JsObject(fields) =>
          fields.get("attributeScores") match {
            case Some(scores: JsObject) => PerspectiveResponse(attributeSummaryScores(scores))
            case _ => deserializationError("No attributeScores")
          }
        case _ => deserializationError("Couldn't decode perspective API response")
      }

    private def attributeSummaryScores(scores: JsObject): Map[PerspectiveAttribute, Double] =
      scores.fields.map { case (name, js) =>
        (
          for {
            scoreJson <- js.asJsObject.fields.get("summaryScore")
            value <- scoreJson.asJsObject.fields.get("value")
          } yield value.convertTo[Double]
        ) match {
          case Some(score) => (PerspectiveAttributes.byKey(name), score)
          case _ => deserializationError(s"Couldn't decode summary score for $name")
        }
      }
  }
}