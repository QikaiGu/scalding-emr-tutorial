[![Build Status](https://secure.travis-ci.org/sharethrough/scalding-emr-tutorial.png)](http://travis-ci.org/sharethrough/scalding-emr-tutorial)

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

## Step 1 - Building The Tutorial
In order to get started, you'll need only [sbt](http://www.scala-sbt.org/).  If you're looking into Scalding, chances are this isn't your first Scala project :)

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
[info] Packaging /Users/rslifka/workspace/slif/scalding-emr-tutorial/target/scala-2.10/scalding_emr_tutorial-assembly-1.0.jar ...
[info] Done packaging.
[success] Total time: 23 s, completed Oct 8, 2013 11:20:55 AM
```

If something else went awry, [file an issue](https://github.com/sharethrough/scalding-emr-tutorial/issues) and we'll have a look.

## Step 2 - Installing Hadoop 1.0.3 (pseudo-optional)

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

## Step 3 - Executing in Local Mode

Now that the jar is assembled, we're ready to run the job locally before submitting to EMR.  Your output directory doesn't exist yet and that's OK, we're about to fill it up.

```
sfo-rslifka:~/workspace/scalding-emr-tutorial(master)$ ll data-output
ls: data/output: No such file or directory
```

Let's go ahead and kick off our job against the test data supplied in `./data`

```
hadoop \
  jar target/scala-2.10/scalding_emr_tutorial-assembly-1.0.jar \
  com.sharethrough.emr_tutorial.LocationCountingJob \
  --hdfs \
  --input "./data/*" \
  --output ./data-output \
  --placementId FAKE_PLACEMENT_ID \
  --impressionFloor 2
```

You'll see reams of Hadoop, Cascading and Scalding output stream by, the end result looking like this:

```
sfo-rslifka:~/workspace/scalding-emr-tutorial(master)$ ll data-output
total 8
-rwxrwxrwx  1 rslifka  staff   0 Oct  8 11:32 _SUCCESS
-rwxrwxrwx  1 rslifka  staff  99 Oct  8 11:32 part-00000
```

Notice the [_SUCCESS](https://issues.apache.org/jira/browse/MAPREDUCE-947) file, our job completed!  How did we do?

```
sfo-rslifka:~/workspace/scalding-emr-tutorial(master)$ cat data-output/part-00000 
www.allaboutbalance.com 3
www.badLocation.com 1
www.emptyOrNoLocation.com 3
www.sharethrough.com  2
```

...and there's our TSV, brilliant.

## Step 4 - Executing Remotely With Elasticity and Elastic MapReduce

Included in this tutorial is `elasticity.rb`, a small script that utilizes the [Elasticity](https://github.com/rslifka/elasticity) gem to submit your shiny new Scalding job to EMR.  It also relies on Elasticity to upload the test data prior to running the job, so you don't have to be concerned about how to make your job data EMR-accessible.

### Step 4a - Install Elasticity

If you're running a .ruby*-aware tool like [RVM](https://rvm.io/), you'll notice that you're using a new gemset in this folder: `scalding-emr-tutorial`.  Go ahead and install Elasticity:

```
gem install elasticity --no-rdoc --no-ri
```

### Step 4b - Create S3 Bucket
...or have the name of an existing bucket you'd like to use handy.  I'll use `my-test-bucket` for the remainder of this document.

### Step 4c - Configure Your AWS Credentials

These are only needed locally to launch the job via Elasticity, which inspects your environment for credentials.

```
export AWS_ACCESS_KEY_ID=your-access-key-here
export AWS_SECRET_ACCESS_KEY=your-secret-key-here
```

**NOTE**: we assume these credentials are valid for `us-east-1`.  If you'd like to change this, pop open `elasticity.rb` and edit the region setting.

### Step 4d - Launch Your Scalding Job

To launch your job, you need only provide the name of the bucket you created (or remebered) from Step 4b.

```
./elasticity.rb my-test-bucket
```

You'll see a bit of status fly by, culminating with the submission of your jobflow to EMR.

```
sfo-rslifka:~/workspace/scalding-emr-tutorial(master)$ ./elasticity.rb my-test-bucket
Running scalding-emr-tutorial...
Settings:
  Job Name   : Sharethrough Scalding EMR Tutorial (1381259280)
  Bucket Path: s3n://my-test-bucket/scalding-emr-tutorial/1381259280 (input and output stored here)
  Region     : us-east-1 (specified in elasticity.rb)
  PlacementID: FAKE_PLACEMENT_ID (specified in elasticity.rb)
  Impr. Floor: 2 (specified in elasticity.rb)
Uploading job jar => s3n://my-test-bucket/scalding-emr-tutorial/1381259280/lib
Uploading test ./data => s3n://my-test-bucket/scalding-emr-tutorial/1381259280/input
Submitting jobflow to EMR...
Submitted! jobflow ID is j-2S4HBS8L3QSU9
```

Head on over to the AWS EMR console to monitor your job.  You'll see it provisioning and configuring your instances followed by running the sole step we provided.  Once complete, use either the AWS S3 browser or a tool like [Transmit](http://panic.com/transmit/) to have a look at the output directory.

## Step 5 - PROFIT!

If you have a look at `s3n://my-test-bucket/scalding-emr-tutorial/1381259280/output` (replace `my-test-bucket` with your bucket name of course) you will see the same `_SUCCESS` and `part-00000` files as when the job was run locally.

## Next Steps

With the framework this tutorial provides, you can proceed in several directions:

1. Experiment locally with Scalding, test-driving your jobs without ever leaving `sbt ~test`.  Definitely refer to the Scalding team's [Fields-based API Reference](https://github.com/twitter/scalding/wiki/Fields-based-API-Reference).
1. Package and run your jobs locally with Hadoop.  Just throw everything into the `./data` directory and tweak the command-line invocation in Step 3.  This might be your go-to if you're digging around in some data locally before spinning up a much larger job on EMR.
1. Package and run your jobs up on EMR when you're ready to take advantage of Amazon's scale.  On 8 x m1.large instances, it took about 1 hour to process 14GB of data.

Enjoy!

## License

    Copyright 2013 Robert Slifka, Sharethrough

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.