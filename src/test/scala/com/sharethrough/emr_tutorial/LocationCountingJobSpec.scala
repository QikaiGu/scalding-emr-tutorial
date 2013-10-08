package com.sharethrough.emr_tutorial

import org.specs2.mutable.Specification
import com.twitter.scalding._

class LocationCountingJobSpec extends Specification {

  // Twitter-provided Scala => Cascading conversions
  import Dsl._

  val lines = List(
    // allaboutbalance.com x3
    ("1", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=http%3A%2F%2Fwww.allaboutbalance.com%2Fand-the-other-months-are%2F&pref= HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    ("2", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=http%3A%2F%2Fwww.allaboutbalance.com%2Fand-the-other-months-are%2F&pref= HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    ("3", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=http%3A%2F%2Fwww.allaboutbalance.com%2Fand-the-other-months-are%2F&pref= HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    // sharethrough.com x2
    ("4", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=http%3A%2F%2Fwww.sharethrough.com%2Fengineering%2F&pref= HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    ("5", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=http%3A%2F%2Fwww.sharethrough.com%2Fengineering%2F&pref= HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    // example.com x0 (below impression floor of 2)
    ("6", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=http%3A%2F%2Fwww.example.com%2Fim-mostly-certain%2F&pref= HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    // emptyOrNoLocation.com x3
    //   no location
    ("7", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=&pref= HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    //   missing location
    ("8", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """),
    //   sequential empty params hosed the initial regex
    ("9", """ {"body": "453d2adc-faf6-11e2-8cbf-1231392033d2 10.198.39.249 - [01/Aug/2013:22:04:00 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=&pref=& HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Linux; U; Android 2.3.3; en-us; ADR6300 Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1\" \"-\" \"198.102.29.191\" \"-\""} """),
    // badUrlLocation.com x1
    ("10", """ {"body": "73b56afa-1298-11e3-a985-12313d08da2a 10.32.101.252 - [31/Aug/2013:23:52:53 +0000] \"GET /impression?pid=FAKE_PLACEMENT_ID&ploc=http%3A%2F%2Fwww.sharethrough.com%2Fengi\"neer\"ing%2F&pref= HTTP/1.1\" 200 145 \"INTENTIONALLY_BLANK_HTTP_REFERER\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\" \"-\" \"76.110.105.162\" \"-\""} """)
  )

  // Fully-qualified name of our job so the runner can find it
  JobTest("com.sharethrough.emr_tutorial.LocationCountingJob")

    // Supply test values for all required arguments
    .arg("input", "inputFile")
    .arg("output", "outputFile")
    .arg("placementId", "FAKE_PLACEMENT_ID")
    .arg("impressionFloor", "2")

    // Supply our test data to Scalding
    .source(TextLine("inputFile"), lines)

    // Specify the sink and format of the output
    .sink[(String, Int)](Tsv("outputFile")) { outputBuffer =>

      "Impressions grouped by 'placementId', greater than 'impressionFloor'" >> {
        outputBuffer.size must_== 4
        outputBuffer must contain(("www.allaboutbalance.com", 3))
        outputBuffer must contain(("www.sharethrough.com", 2))
        outputBuffer must contain(("www.emptyOrNoLocation.com", 3))
        outputBuffer must contain(("www.badLocation.com", 1))
      }

  }.run.finish
}
