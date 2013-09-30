package com.sharethrough.emr_tutorial

import org.specs2.mutable.Specification
import com.twitter.scalding._

/**
 * We haven't fully settled on this as a paradigm for testing jobs (splitting
 * good data and bad data into separate specifications).  I do like the
 * simplicity of these 'bad data' specifications: no matter what you add to the
 * source data, the output is empty.
 */
class BadLocationCountingJobSpec extends Specification {

  import Dsl._

  val lines = List(
    // Missing all content
    ("1", """ {"body": "MISSING BODY"} """),
    // Well-formed, non-200
    ("2", """ {"body": "73ca4f06-1298-11e3-ac53-12313d08da2a 10.202.191.22 - [31/Aug/2013:23:52:53 +0000] \"-\" 400 0 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"-\" \"-\" \"-\" \"-\""} """),
    // Non-/impression beacon
    ("3", """ {"body": "74d9905a-1298-11e3-9cff-12313d08da2a 10.162.87.123 - [31/Aug/2013:23:52:55 +0000] \"GET /playtime?pid=429&el=10.5&vph=1230&vpw=720&ploc=&pref= HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Linux; U; Android 4.1.2; en-us; SCH-I535 Build/JZO54K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30\" \"-\" \"108.83.58.25\" \"-\""} """),
    // Impression beacon with non-matching pid
    ("4", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=NON_MATCHING_PID&ploc=http%3A%2F%2Fwww.allaboutbalance.com%2Fand-the-other-months-are%2F HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    // Impression beacon with missing pid
    ("5", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?ploc=http%3A%2F%2Fwww.allaboutbalance.com%2Fand-the-other-months-are%2F HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    // Impression beacon with empty pid
    ("6", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=&ploc=http%3A%2F%2Fwww.allaboutbalance.com%2Fand-the-other-months-are%2F HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    // Valid beacon, but only one of them so it's below the impression floor of 2
    ("7", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=http%3A%2F%2Fwww.allaboutbalance.com%2Fand-the-other-months-are%2F HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """)
  )

  JobTest("com.sharethrough.emr_tutorial.LocationCountingJob").
    arg("input", "inputFile").
    arg("output", "outputFile").
    arg("placementId", "FAKE_PLACEMENT_ID").
    arg("impressionFloor", "2").
    source(JsonLine("inputFile", List("body")), lines).
    sink[(String, Int)](Tsv("outputFile")) {
    outputBuffer =>
      "LocationCountingJob outputs nothing when the data is bad" >> {
        outputBuffer must beEmpty
      }
  }.run.finish
}
