package com.sharethrough.emr_tutorial

import org.specs2.mutable.Specification
import com.twitter.scalding._

class ImpressionCountingJobSpec extends Specification {

  // Scalding's implicit conversions
  import Dsl._

  val fakeInputData = List(
    ("2012-12-14", "1355529600.081", "session-id-0",
      "http://b.sharethrough.com/butler?type=impressionRequest&uid=4a7f2630-2878-0130-3bfb-22000a9f107a&pwidth=300&pheight=93&ckey=abcd123&pkey=placement1&session=session-id-0&bwidth=320&bheight=372",
      "/butler", "impressionRequest", "unknown", "en-us", "76.104.135.9",
      "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Mobile/9A405", "iOS", "Mobile Safari", "1",
      "http://m.example.com/article1"),

    ("2012-12-14", "1355529600.081", "session-id-1",
      "http://b.sharethrough.com/butler?type=impressionRequest&uid=4a7f2630-2878-0130-3bfb-22000a9f107a&pwidth=300&pheight=93&ckey=abcd123&pkey=placement2&session=session-id-0&bwidth=320&bheight=372",
      "/butler", "impressionRequest", "unknown", "en-us", "96.49.97.5",
      "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Mobile/9A405", "iOS", "Mobile Safari", "1",
      "http://m.example.com/article1"),

    ("2012-12-14", "1355529600.081", "session-id-2",
      "http://b.sharethrough.com/butler?type=impressionRequest&uid=4a7f2630-2878-0130-3bfb-22000a9f107a&pwidth=300&pheight=93&ckey=abcd123&pkey=placement2&session=session-id-0&bwidth=320&bheight=372",
      "/butler", "impressionRequest", "unknown", "en-us", "96.49.97.5",
      "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Mobile/9A405", "iOS", "Mobile Safari", "1",
      "http://m.example.com/article1")
  )

  /*
    TODO: Wrapping a JobTest in the Specs2 "should" syntax does not work here,
    contrary to the working example provided in the Scalding Wiki.  I'm assuming
    one or both of Specs2 or a Scalding version bump is to be looked into.

    What are the semantics of 'should' with respect to parallel execution?
  */
  JobTest("com.sharethrough.emr_tutorial.ImpressionCountingJob").
    arg("input", "inputFile").
    arg("output", "outputFile").
    source(Tsv("inputFile", ImpressionCountingJob.inputFormat), fakeInputData).
    sink[(String, Int)](Tsv("outputFile")) { outputBuffer =>
      // TODO: Note that the example block is now nested
      "Return the number of impressions requests per placement" >> {
        outputBuffer.size must_== 2
        outputBuffer(0) must_==("placement1", 1)
        outputBuffer(1) must_==("placement2", 2)
      }
  }.run.finish
}
