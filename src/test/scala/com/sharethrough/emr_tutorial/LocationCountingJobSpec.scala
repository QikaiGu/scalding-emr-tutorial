package com.sharethrough.emr_tutorial

import org.specs2.mutable.Specification
import com.twitter.scalding._

class LocationCountingJobSpec extends Specification {

  // Twitter-provided Scala => Cascading conversions
  import Dsl._

  val lines = List(
    // allaboutbalance.com x3
    ("1", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=http%3A%2F%2Fwww.allaboutbalance.com%2Fand-the-other-months-are%2F HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    ("2", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=http%3A%2F%2Fwww.allaboutbalance.com%2Fand-the-other-months-are%2F HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    ("3", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=http%3A%2F%2Fwww.allaboutbalance.com%2Fand-the-other-months-are%2F HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    // sharethrough.com x2
    ("4", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=http%3A%2F%2Fwww.sharethrough.com%2Fengineering%2F HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    ("5", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=http%3A%2F%2Fwww.sharethrough.com%2Fengineering%2F HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    // empty ploc x1
    ("6", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc= HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    // missing ploc x1
    ("7", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """)
  )

  /*
    TODO: Wrapping a JobTest in the Specs2 "should" syntax does not work here,
    contrary to the working example provided in the Scalding Wiki.  I'm assuming
    one or both of Specs2 or a Scalding version bump is to be looked into.

    What are the semantics of 'should' with respect to parallel execution?
  */
  JobTest("com.sharethrough.emr_tutorial.LocationCountingJob")
    .arg("input", "inputFile")
    .arg("output", "outputFile")
    .arg("placementId", "FAKE_PLACEMENT_ID")
    .arg("impressionFloor", "2")
    .source(TextLine("inputFile"), lines)

    // TODO This tuple has to match the output format
    .sink[(String, Int)](Tsv("outputFile")) { outputBuffer =>

      // TODO: This example block is inside our job now! (not outside like it used to be)
      "Impressions grouped by 'placementId', greater than 'impressionFloor'" >> {
        outputBuffer.size must_== 3
        outputBuffer must contain(("www.allaboutbalance.com", 3))
        outputBuffer must contain(("www.sharethrough.com", 2))
        outputBuffer must contain(("www.UNKNOWNLOCATION.com", 2))
      }

  }.run.finish
}
