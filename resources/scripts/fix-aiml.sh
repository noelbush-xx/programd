#!/bin/sh

# Add AIML namespace and version number to root aiml element.
perl -pi -e '
    s/\<aiml\>/\<aiml version="1\.0\.1" xmlns="http\:\/\/alicebot\.org\/2001\/AIML\-1\.0\.1"\n      xmlns:xsi="http:\/\/www.w3.org\/2001\/XMLSchema-instance"\n      xsi:schemaLocation="http:\/\/alicebot.org\/2001\/AIML-1.0.1 http:\/\/aitools.org\/aiml\/schema\/AIML.xsd"\>/;

# Look for any (likely) HTML elements, add prefixes to them, and define this prefix in the root element of the containing documents.
    s/\<a /\<html:a /gi; s/\<\/a\>/\<\/html:a\>/gi;
    s/\<img (.+" *)\/?\>/\<html:img $1\/\>/gi;
    s/\<\/img\>//gi;
    s/\<br ?\/\>/\<html:br\/\>/gi;
    s/\<p\>/\<html:p\>/gi; s/\<\/p\>/\<\/html:p\>/gi;
    s/\<b\>/\<html:b\>/gi; s/\<\/b\>/\<\/html:b\>/gi;
    s/\<em\>/\<html:em\>/gi; s/\<\/em\>/\<\/html:em\>/gi;
    s/\<ul\>/\<html:ul\>/gi; s/\<\/ul\>/\<\/html:ul\>/gi;
    s/\<html:ul\>\<li\>/\<html:ul\>\<html:li\>/gi; s/\<\/li\>\<\/html:ul\>/\<\/html:li\>\<\/html:ul\>/gi;

# Known errors in the AAA set
    s/\<get_(.+)\/\>/\<get name="$1"\/\>/g;
    s/\<bot Name/\<bot name/g;
    s/\<str\/\>/\<star\/\>/g;
    s/\<p\/\>([^\<]+)/\<html:p\>$1\<\/html:p\>/g;
    s/\<\/li\> +(\<think.+\<\/think\>) +\<\/random\>/\<\/li\>\<\/random\>$1/g;
    s/condition name="ip" value="localhost"/condition name="ip" value="LOCALHOST"/g;
    s/\<\/category\>  n/\<\/category\>/g;
    s/\<\/li\>\. *\<\/random\>/\<\/li\>\<\/random\>./g;
    s/value="om"/value="OM"/g;
    s/\<person index="([0-9]+)"\/\>/\<person\>\<star index="$1"\/\>\<\/person\>/g' $1

# Add HTML namespace (this will add it each time, so don't run more than once).
grep -l "html:" $1 | xargs perl -pi -e 's/ xmlns="http:\/\/alicebot\.org\/2001\/AIML\-1\.0\.1"/ xmlns="http:\/\/alicebot.org\/2001\/AIML-1.0.1"\n      xmlns:html="http:\/\/www.w3.org\/1999\/xhtml"/'

