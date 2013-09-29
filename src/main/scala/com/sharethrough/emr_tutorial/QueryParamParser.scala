package com.sharethrough.emr_tutorial

/**
 * Parse a query param string into a Map[String, List(String)]
 */
object QueryParamParser {

  def parseFullRequest(fullRequest: String): Map[String, List[String]] = {
    if (fullRequest == "" || fullRequest == null)
      return Map.empty[String, List[String]]

    fullRequest.split("\\?") match {
      case Array(_, x) =>
        parseQueryString(x)
      case _ =>
        Map.empty[String, List[String]]
    }
  }

  def parseQueryString(rawQueryString: String): Map[String, List[String]] = {
    if (rawQueryString == "" || rawQueryString == null)
      return Map.empty[String, List[String]]
    rawQueryString.split("&").foldLeft(Map.empty[String, List[String]])(
      (params, paramPair) => {
        val (key, value) = extractKeyValueFromQueryParamPair(paramPair)
        val existingValues: List[String] = params.getOrElse(key, List.empty[String])
        params + (key -> (existingValues :+ value))
      }
    )
  }

  def extractKeyValueFromQueryParamPair(queryParamPair: String): (String, String) =
    queryParamPair.split("=") match {
      case Array(k, v) => (k, v)
      case Array(k) => (k, "")
    }
}
