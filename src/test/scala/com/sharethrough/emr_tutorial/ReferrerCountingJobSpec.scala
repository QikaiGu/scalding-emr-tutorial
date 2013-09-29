package com.sharethrough.emr_tutorial

import org.specs2.mutable.Specification
import com.twitter.scalding._

class ReferrerCountingJobSpec extends Specification {
  import Dsl._

  val lines = List(
  )

  /*
    TODO: Wrapping a JobTest in the Specs2 "should" syntax does not work here,
    contrary to the working example provided in the Scalding Wiki.  I'm assuming
    one or both of Specs2 or a Scalding version bump is to be looked into.

    What are the semantics of 'should' with respect to parallel execution?
  */
  JobTest("com.sharethrough.emr_tutorial.ReferrerCountingJob").
    arg("input", "inputFile").
    arg("output", "outputFile").
    source(JsonLine("inputFile", List("body")), lines).

    // TODO This tuple has to match the output format
    sink[(String, Int)](Tsv("outputFile")) { outputBuffer =>

      // TODO: This example block is inside our job now! (not outside like it used to be)
      "Contains all impressions for all publishers, grouped by referrers " >> {
        outputBuffer.size must_== 2
        outputBuffer must contain(("www.example.com", 2))
        outputBuffer must contain(("UNKNOWN", 1))
      }

  }.run.finish
}
