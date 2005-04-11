#!/bin/sh

# Add AIML namespace and version number to root aiml element.
perl -pi -e 's/\<aiml\>/\<aiml version="1\.0\.1" xmlns="http\:\/\/alicebot\.org\/2001\/AIML\-1\.0\.1"\n      xmlns:xsi="http:\/\/www.w3.org\/2001\/XMLSchema-instance"\n      xsi:schemaLocation="http:\/\/alicebot.org\/2001\/AIML-1.0.1 http:\/\/aitools.org\/aiml\/schema\/AIML.xsd"\>/' *.aiml

# Look for any (likely) HTML elements, add prefixes to them, and define this prefix in the root element of the containing documents.
perl -pi -e 's/\<a /\<html:a /gi; s/\<\/a\>/\<\/html:a\>/gi' *.aiml
perl -pi -e 's/\<img /\<html:img /gi' *.aiml
perl -pi -e 's/\<br ?\/\>/\<html:br\/\>/gi' *.aiml
perl -pi -e 's/\<p\>/\<html:p\>/gi; s/\<\/p\>/\<\/html:p\>/gi' *.aiml
perl -pi -e 's/\<em\>/\<html:em\>/gi; s/\<\/em\>/\<\/html:em\>/gi' *.aiml
perl -pi -e 's/\<ul\>/\<html:ul\>/gi; s/\<\/ul\>/\<\/html:ul\>/gi' *.aiml
perl -pi -e 's/\<html:ul\>\<li\>/\<html:ul\>\<html:li\>/gi; s/\<\/li\>\<\/html:ul\>/\<\/html:li\>\<\/html:ul\>/gi' *.aiml

# Known errors in the AAA set
perl -pi -e 's/\<get_(.+)\/\>/\<get name="$1"\/\>/g' *.aiml
perl -pi -e 's/\<bot Name/\<bot name/g' *.aiml
perl -pi -e 's/\<str\/\>/\<star\/\>/g' *.aiml

grep -l "html:" *.aiml | xargs perl -pi -e 's/ xmlns="http:\/\/alicebot\.org\/2001\/AIML\-1\.0\.1"/ xmlns="http:\/\/alicebot.org\/2001\/AIML-1.0.1"\n      xmlns:html="http:\/\/www.w3.org\/1999\/xhtml"/'
