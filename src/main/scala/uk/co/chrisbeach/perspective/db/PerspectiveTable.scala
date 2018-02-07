package uk.co.chrisbeach.perspective.db

import doobie.imports._
import uk.co.chrisbeach.perspective.db.PostTypes.PostId
import uk.co.chrisbeach.perspective.json.PerspectiveResponse
import uk.co.chrisbeach.perspective.model._

import scalaz.effect.IO

class PerspectiveTable(transactor: Transactor[IO]) {

  def createTableIfMissing(): Int =
    sql"""
      CREATE TABLE IF NOT EXISTS public.perspective
        (
            post_id INT PRIMARY KEY,
            attack_on_author DOUBLE PRECISION NOT NULL,
            attack_on_commenter DOUBLE PRECISION NOT NULL,
            incoherent DOUBLE PRECISION NOT NULL,
            inflammatory DOUBLE PRECISION NOT NULL,
            likely_to_reject DOUBLE PRECISION NOT NULL,
            obscene DOUBLE PRECISION NOT NULL,
            severe_toxicity DOUBLE PRECISION NOT NULL,
            spam DOUBLE PRECISION NOT NULL,
            toxicity DOUBLE PRECISION NOT NULL,
            unsubstantial DOUBLE PRECISION NOT NULL
        );
    """.update.run.transact(transactor).unsafePerformIO

  def insert(postId: PostId, perspectiveResponse: PerspectiveResponse): Int =
    sql"""
      INSERT INTO public.perspective
        (
            post_id,
            attack_on_author,
            attack_on_commenter,
            incoherent,
            inflammatory,
            likely_to_reject,
            obscene,
            severe_toxicity,
            spam,
            toxicity,
            unsubstantial
        ) VALUES (
            $postId,
            ${perspectiveResponse.attributeScores(AttackOnAuthor)},
            ${perspectiveResponse.attributeScores(AttackOnCommenter)},
            ${perspectiveResponse.attributeScores(Incoherent)},
            ${perspectiveResponse.attributeScores(Inflammatory)},
            ${perspectiveResponse.attributeScores(LikelyToReject)},
            ${perspectiveResponse.attributeScores(Obscene)},
            ${perspectiveResponse.attributeScores(SevereToxicity)},
            ${perspectiveResponse.attributeScores(Spam)},
            ${perspectiveResponse.attributeScores(Toxicity)},
            ${perspectiveResponse.attributeScores(Unsubstantial)}
        );
    """.update.run.transact(transactor).unsafePerformIO()
}