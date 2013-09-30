package com.sharethrough.emr_tutorial

import com.twitter.scalding._
import java.net.URI
import java.net.URLDecoder

class LocationCountingJob(args: Args) extends Job(args) {
  JsonLine(args("input"), List("body"))
    .filter('body) {
      body: String =>
        body.matches(".*GET /impression\\S*pid="+args("placementId")+".*")
    }
    .map('body -> 'ploc) {
      body: String =>
        val pattern = """GET /impression\S*ploc=(\S+)(&| )""".r
        pattern.findFirstMatchIn(body) match {
          case Some(matchData) => matchData.subgroups(0)
          case None => "http://www.UNKNOWNLOCATION.com/"
        }
    }
    .map('ploc -> 'hostname) {
      ploc: String => new URI(URLDecoder.decode(ploc, "UTF-8")).getHost
    }
    .groupBy('hostname) { _.size }
    .filter('hostname, 'size) {
      fields: (String, Int) =>
        val (hostname, size) = fields
        hostname.equals("http://www.UNKNOWNLOCATION.com/") || size >= args("impressionFloor").toInt
    }
    .write(Tsv(args("output")))
}
