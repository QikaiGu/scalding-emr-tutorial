package com.sharethrough.emr_tutorial

import com.twitter.scalding._
import java.net.{URISyntaxException, URI, URLDecoder}

class LocationCountingJob(args: Args) extends Job(args) {
  TextLine(args("input"))
    // TextLine implicitly creates a 'line field
    .filter('line) {
      // Filter by lines that include the placementId we're looking for
      line: String =>
        line.matches(".*GET /impression\\S*pid="+args("placementId")+".*")
    }
    .map('line -> 'ploc) {
      // Extract the placement location into a new 'ploc field
      line: String =>
        // Extract the placement location from the URL
        val pattern = """GET /impression\S*ploc=(\S*)&""".r
        pattern.findFirstMatchIn(line) match {
          case Some(matchData) => matchData.subgroups(0)
          // This will help us track the number of times we couldn't get the location
          case None => "http://www.emptyOrNoLocation.com/"
        }
    }
    .map('ploc -> 'hostname) {
      // Extract the hostname into a new 'hostname field
      ploc : String =>
        try {
          // When ploc is empty, getHost will return NULL
          val hostname = Option(new URI(URLDecoder.decode(ploc, "UTF-8")).getHost)
          hostname.getOrElse("www.emptyOrNoLocation.com")
        } catch {
          case _: URISyntaxException => "www.badLocation.com"
        }
    }
    // Count the number of impressions by 'hostname
    .groupBy('hostname) { _.size }
    .filter('hostname, 'size) {
      // We're only interested in the hosts who beat the impressionFloor.  We're
      // also interested in preserving stats for impressions where we couldn't
      // capture placement location
      fields: (String, Int) =>
        val (hostname, size) = fields
        hostname match {
          case "www.emptyOrNoLocation.com" => true
          case "www.badLocation.com" => true
          case _ => size >= args("impressionFloor").toInt
        }
    }
    .write(Tsv(args("output")))
}
