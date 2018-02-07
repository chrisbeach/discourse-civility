package uk.co.chrisbeach.perspective.json

import org.scalatest.{Matchers, WordSpec}
import spray.json._
import uk.co.chrisbeach.perspective.json.PerspectiveCodec.PerspectiveResponseFormat
import uk.co.chrisbeach.perspective.model.{AttackOnCommenter, Incoherent}


class CodecTest extends WordSpec with Matchers {
  private val validJson: String =
    """
      |{
      |    "attributeScores": {
      |        "INCOHERENT": {
      |            "spanScores": [
      |                {
      |                    "begin": 0,
      |                    "end": 31,
      |                    "score": {
      |                        "value": 0.28371498,
      |                        "type": "PROBABILITY"
      |                    }
      |                }
      |            ],
      |            "summaryScore": {
      |                "value": 0.28371498,
      |                "type": "PROBABILITY"
      |            }
      |        },
      |        "ATTACK_ON_COMMENTER": {
      |            "spanScores": [
      |                {
      |                    "begin": 0,
      |                    "end": 31,
      |                    "score": {
      |                        "value": 0.91189426,
      |                        "type": "PROBABILITY"
      |                    }
      |                }
      |            ],
      |            "summaryScore": {
      |                "value": 0.91189426,
      |                "type": "PROBABILITY"
      |            }
      |        }
      |    },
      |    "languages": [
      |        "en"
      |    ]
      |}
      |
    """.stripMargin


  "Codec" should {
    "decode valid JSON" in {
      val json = validJson.parseJson

      PerspectiveResponseFormat.read(json) should be(
        PerspectiveResponse(Map(Incoherent -> 0.28371498, AttackOnCommenter -> 0.91189426))
      )
    }
  }
}