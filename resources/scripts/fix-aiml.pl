#!/usr/bin/perl
undef $/;           # each read is whole file
foreach (@ARGV)
{
	print "Processing $_\n";
	rename("$_", "$_.tmp");
	open(IN, "$_.tmp");
	open(OUT, ">$_");
	while (<IN>) {
		# Add AIML namespace and version number to root aiml element.
	    s/\<aiml( version=".+")?\>/\<aiml version="1\.0\.1" xmlns="http\:\/\/alicebot\.org\/2001\/AIML\-1\.0\.1"\n      xmlns:html="http:\/\/www.w3.org\/1999\/xhtml"\n      xmlns:xsi="http:\/\/www.w3.org\/2001\/XMLSchema-instance"\n      xsi:schemaLocation="http:\/\/alicebot.org\/2001\/AIML-1.0.1 http:\/\/aitools.org\/aiml\/schema\/AIML.xsd"\>/;
		
		# Known AIML errors in the AAA set
	    s/\<get_(.+)\/\>/\<get name="$1"\/\>/gi;
	    s/\<bot Name/\<bot name/gi;
	    s/\<str\/\>/\<star\/\>/gi;
	    s/\<p\/\>([^\<]+)/\<html:p\>$1\<\/html:p\>/gi;
	    s/\<\/li\> +(\<think.+\<\/think\>) +\<\/random\>/\<\/li\>\<\/random\>$1/gi;
	    s/condition name="ip" value="localhost"/condition name="ip" value="LOCALHOST"/gi;
	    s/\<\/category\>  n/\<\/category\>/gi;
	    s/\<\/li\>\.\r?\n?\<\/random\>/\<\/li\>$1\<\/random\>./gis;
	    s/value="om"/value="OM"/gi;
	    s/\<person index="([0-9]+)"\/\>/\<person\>\<star index="$1"\/\>\<\/person\>/gi;
		    
		# Known AIML errors in the "standard" AIML set
		s/\<meta.+\/\>\r?\n?//gi;
		s/\<favoritemovie\/\>/\<bot name="favoritemovie"\/\>/gi;
		s/\<favoritesong\/\>/\<bot name="favoritesong"\/\>/gi;
		s/\<for_fun\/\>/\<bot name="for_fun"\/\>/gi;
		s/\<javascript\> *\r?\n?(\<!\[CDATA\[)?(.+?)(\]\]\>)?\r?\n?<\/javascript\>/\<javascript>\<![CDATA[$2]]\>\<\/javascript\>/gis;
		s/\<display[^\>]*\>\r?\n?(\<!\[CDATA\[)?(http[^\]]+?)(\]\]\>)?\r?\n?\<\/display\>/\<html:script language="javascript">\<![CDATA[window.open('$2', '_blank');]]\>\<\/html:script\>/gis;
		s/(\<!\[CDATA\[)(?!\]\]\>)(.*?)(?!\]\]\>)(\<[^\>!]+?\/\>)(?!\]\]\>)(.*?)(?!\]\]\>)(\]\]\>)/$1$2]]\>$3\<![CDATA[$4$5/gis;
		s/\<javascript\r?\n?language="javascript"\>(.+?)\<\/javascript\>/\<html:script language="javascript"\>\<![CDATA[$1]]\>\<\/html:script\>/gis;
		s/minhutes/minutes/gis;
		s/ ok\?\n\<\/random\>/\<\/random\> ok?/s;
		s/topic name="lizards"/topic name="LIZARDS"/gis;
		s/topic name="me"/topic name="ME"/gis;
		s/value="sick"/value="SICK"/gis;
		s/value="married"/value="MARRIED"/gis;
		s/value="male"/value="MALE"/gis;
		s/value="female"/value="FEMALE"/gis;
		s/value="gay"/value="GAY"/gis;
		s/value="hetro"/value="HETERO"/gis;
		s/value="bi sexual"/value="BISEXUAL"/gis;
		# Empty values not allowed (strange but true)
		s/value=""/value="UNKNOWN"/gis;
		# Approximate the "if" in one case (who cares?)
		s/\<if expr="(.+?) == (.+?)"\>/\<condition name="$1" value="$2"\>/gis;
		s/\<\/if\>/\<\/condition\>/gis;
		# Eliminate categories using unimplemented functionality.
		s/(\<category\>\n\<pattern\>DO IF EXIST TEST\<\/pattern\>.+?\<\/category\>)/\<!--$1--\>/s;
		s/(\<category\>\n\<pattern\>\* IS ONE OF MY KIDS\<\/pattern\>.+?\<\/category\>)/\<!--$1--\>/s;
		s/(\<category\>\n\<pattern\>START TIMER\<\/pattern\>.+?\<\/category\>)/\<!--$1--\>/s;
		s/(\<category\>\n\<pattern\>KILL TIMER\<\/pattern\>.+?\<\/category\>)/\<!--$1--\>/s;
		# Kill the rest of the corrupted "std-dont" file
		s/\(GUI\).   4s \(with each reFensible for the evalsponse te AIML.+/(GUI).\n\<\/template\>\n\<\/category\>\n\<\/aiml\>/s;
			
		# Look for any (likely) HTML elements and add prefixes to them
	    s/\<(a|applet|b|em|table|td|tr|ul)\b(\r?\n?)/\<html:$1$2/gi;
	    s/\<\/(a|applet|b|em|table|td|tr|ul)\>/\<\/html:$1\>/gi;
	    s/\<img\b([^\>]*)\/?\>/\<html:img$1\/\>/gi;
	    s/\<\/img\>//gi;
	    s/\<br\b *\r?\n?\/?\>/\<html:br\/\>/gi;
	    s/\<p\b/\<html:p/gi;
	    s/\<\/p\>/\<\/html:p\>/gi;
	    #s/\<template\>(.*?)\<html:ul\>(.*?)\<li\>(.*?)\<\/template\>/\<template\>$1\<html:ul\>$2\<html:li\>$3\<\/template\>/gis;
	    s/\<\/li\>(?!\<\/random\>|\<\/template\>)(.*?)(\<li\>|\<\/html:ul\>)/\<\/html:li\>$1$2/gis;
		
		# Print the result
		print OUT "$_";
			
		# Remove the temporary file.
		unlink("$_.tmp");
	}
}
