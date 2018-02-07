package uk.co.chrisbeach.perspective.json

import spray.json.JsString

object JsonUtils {
  private val jsonOverheadFudgeFactor = 0.05

  /**
    * A string represented as JSON will be longer in some cases due, for example, to escaped quotes.
    *
    * A "fudge factor" was introduced as it seems the JsString/compactPrint conversion was underestimating the length
    *
    * TODO: work out what's causing this and remove the fudge factor
    *
    * @return number of extra characters to represent the string in JSON
    */
  def estimatedJsonOverheadCharacterCount(string: String): Int =
    (JsString(string).compactPrint.length - string.length) + (jsonOverheadFudgeFactor * string.length).toInt
}
