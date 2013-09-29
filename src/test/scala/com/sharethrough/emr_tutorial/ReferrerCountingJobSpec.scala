package com.sharethrough.emr_tutorial

import org.specs2.mutable.Specification
import com.twitter.scalding._

class ReferrerCountingJobSpec extends Specification {
  // Scalding implicit conversions (Scala => Cascading)
  import Dsl._

  val lines = List(
  )

  JobTest("com.sharethrough.emr_tutorial.ReferrerCountingJob").
    arg("input", "inputFile").
    arg("output", "outputFile").
    source(JsonLine("inputFile", List("body")), lines).
    sink[(String, Int)](Tsv("outputFile")) { outputBuffer =>

      "Contains all impressions for all publishers, grouped by referrers " >> {
        outputBuffer.size must_== 2
        outputBuffer must contain(("www.example.com", 2))
        outputBuffer must contain(("UNKNOWN", 1))
      }

  }.run.finish
}
