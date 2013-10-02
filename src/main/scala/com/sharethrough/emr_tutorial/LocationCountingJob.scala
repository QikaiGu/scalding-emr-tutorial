package com.sharethrough.emr_tutorial

import com.twitter.scalding._
import java.net.{URISyntaxException, URI, URLDecoder}

class LocationCountingJob(args: Args) extends Job(args) {
  TextLine(args("input"))
    // TextLine implicitly creates a 'line field
    .filter('line) {
      // Filter by lines that include the placementId we're looking for
      body: String =>
        body.matches(".*GET /impression\\S*pid="+args("placementId")+".*")
    }
    .map('line -> 'ploc) {
      // Extract the placement location into a new 'ploc field
      body: String =>
        // Extract the placement location from the URL
        val pattern = """GET /impression\S*ploc=(\S*)&""".r
        pattern.findFirstMatchIn(body) match {
          case Some(matchData) => matchData.subgroups(0)
          // This will help us track the number of times we couldn't get the location
          case None => "http://www.UNKNOWNLOCATION.com/"
        }
    }
    .map('ploc -> 'hostname) {
      // Extract the hostname into a new 'hostname field
      ploc : String =>
        try {
          val hostname = new URI(URLDecoder.decode(ploc, "UTF-8")).getHost
          // When ploc is empty, hostname is NULL
          if (hostname != null) hostname else "www.UNKNOWNLOCATION.com"
        } catch {
          case _: URISyntaxException => "www.UNKNOWNLOCATION.com"
        }
    }
    // Count the number of impressions by 'hostname
    .groupBy('hostname) { _.size }
    .filter('hostname, 'size) {
      // We're only interested in the hosts who beat the impressionFloor.  We're
      // also interested in knowing how many impressions where the placement location
      // was unavailable.
      fields: (String, Int) =>
        val (hostname, size) = fields
        hostname.equals("http://www.UNKNOWNLOCATION.com/") || size >= args("impressionFloor").toInt
    }
    .write(Tsv(args("output")))
}
