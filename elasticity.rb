#!/usr/bin/env ruby

BLUE = "\033[34m"
GREEN = "\033[32m"
RED = "\033[31m"
YELLOW = "\033[33m"
WHITE = "\033[37m"

begin
  require 'elasticity'
rescue LoadError
  puts "#{RED}Elasticity is required to launch jobs on EMR (https://github.com/rslifka/elasticity)"
  puts "#{GREEN}  > gem install elasticity"
  exit
end

if ARGV.length != 1
  puts "#{YELLOW}Usage: elasticity <bucket_name>"
  puts "#{WHITE}"
  puts "  bucket_name: The globally unique name of your S3 bucket where"
  puts "               assets will be uploaded and where output will be"
  puts "               stored."
  puts
  puts "#{YELLOW}What about AWS keys?  They're pulled from your environment; the"
  puts "AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY variables."
  exit
end

bucket_name      = ARGV[0]
placement_id     = 'FAKE_PLACEMENT_ID'
impression_floor = 2
timestamp        = Time.new.to_i
bucket_path      = "scalding-emr-tutorial/#{timestamp}"
input_path       = "#{bucket_path}/input"
output_path      = "#{bucket_path}/output"
log_path         = "#{bucket_name}/#{bucket_path}/logs"
jobflow_name     = "Sharethrough Scalding EMR Tutorial (#{timestamp})"
fat_jar          = 'scalding_emr_tutorial-assembly-1.0.jar'
region           = 'us-east-1'

puts "#{YELLOW}Running scalding-emr-tutorial..."
puts "Settings:"
puts "  #{YELLOW}Job Name   : #{WHITE}#{jobflow_name}"
puts "  #{YELLOW}Bucket Path: #{WHITE}s3n://#{bucket_name}/#{bucket_path} #{BLUE}(input and output stored here)"
puts "  #{YELLOW}Region     : #{WHITE}#{region} #{BLUE}(specified in elasticity.rb)"
puts "  #{YELLOW}PlacementID: #{WHITE}#{placement_id} #{BLUE}(specified in elasticity.rb)"    
puts "  #{YELLOW}Impr. Floor: #{WHITE}#{impression_floor} #{BLUE}(specified in elasticity.rb)"

# nil signifies that the AWS environment variables should be used
s3 = Elasticity::SyncToS3.new(bucket_name, nil, nil, region)

puts "#{YELLOW}Uploading job jar #{WHITE}=> s3n://#{bucket_name}/#{bucket_path}/lib"
s3.sync("./target/scala-2.10/#{fat_jar}", "#{bucket_path}/lib")

puts "#{YELLOW}Uploading test ./data #{WHITE}=> s3n://#{bucket_name}/#{input_path}"
s3.sync("./data/click-stream.log", "#{input_path}")

puts "#{YELLOW}Submitting jobflow to EMR..."
jobflow                = Elasticity::JobFlow.new
jobflow.name           = jobflow_name
jobflow.hadoop_version = '1.0.3'
jobflow.log_uri        = "s3n://#{log_path}"
jobflow.placement      = "#{region}c"

# Scalding jobs aren't directly supported; use the "Custom Jar" EMR job type
step = Elasticity::CustomJarStep.new("s3://#{bucket_name}/#{bucket_path}/lib/#{fat_jar}")

# Here are the arguments to pass to the jar
step.arguments = %W(
  com.sharethrough.emr_tutorial.LocationCountingJob
  --hdfs
  --input s3://#{bucket_name}/#{input_path}
  --output s3://#{bucket_name}/#{output_path}
  --placementId #{placement_id}
  --impressionFloor #{impression_floor}
)

# Add the step to the jobflow
jobflow.add_step(step)

# Send up to Amazon
jobflow_id = jobflow.run
puts "#{GREEN}Submitted! jobflow ID is #{BLUE}#{jobflow_id}"
