package com.sharethrough.emr_tutorial

import com.twitter.scalding._
import java.net.URI
import java.net.URLDecoder

class LocationCountingJob(args: Args) extends Job(args) {
  TextLine(args("input"))
    // TextLine implicitly creates a 'line field
    .filter('line) {
      // Filter by lines that include the placementId we're looking for
      body: String =>
        body.matches(".*GET /impression\\S*pid="+args("placementId")+".*")
    }
    .map('line -> 'ploc) {
      // Extract the page location into a new 'ploc field
      body: String =>
        val pattern = """GET /impression\S*ploc=(\S+)(&| )""".r
        pattern.findFirstMatchIn(body) match {
          // Extract the page location
          case Some(matchData) => matchData.subgroups(0)
          // This will help us track the number of times we couldn't get the location
          case None => "http://www.UNKNOWNLOCATION.com/"
        }
    }
    .map('ploc -> 'hostname) {
      // Extract the hostname into a new 'hostname field
      ploc: String => new URI(URLDecoder.decode(ploc, "UTF-8")).getHost
    }
    // Count the number of impressions by 'hostname
    .groupBy('hostname) { _.size }
    .filter('hostname, 'size) {
      // We're only interested in the hosts who beat the impressionFloor.  We're
      // also interested in knowing how many impressions where the page location
      // was unavailable.
      fields: (String, Int) =>
        val (hostname, size) = fields
        hostname.equals("http://www.UNKNOWNLOCATION.com/") || size >= args("impressionFloor").toInt
    }
    .write(Tsv(args("output")))
}
