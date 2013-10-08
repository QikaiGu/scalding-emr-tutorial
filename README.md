## Objective

We're aiming to create the canonical "Scalding w/Testing" repo; the minimum of what's required to get started with Scalding on a problem of reasonable complexity.

Here's what you can expect:

* **Minimal reliance on external libraries:**  We might rely on regular expressions or some manual text parsing instead of relying on a library to facilitate this.  Things change quickly, especially where [JSON parsing](http://stackoverflow.com/questions/8054018/json-library-for-scala) in Scala is concerned.  Same goes for the small bit of Ruby included: manual argument parsing so you don't have to understand Thor.  By limiting dependencies, the goal is to keep this repo relevant and resilient.
* **Minimal indirection:**  When learning something new, it helps to be focused on precisely what we're discussing.  You know a regexp when you see it versus 3rd-party library utilization or using helpers to DRY things up.  We're trading off an ability quickly follow for something that is well-factored.  Once you're comfortable with Scalding, you'll have no problem refactoring :)

The list of components used in this tutorial:

* **For you**
  * [Scalding 0.8.11](https://github.com/twitter/scalding) - What you're here for!  "Scalding is a Scala library that makes it easy to specify Hadoop MapReduce jobs."
  * [Specs2 2.2](http://etorreborre.github.io/specs2/) - "specs2 is a library for writing executable software specifications.  With specs2 you can write software specifications for one class (unit specifications) or a full system (acceptance specifications)."
* **For plumbing**
  * [sbt-assembly](https://github.com/sbt/sbt-assembly) - Packages all dependencies into a single JAR file (immensely helpful when submitting Hadoop jobs).
  * [sbt-idea](https://github.com/mpeltonen/sbt-idea) - Generates IDEA project files.  Alright, this one isn't absolutely required but chances are some of you are like me and have been using IDEA since 2002.
  * [Elasticity](http://github.com/rslifka/elasticity)) - A Ruby wrapper around the AWS EMR API.  Allows you to work with EMR without having to figure out the EMR CLI (a very thin wrapper around the actual web API).

## Step 1 - Installing Hadoop 1.0.3 (pseudo-optional)

As of 2013/09/26, EMR supports Hadoop 1.0.3 (among other, older versions).  Yes, 1.2 is the latest and 2.x is in beta.  That's OK, it still works well enough for the purposes of this tutorial.  To ensure we're developing against the same version we'll use in production, let's make sure we're running 1.0.3 locally.  Alternatively, you can skip this step and proceed at your own risk.

```
# Assuming you have Homebrew installed
cd /usr/local/Library/

# Set Homebrew to 1.0.3
git checkout 3c5ca25 /usr/local/Library/Formula/hadoop.rb

# You're going to have to edit hadoop.rb.  Remember what I wrote about resiliency
# and dependencies? :)
#
# Here is the most recent path to Apache Hadoop 1.0.3:
#   http://archive.apache.org/dist/hadoop/common/hadoop-1.0.3/hadoop-1.0.3.tar.gz
#
# Edit hadoop.rb to reflect this (it will be obvious where to make the change)
#   vi /usr/local/Library/Formula/hadoop.rb
brew install hadoop

# You may have to force the link
# brew link --overwrite hadoop

# How did we do?
hadoop version

# Hadoop 1.0.3
# Subversion https://svn.apache.org/repos/asf/hadoop/common/branches/branch-1.0 -r 1335192
# Compiled by hortonfo on Tue May  8 20:31:25 UTC 2012
# From source with checksum e6b0c1e23dcf76907c5fecb4b832f3be

# Done!
```

## Step 2 - Building The Tutorial
Clone this repo and attempt to assemble the fat jar.

```
git clone git@github.com:sharethrough/scalding-emr-tutorial.git
cd scalding-emr-tutorial
sbt update assembly
```
You should see several lines of output fetching dependencies, running tests and resolving merge conflicts when building the jar.  If everything goes well, you'll end up with something like this:

```
[warn] Strategy 'concat' was applied to a file
[warn] Strategy 'discard' was applied to 4 files
[warn] Strategy 'last' was applied to 57 files
[warn] Strategy 'rename' was applied to 4 files
[info] Checking every *.class/*.jar file's SHA-1.
[info] SHA-1: 747b2d2ccf452993ded540dff420e93cc3449eb5
[info] Packaging /Users/rslifka/workspace/slif/scalding-emr-tutorial/target/scala-2.10/emr_tutorial-assembly-1.0.jar ...
[info] Done packaging.
[success] Total time: 23 s, completed Oct 8, 2013 11:20:55 AM
```



## Step 1 - Executing in Local Mode

```
hadoop jar target/scala-2.10/emr_tutorial-assembly-1.0.jar com.sharethrough.emr_tutorial.LocationCountingJob --hdfs --input "./data/*" --output ./data/output --placementId 51 --impressionFloor 2
```