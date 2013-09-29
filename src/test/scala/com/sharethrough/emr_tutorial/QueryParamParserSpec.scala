package com.sharethrough.emr_tutorial

import org.specs2.mutable.Specification

class QueryParamParserSpec extends Specification {

  "When passed a standard request" should {
    "return a dictionary of the key/value pairs from the query string" in {
      val request = "http://example.com/?key1=value1&key2=value2"
      val expectedParamMap = Map("key1" -> List("value1"), "key2" -> List("value2"))

      QueryParamParser.parseFullRequest(request) must_== expectedParamMap
    }
  }

  "When passed an empty request string QueryParamParser.parseFullRequest" should {
    "return an empty dictionary" in {
      val request = "http://example.com/"
      val expectedParamMap = Map.empty[String, List[String]]

      QueryParamParser.parseFullRequest(request) must_== expectedParamMap
    }
  }

  "When passed a null request string QueryParamParser.parseFullRequest" should {
    "return an empty dictionary" in {
      val request = null
      val expectedParamMap = Map.empty[String, List[String]]

      QueryParamParser.parseFullRequest(request) must_== expectedParamMap
    }
  }


  "When passed a standard query string QueryParamParser.parseQueryString" should {
    "return a dictionary of the key/value pairs from the query string" in {
      val rawQueryString = "key1=value1&key2=value2"
      val expectedParamMap = Map("key1" -> List("value1"), "key2" -> List("value2"))

      QueryParamParser.parseQueryString(rawQueryString) must_== expectedParamMap
    }
  }

  "When passed null QueryParamParser.parseQueryString" should {
    "return an empty dictionary" in {
      QueryParamParser.parseQueryString(null) must_== Map.empty[String, List[String]]
    }
  }

  "When passed an empty string QueryParamParser.parseQueryString" should {
    "return an empty dictionary" in {
      QueryParamParser.parseQueryString("") must_== Map.empty[String, List[String]]
    }
  }

  "When passed an a query string with empty value QueryParamParser.parseQueryString" should {
    "return a map containing the key and an empty String" in {
      val rawQueryString = "key1=&key2=value2"
      val expectedParamMap = Map("key1" -> List(""), "key2" -> List("value2"))

      QueryParamParser.parseQueryString(rawQueryString) must_== expectedParamMap
    }
  }

  "When passed a query string with a blank value at the end of the string without an equals sign QueryParamParser.parseQueryString" should {
    "return a map containing the key and an empty string as the value" in {
      val rawQueryString = "key1=value1&key2"
      val expectedParamMap = Map("key1" -> List("value1"), "key2" -> List(""))

      QueryParamParser.parseQueryString(rawQueryString) must_== expectedParamMap
    }
  }

  "When passed a query string with 1 key having multiple values QueryParamParser.parseQueryString" should {
    "return a map with the key and the list of values" in {
      val rawQueryString = "key1=value1&key1=value2"
      val expectedParamMap = Map("key1" -> List("value1", "value2"))

      QueryParamParser.parseQueryString(rawQueryString) must_== expectedParamMap
    }
  }

}