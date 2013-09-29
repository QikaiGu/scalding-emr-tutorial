package com.sharethrough.emr_tutorial

import com.twitter.scalding._
import java.net.URI
import java.net.URLDecoder

class LocationCountingJob(args: Args) extends Job(args) {
  JsonLine(args("input"), List("body"))
    .map('body -> 'ploc) {
      body: String =>
        val pattern = """GET /impression\S*&ploc=(\S+)&""".r
        pattern.findFirstMatchIn(body) match {
          // Extract the results of the regex capture
          case Some(matchData) => matchData.subgroups(0)
          // Rather than a separate flow for 'bad' data, let it flow through
          case None => "http://www.UNKNOWNLOCATION.com/"
        }
    }
    .map('ploc -> 'hostname) {
      ploc: String => new URI(URLDecoder.decode(ploc, "UTF-8")).getHost
    }
    .groupBy('hostname) { _.size }
    .write(Tsv(args("output")))
}
