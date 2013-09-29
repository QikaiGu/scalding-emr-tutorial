package com.sharethrough.emr_tutorial

import com.twitter.scalding._
import java.net.URI
import java.net.URLDecoder

class ReferrerCountingJob(args: Args) extends Job(args) {
  JsonLine(args("input"), List("body"))
    .map('body -> 'ploc) {
      body: String =>
        val pattern = """GET /impression\S*&ploc=(\S+)&""".r
        pattern.findFirstMatchIn(body) match {
          case Some(matchData) => matchData.subgroups(0)
          case None => "UNKNOWN"
        }
    }
    .map('ploc -> 'hostname) {
      ploc: String =>
        ploc match {
          case "UNKNOWN" => "UNKNOWN"
          case _ => new URI(URLDecoder.decode(ploc, "UTF-8")).getHost
        }
    }
    .groupBy('hostname) { _.size }
    .write(Tsv(args("output")))
}
