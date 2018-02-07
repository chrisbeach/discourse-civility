package uk.co.chrisbeach.perspective.json

import uk.co.chrisbeach.perspective.json.JsonUtils.estimatedJsonOverheadCharacterCount
import uk.co.chrisbeach.perspective.model.{PerspectiveAttribute, PerspectiveAttributes}

case class PerspectiveRequest(comment: Map[String, String],
                              languages: Seq[String],
                              requestedAttributes: Map[String, Map[String, String]])

object PerspectiveRequests {
  private val apiMaxCommentLength = 3000

  def perspectiveRequest(comment: String,
                         languages: Seq[String] = Seq("en"),
                         requestedAttributes: Set[PerspectiveAttribute] = PerspectiveAttributes.all) =
    PerspectiveRequest(
      Map("text" -> comment.take(apiMaxCommentLength - estimatedJsonOverheadCharacterCount(comment))),
      languages,
      requestedAttributes.map(attribute => attribute.key -> Map[String, String]()).toMap
    )
}
