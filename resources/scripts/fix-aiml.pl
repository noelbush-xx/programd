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
	    s/\<(aiml|alice)( version=".+")?\>/\<aiml version="1\.0\.1" xmlns="http\:\/\/alicebot\.org\/2001\/AIML\-1\.0\.1"\n      xmlns:html="http:\/\/www.w3.org\/1999\/xhtml"\n      xmlns:xsi="http:\/\/www.w3.org\/2001\/XMLSchema-instance"\n      xsi:schemaLocation="http:\/\/alicebot.org\/2001\/AIML-1.0.1 http:\/\/aitools.org\/aiml\/schema\/AIML.xsd"\>/;
		s/\<\/alice\>/\<\/aiml\>/gis;
		
		# Known AIML errors in the AAA set
	    s/\<get_(.+)\/\>/\<get name="$1"\/\>/gi;
	    s/\<bot Name/\<bot name/gi;
	    s/\<str\/\>/\<star\/\>/gi;
	    s/\<p\/\>([^\<]+)/\<html:p\>$1\<\/html:p\>/gi;
	    s/\<\/li\> +(\<think.+\<\/think\>) +\<\/random\>/\<\/li\>\<\/random\>$1/gi;
	    s/value="([a-z]+)"/value="\U$1"/gs;
	    s/\<\/category\>  n/\<\/category\>/gi;
	    s/\<\/li\>\.\r?\n?\<\/random\>/\<\/li\>$1\<\/random\>./gis;
	    s/\<person index="([0-9]+)"\/\>/\<person\>\<star index="$1"\/\>\<\/person\>/gi;
		    
		# Known AIML errors in the "standard" AIML set
		s/\<meta.+\/\>\r?\n?//gi;
		s/\<favoritemovie\/\>/\<bot name="favoritemovie"\/\>/gi;
		s/\<favoritesong\/\>/\<bot name="favoritesong"\/\>/gi;
		s/\<for_fun\/\>/\<bot name="for_fun"\/\>/gi;
		s/\<javascript\> *\r?\n?(\<!\[CDATA\[)?(.+?)(\]\]\>)?\r?\n?<\/javascript\>/\<javascript>\<![CDATA[$2]]\>\<\/javascript\>/gis;
		s/\<display[^\>]*\>\r?\n?(\<!\[CDATA\[)?(http[^\]]+?)(\]\]\>)?\r?\n?\<\/display\>/\<html:script language="javascript">\<![CDATA[window.open('$2', '_blank');]]\>\<\/html:script\>/gis;
		s/(\<!\[CDATA\[)(?!\]\]\>)(.*?)(?!\]\]\>)(\<[^\>!]+?\/\>)(?!\]\]\>)(.*?)(?!\]\]\>)(\]\]\>)/$1$2]]\>$3\<![CDATA[$4$5/gis;
		s/\<(java)?script *\r?\n?language="javascript"\>(.+?)\<\/(java)?script\>/\<html:script language="javascript"\>\<![CDATA[$2]]\>\<\/html:script\>/gis;
		s/minhutes/minutes/gis;
		s/ ok\?\n\<\/random\>/\<\/random\> ok?/s;
		s/topic name="lizards"/topic name="LIZARDS"/gis;
		s/topic name="me"/topic name="ME"/gis;
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
			
		# Known errors in the A.L.I.C.E. AIML set.
		s/(\<\/li\>\s*)(\<think\>.*?\<\/think\>\s*)(\<\/random\>)/$1$3$2/gis;
		s/\<justhat\/\>/\<that index="2"\/\>/gis;
		s/\<getgender\/\>/\<get name="gender"\/\>/gis;
		s/\<getlocation\/\>/\<get name="location"\/\>/gis;
		s/\<getjob\/\>/\<get name="job"\/\>/gis;
		s/\<birthdate\/\>/\<bot name="birthdate"\/\>/gis;
		s/\<thatstat\/\>/\<thatstar\/\>/gis;
		
		# Known errors in the French AIML set.
		s/\<\/pattern\>[^\<]+\<template\>/\<\/pattern\>\<template\>/gis;
		s/\<set name="\*"\>\<\/set\>//gis;
		s/get name=" +(.+?) +"/get name="$1"/gis;
		s/name= *"le travail"/name="le_travail"/gis;
		s/\<\/a \>/\<\/a\>/gis;
		s/\<\/category\>[^\<]+\<category\>/\<\/category\>\n\<category\>/gis;
		s/\<\/that\>[^\<]+\<template\>/\<\/that\>\n\<template\>/gis;
		s/\<\/template\>[^\<]+\<\/category\>/\<\/template\>\n\<\/category\>/gis;
		s/\<\/pattern\>(\<srai\>.*?\<\/srai\>)\<template\>/\<\/pattern\>\<template\>$1/gis;
		#s/\<\/pattern\>\<srai/\<\/pattern\>\<template\>\<srai\>/gis;
		s/nomment *= */name=/gis;
		s/\<taille\/\>//gis;
		s/lles\. \<\/li\> \<\/te/lles.\<\/te/gis;
		s/Je ne suis pas ton type de robot\. \<\/li\>/Je ne suis pas ton type de robot./gi;
		s/index =" 2,1"/index="2,1"/gis;
		s/\<topic\/\>/\<get name="topic"\/\>/gis;
		s/name="niveau.acc.s"/name="niveau_acc_s"/gis;
		s/name="privil.ges"/name="privil_ges"/gis;
		s/index =" 2"/index="2"/gis;
		s/name= "l'Oge"/name="lage"/gis;
		s/name="le sujet"/name="le_sujet"/gis;
		
		# Known errors in the German AIML set.
		s/\<set_([a-z]+)\>/\<set name="$1"\>/gis;
		s/\<\/set_[a-z]+\>/\<\/set\>/gis;
		s/\<srai\>Welche Farbe hat Dein BH\?\<\/template\>/Welche Farbe hat Dein BH?\<\/template\>/gis;
		s/botasmter/botmaster/gis;
		s/\<botmaster\/\>/\<bot name="master"\/\>/gis;
		s/\<A HREF="http:\/\/www\.alicebot\.com"\>The A\.L\.I\.C\.E\. nexus\.\<\/template\>/\<a href="http:\/\/www.alicebot.com"\>The A.L.I.C.E. nexus\<\/a\>.\<\/template\>/gs;
		s/\<\/li\>Was sind Deine/\<li\>Was sind Deine/gis;
		s/\<a href="http:\/\/www\.alicebot\.org"\> The alice Nexus!\<\/template\>/\<a href="http:\/\/www\.alicebot\.org"\>The alice Nexus\<\/a\>!\<\/template\>/gis;
		s/\<template\>\<srai\>ICH BIN \<star\/\> JAHRE ALT\<\/template\>/\<template\>\<srai\>ICH BIN \<star\/\> JAHRE ALT\<\/srai\>\<\/template\>/gis;
		s/\<template\>Du klingst ueberzeugend\. \<sr\/\> \<\/srai\>/\<template\>\<srai\>Du klingst ueberzeugend\. \<sr\/\> \<\/srai\>/gis;
		s/\<A HREF="http:\/\/www\.alicebot\.net"\> www\.alicebot\.net \.\<\/template\>/\<a href="http:\/\/www.alicebot.net"\>www.alicebot.net\<\/a\>.\<\/template\>/gis;
		s/\<A HREF="http:\/\/german\.alicebot\.com"\> meiner Homepage\<\/template\>/\<a href="http:\/\/german.alicebot.com"\>meiner Homepage\<\/a\>.\<\/template\>/gis;
		s/\<a href="http:\/\/www\.charthitz\.da\.ru\> MP3Hitz\.\<\/template\>/\<a href="http:\/\/www.charthitz.da.ru"\>MP3Hitz\<\/a\>.\<\/template\>/gis;
		s/ich werde Dich \<setname\/\> nennen/ich werde Dich \<set name="name"\>\<star\/\>\<\/set\> nennen/gis;
		s/\<srai\>KANNST DU \<star\/\>\<\/template\>/\<srai\>KANNST DU \<star\/\>\<\/srai\>\<\/template\>/gis;
		s/\<\/random\>\<setname\/\>\.\<\/template\>/\<\/random\>\<set name="name"\>\<star\/\>\<\/set\>\.\<\/template\>/gis;
		s/\<srai\>WAS SIND \<star\/\>\<\/template\>/\<srai\>WAS SIND \<star\/\>\<\/srai\>\<\/template\>/gis;
		s/\<a href="http:\/\/www\.cinemaxx\.de"\> die CinemaXX Homepage\.\<\/template\>/\<a href="http:\/\/www.cinemaxx.de"\>die CinemaXX Homepage\<\/a\>.\<\/template\>/gis;
		s/code=Sys width=350 height=200/code="Sys" width="350" height="200"/gis;
		s/\<getsize\/\>/\<size\/\>/gis;
		s/\<template\>In \<\/location\>\<\/template\>/\<template\>In \<bot name="location"\/\>\<\/template\>/gis;
		s/\<location\/\>/\<bot name="location"\/\>/gis;
		s/\<get_thema\/?\>/\<get name="thema"\/\>/gis;
		
		# Known errors in the Italian AIML set (Maria).
		s/\<set_it\>/\<set name="it"\>/gis;
		s/\<\/set_it\>/\<\/set\>/gis;
		s/\<set_want\>/\<set name="want"\>/gis;
		s/\<\/set_want\>/\<\/set\>/gis;
		s/\<set_he\>/\<set name="he"\>/gis;
		s/\<\/set_he\>/\<\/set\>/gis;
		s/\<set_does\>/\<set name="does"\>/gis;
		s/\<\/set_does\>/\<\/set\>/gis;
		s/\<set_like\>/\<set name="like"\>/gis;
		s/\<\/set_like\>/\<\/set\>/gis;
		s/\<getname\/\>/\<get name="name"\/\>/gis;
		s/\<\/getname\>/\<get name="name"\/\>/gis;
		s/gettopic/get name="topic"/gis;
		s/\<name\/\>/\<bot name="name"\/\>/gis;
		s/\<favorite_song\/\>/\<bot name="favorite_song"\/\>/gis;
		s/\<birthday\/\>/\<bot name="birthday"\/\>/gis;
		s/\<settopic\>/\<set name="topic"\>/gis;
		s/\<\/settopic\>/\<\/set\>/gis;
		s/\<template\>L'elettrita' dove va' quando spegni l'aspirapolvere\?\<\/set\>\<\/set\>\<\/think\>\<\/template\>/\<template\>L'elettrita' dove va' quando spegni l'aspirapolvere?\<\/template\>/gis;
		s/\<template\>Nel paese dei singhiozzi perduti, dove si gioca  a ping il pin g pong per solitari, e dove gli elettroni usciti dagli aspirapolveri spente vanno a bersi la birra.\<\/set\>\<\/set\>\<\/think\>\<\/template\>/\<template\>Nel paese dei singhiozzi perduti, dove si gioca  a ping il pin g pong per solitari, e dove gli elettroni usciti dagli aspirapolveri spente vanno a bersi la birra.\<\/template\>/gis;
		s/topic name="gattini"/topic name="GATTINI"/gis;
		s/topic name="nutrire i gattini"/topic name="NUTRIRE I GATTINI"/gis;
		s/\<\/category\>\s*\<pattern\>/\<\/category\>\n\n\<category\>\n\<pattern\>/gis;
		s/\<category\>\s*\<category\>/\<category\>/gis;
			
		# Look for any (likely) HTML elements and add prefixes to them
	    s/\<(a|applet|b|em|script|table|td|tr|ul)\b(\r?\n?)/\<html:\L$1\L$2/gi;
	    s/\<\/(a|applet|b|em|script|table|td|tr|ul)\>/\<\/html:\L$1\>/gi;
	    s/\<img\b([^\>]*)\/*\>/\<html:img$1\/\>/gi;
	    s/\<\/img\>//gi;
	    s/\<br\b *\r?\n?\/?\>/\<html:br\/\>/gi;
	    s/\<p\b/\<html:p/gi;
	    s/\<\/p\>/\<\/html:p\>/gi;
	    s/\<html:ul\>\s*\<li\>(.*?)\<\/li\>/\<html:ul\>\n  \<html:li\>$1\<\/html:li\>/gis;
	    s/\<\/html:li\>\s*\<li\>(.*?)\<\/li\>/\<\/html:li\>\n  \<html:li\>$1\<\/html:li\>/gis;
	    s/(\<\/html:li\>)\s*\<li\>(.*?)\<\/li\>\s*\<\/html:ul\>/$1\n  \<html:li\>$2\<\/html:li\>\n\<\/html:ul\>/gis;
		
		# Print the result
		print OUT "$_";
			
		# Remove the temporary file.
		unlink "$_.tmp";
	}
}
print "Done.\n";
