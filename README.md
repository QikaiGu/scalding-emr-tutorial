## Installing Hadoop 1.0.3

As of 2013/09/26, EMR supports Hadoop 1.0.3 (among other, older versions).  To ensure we're developing against the same version we'll use in production, let's make sure we're running 1.0.3 locally.

```
# Assuming you have Homebrew installed
cd /usr/local/Library/

# Set Homebrew to 1.0.3, 
git checkout 3c5ca25 /usr/local/Library/Formula/hadoop.rb

# Edit hadoop.rb; download path has changed
# http://archive.apache.org/dist/hadoop/common/hadoop-1.0.3/hadoop-1.0.3.tar.gz
#
# vi /usr/local/Library/Formula/hadoop.rb

brew install hadoop

# You may have to force the link
# brew link --overwrite hadoop
```
