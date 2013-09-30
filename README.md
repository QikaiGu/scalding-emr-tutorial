## Objective

The goal is to create the canonical "Scalding w/Testing" repo, with the minimum of what's required to get started with Scalding on a problem of reasonable complexity.  Here's what you can expect:

* Minimal reliance on external libraries.  We might rely on regular expressions or some manual text parsing instead of relying on a library to facilitate this.  Things change quickly, especially where Scala is concerned and **especially** where [JSON parsing](http://stackoverflow.com/questions/8054018/json-library-for-scala) in Scala is concerned :)  By limiting dependencies, the goal is to keep this repo relevant (and resilient).
* Minimal indirection.  When learning something new, it helps to be focused on precisely what we're discussing.  You know a regexp when you see it versus 3rd-party library utilization.

## Installing Hadoop 1.0.3

As of 2013/09/26, EMR supports Hadoop 1.0.3 (among other, older versions).  To ensure we're developing against the same version we'll use in production, let's make sure we're running 1.0.3 locally.

```
# Assuming you have Homebrew installed
cd /usr/local/Library/

# Set Homebrew to 1.0.3
git checkout 3c5ca25 /usr/local/Library/Formula/hadoop.rb

# Edit hadoop.rb; download path has changed
# Remember what I wrote about resiliency and dependencies? :)
# http://archive.apache.org/dist/hadoop/common/hadoop-1.0.3/hadoop-1.0.3.tar.gz
#
# vi /usr/local/Library/Formula/hadoop.rb

brew install hadoop

# You may have to force the link
# brew link --overwrite hadoop
```

## Building sharethrough/emr_tutorial

```
# Clone
# cd
# assemble
```

## Executing in Local Mode

```
hadoop jar target/scala-2.10/emr_tutorial-assembly-1.0.jar com.sharethrough.emr_tutorial.ReferrerCountingJob --input ./data/nginx-ip-10-118-229-212.ec2.internal.1377990007430 --output ./data/output --hdfs
```