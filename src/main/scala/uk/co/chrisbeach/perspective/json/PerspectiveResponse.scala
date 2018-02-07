package uk.co.chrisbeach.perspective.json

import uk.co.chrisbeach.perspective.model.PerspectiveAttribute

case class PerspectiveResponse(attributeScores: Map[PerspectiveAttribute, Double]) {
  override def toString: String =
    attributeScores.map { case (attribute, score) => f"$attribute: $score%.3f"}.mkString(", ")
}