package uk.co.chrisbeach.perspective.model

sealed abstract class PerspectiveAttribute(val key: String)

case object AttackOnAuthor extends PerspectiveAttribute("ATTACK_ON_AUTHOR")
case object AttackOnCommenter extends PerspectiveAttribute("ATTACK_ON_COMMENTER")
case object Incoherent extends PerspectiveAttribute("INCOHERENT")
case object Inflammatory extends PerspectiveAttribute("INFLAMMATORY")
case object LikelyToReject extends PerspectiveAttribute("LIKELY_TO_REJECT")
case object Obscene extends PerspectiveAttribute("OBSCENE")
case object SevereToxicity extends PerspectiveAttribute("SEVERE_TOXICITY")
case object Spam extends PerspectiveAttribute("SPAM")
case object Toxicity extends PerspectiveAttribute("TOXICITY")
case object Unsubstantial extends PerspectiveAttribute("UNSUBSTANTIAL")

object PerspectiveAttributes {
  val all: Set[PerspectiveAttribute] = Set(
    AttackOnAuthor, AttackOnCommenter, Incoherent, Inflammatory, LikelyToReject,
    Obscene, SevereToxicity, Spam, Toxicity, Unsubstantial)

  val byKey: Map[String, PerspectiveAttribute] =
    all.map { attribute => (attribute.key, attribute) }.toMap
}