package com.sharethrough.emr_tutorial

import com.twitter.scalding._

class ImpressionCountingJob(args:Args) extends Job(args) {

  Tsv(args("input"), ImpressionCountingJob.inputFormat).
    map('full_request ->'placement_key) {
      request: String =>
        val params = QueryParamParser.parseFullRequest(request)
        val placement_key = (params.getOrElse("pkey", List("unknown"))(0))
        (placement_key)
    }.
    map('beacon_type -> 'impressionRequests) {
      fields: (String) =>
        fields match {
          case "impressionRequest" => 1
          case _ => 0
        }
    }.
    filter('placement_key) {
      fields: String => fields match {
        case "unknown" => false
        case _ => true
      }
    }.
    groupBy('placement_key) {
      _.size
    }.
    write(Tsv(args("output"))
  )
}

object ImpressionCountingJob {
  val inputFormat = (
    'date,
    'unix_time,
    'session_id,
    'full_request,
    'uri,
    'beacon_type,
    'user_id,
    'http_accept_language,
    'http_x_forwarded_for,
    'http_user_agent,
    'operating_system,
    'browser,
    'browser_version,
    'http_referer
  )
}