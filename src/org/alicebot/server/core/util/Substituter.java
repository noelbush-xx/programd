package org.alicebot.server.core.util;


/**
Alice Program D
Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
USA.

@author  Richard Wallace
@author  Jon Baer
@author  Thomas Ringate/Pedro Colla
@version 4.1.1
*/


// The Substituer class contains methods for the several type of
// syntactic substitutions performed by Program B.
// It has been updated to be included in ProgramD version 4.1.1 b1

import java.io.*;
import java.util.*;

import org.alicebot.server.core.*;

/**
  The substituter class contains methods for the several type of
  syntactic substitutions performed by ProgramD.
  @version 4.1.1
  @author  Richard S. Wallace
  @author  Jon Baer
  @author  Thomas Ringate/Pedro Colla
*/
public class Substituter extends Object implements Serializable {

        /**
         Transformation table Plain Text to HTML
        */

	static String http_reverse_subst[][] = {
		{"%","%25"},
		{"\"","%22"},
		{"\\","%2F"},
		{" ","%20"},
		{"!","%21"},
		{".","%22"},
		{"?","%23"},
		{"$","%24"},
		{"&","%26"},
		{"'","%27"},
		{"(","%28"},
		{")","%29"},
		{"+","%2B"},
		{",","%2C"},
		{":","%3A"},
		{",","%3B"},
		{"<","%3C"},
		{"=","%3D"},
		{">","%3E"},
		{"?","%3F"}};

        /**
         Transformation table HTML to Plain Text
        */
	
	static String http_subst[][] = {
		{"%0D"," "},
		{"%0A"," "},
		{"%20"," "},
		{"%21","!"},
		{"%22","\""},
		{"%23","?"},
		{"%24","$"},
		{"%25","%"},
		{"%26","&"},
		{"%27","'"},
		{"%28","("},
		{"%29",")"},
		{"%2A"," "},
		{"%2B","+"},
		{"%2C",","},
		{"%2D","-"},
		{"%2E","."},
		{"%2F","\\"},
		{"%2a"," "},
		{"%2b","+"},
		{"%2c",","},
		{"%2d","-"},
		{"%2e","."},
		{"%2f","\\"},
		{"%3A",":"},
		{"%3B",","},
		{"%3C","<"},
		{"%3D","="},
		{"%3E",">"},
		{"%3F","?"}, 
		{"%3a",":"},
		{"%3b",","},
		{"%3c","<"},
		{"%3d","="},
		{"%3e",">"},
		{"%3f","?"}, 
		{"%40","@"},
		{"%5B"," "},
		{"%5C"," "},
		{"%5D","]"},
		{"%5E","^"},
		{"%5F","_"},
		{"%60","`"},
		{"%7B","{"},
		{"%7C","|"},
		{"%7D","}"},
		{"%7E","~"},
		{"%92","'"},
		{"%B4"," "},
		{"%E9"," "},
		// German characters:
		{"%C4","Ae"},
		{"%E4","ae"},
		{"%D6","Oe"},
		{"%F6","oe"},
		{"%DC","Ue"},
		{"%FC","ue"},
		{"%DF","ss"},
		{"HTTP/1.0"," "},
		{"HTTP/1.1"," "},
		{"404"," "},
		{"206"," "},
		{"text=",""},
		{"virtual=none",""},
		{"submit"," "},
		{"=Reply"," "}
	}; // http_subst

        /**
         De-Periodize transformation table
        */

	static String period_subst[][] = {
		{"{", " BEGINSCRIPT "},
		{"}", " ENDSCRIPT "},
		{"\""," "},
		{"\\"," "},
		{":0"," 0"},
		{": 0"," 0"},
		{":1"," 1"},
		{": 1"," 1"},
		{":2"," 2"},
		{": 2"," 2"},
		{":3"," 3"},
		{": 3"," 3"},
		{":4"," 4"},
		{": 4"," 4"},
		{":5"," 5"},
		{": 5"," 5"},
		{".0"," POINT 0"},
		{".1"," POINT 1"},
		{".2"," POINT 3"},
		{".4"," POINT 4"},
		{".5"," POINT 5"},
		{".6"," POINT 6"},
		{".7"," POINT 7"},
		{".8"," POINT 8"},
		{".9"," POINT 9"},
		{" Dr. "," Dr "},
		{" DR. "," Dr "},
		{" dr. "," Dr "},
		{" dr.W"," Dr W"},
		{" dr.w"," Dr W"},
		{" Dr.w"," Dr W"},
		{" Dr.W"," Dr W"},
		{" Dr . "," Dr "},
		{" DR . "," Dr "},
		{" dr . "," Dr "},
		{" MR. "," Mr "},
		{" Mr. "," Mr "},
		{" Mrs. "," Mrs "},
		{" MRS. "," MRS "},
		{" mr. "," Mr "},
		{" St. "," St "},
		{" ST. "," St "},
		{" st. "," St "},
		{" www."," www dot "},
		{" WWW."," WWW dot "},
		{" botspot."," botspot dot "},
		{" BOTSPOT."," botspot dot "},
		{" BotSpot."," botspot dot "},
		{" amused.com"," amused dot com "},
		{" whatis."," whatis dot "},
		{" AMUSED.COM"," amused dot com "},
		{" Amused.com"," amused dot com "},
		{".com "," dot com "},
		{".COM "," dot com "},
		{".net "," dot net "},
		{".NET "," dot NET "},
		{".org "," dot org "},
		{".ORG "," dot ORG "},
		{".edu "," dot edu "},
		{".EDU "," DOT EDU "},
		{".UK "," DOT UK "},
		{".uk "," dot uk "},
		{".jp "," dot jp "},
		{".JP "," DOT JP "},
		{".au "," dot au "},
		{".AU "," dot au "},
		{".CO "," dot CO "},
		{".co "," dot co "},
		{".ac "," dot ac "},
		{" O.K. "," OK "},
		{" o. k. "," OK "},
		{" L.L. "," L L "},
		{" o.k. "," OK "},
		{" P.S. "," PS "},
		{" p.s. "," PS "},
		{" P.S. "," PS "},
		{" ALICEBOT "," ALICE "},
		{" A L I C E "," ALICE "},
		{" A.L.I.C.E. "," ALICE "},
		{" A.L.I.C.E "," ALICE "},
		{" I.C.E "," I C E "},
		{" a.l.i.c.e. "," ALICE "},
		{" a.l.i.c.e "," ALICE "},
		{" E L V I S "," ELVIS "},
		{" E.L.V.I.S. "," ELVIS "},
		{" E.L.V.I.S "," ELVIS "},
		{" V.I.S "," V I S "},
		{" e.l.v.i.s. "," ELVIS "},
		{" e.l.v.i.s "," ELVIS "},
		{" H A L "," HAL "},
		{" H.A.L. "," HAL "},
		{" h.a.l. "," HAL "},
		{" H.a.l. "," HAL "},
		{" U S A "," USA "},
		{" U. S. A. "," USA "},
		{" U.S.A. "," USA "},
		{" u.s.a. "," USA "},
		{" U.S. "," USA "},
		{" Ph.D. "," PhD "},
		{" A."," A "},  // period_subst
		{" L."," L "},
		{" I."," I "},
		{" C."," C "},
		{" E."," E "},
		{" B."," B "},
		{" C."," C "},
		{" D."," D "},
		{" E."," E "},
		{" F."," F "},
		{" G."," G "},
		{" H."," H "},
		{" I."," I "},
		{" J."," J "},
		{" K."," K "},
		{" L."," L "},
		{" M."," M "},
		{" N."," N "},
		{" P."," P "},
		{" O."," O "},
		{" Q."," Q "},
		{" R."," R "},
		{" S."," S "},
		{" T."," T "},
		{" U."," U "},
		{" V."," V "},
		{" X."," X "},
		{" Y."," Y "},
		{" W."," W "},
		{" Z."," Z "},
		{" a."," A "},
		{" b."," B "},
		{" c."," C "},
		{" d."," D "},
		{" e."," E "},
		{" f."," F "},
		{" g."," G "},
		{" h."," H "},
		{" i."," I "},
		{" j."," J "},
		{" k."," K "},
		{" l."," L "},
		{" m."," M "},
		{" n."," N "},
		{" o."," O "},
		{" p."," P "},
		{" q."," Q "},
		{" r."," R "},
		{" s."," S "},
		{" t."," T "},
		{" u."," U "},
		{" v."," V "},
		{" w."," W "},  // period_subst
		{" x."," X "},
		{" y."," Y "},
		{" z."," Z "},
		{".jar"," JAR"},
		{".JAR"," JAR"},
		{".zip"," ZIP"},
		{".ZIP"," ZIP"},
		{", but ",".  "},
		{", and ",".  "},
		{", And ",".  "},
		{", AND ",".  "},
		{", But ",".  "},
		{", But ",".  "},
		{", BUT ",".  "},
		{",but ",".  "},
		{",and ",".  "},
		{",And ",".  "},
		{",AND ",".  "},
		{",But ",".  "},
		{",But ",".  "},
		{",BUT ",".  "},
		{"  but ",".  "},
		{"  and ",".  "},
		{"  And ",".  "},
		{"  AND ",".  "},
		{"  But ",".  "},
		{"  But ",".  "},
		{"  BUT ",".  "},
		{", I ",".  I "},
		{", I ",".  I "},
		{", i ",".  I "},
		{", i ",".  I "},
		{", You ",".  You "},
		{", You ",".  You "},
		{", you ",".  You "},
		{", you ",".  You "},
		{",I ",".  I "},
		{",I ",".  I "},
		{",i ",".  I "},
		{",i ",".  I "},
		{",You ",".  You "},
		{",You ",".  You "},
		{",you ",".  You "},
		{",you ",".  You "},
		{", What ",".  What "},
		{", what ",".  What "},
		{",What ",".  What "},
		{",what ",".  What "},
		{", Do ",".  Do "},
		{", do ",".  Do "},
		{",Do ",".  Do "},
		{",do ",".  Do "}
	}
	; // period_subst
	
	
        /**
         Normalization table, this is aimed to eliminate
         punctuation, correct spelling mistakes, place exactly
         one space between words, expand all common contractions
         and also some abbreviations.
        */
	static String normal_subst[][] = {
		{"=reply",""},
		{"=REPLY",""},
		{"NAME=RESET",""},
		{":-)"," SMILE "},
		{":)"," SMILE "},
		{",)"," SMILE "},
		{";)"," SMILE "},
		{";-)"," SMILE "}, // normal_subst
		{"\"",""},
		{"/"," "},
		{">"," GT "},
		{"<"," LT "},
		{"("," "},
		{")"," "},
		{"`"," "},
		{"."," "},
		{","," "},
		{":"," "},
		{";"," "},
		{"!"," "},
		{"?"," "},
		{"&"," "},
		{"-","-"},
		{"="," "},
		{","," "},
		{"  "," "},
		{" L A "," LA "},
		{" O K "," OK "},
		{" P S "," PS "},
		{" OHH"," OH"},
		{" HEHE"," HE"},
		{" HAHA"," HA"},    
		{" HELLP "," HELP "},
		{" BECUSE "," BECAUSE "},
		{" BELEIVE "," BELIEVE "},
		{" BECASUE "," BECAUSE "},
		{" BECUASE "," BECAUSE "},
		{" BECOUSE "," BECAUSE "},
		{" PRACTICE "," PRACTISE "},
		{" REDUCTIONALISM ", " REDUCTIONISM "},
		{" LOEBNER PRICE ", " LOEBNER PRIZE "},
		{" ITS A "," IT IS A "},
		{" NOI "," YES I "},
		{" FAV "," FAVORITE "},
		{" YESI "," YES I "},
		{" YESIT "," YES IT "},
		{" IAM "," I AM "},
		{" WELLI "," WELL I "},
		{" WELLIT "," WELL IT "},
		{" AMFINE "," AM FINE "},
		{" AMAN "," AM AN "},
		{" AMON "," AM ON "},  // normal_subst
		{" AMNOT "," AM NOT "},
		{" REALY "," REALLY "},
		{" IAMUSING "," I AM USING "},
		{" AMLEAVING "," AM LEAVING "},
		{" YEAH "," YES "},
		{" YEP "," YES "},
		{" YHA "," YES "},
		{" YUO "," YOU "},
		{" WANNA "," WANT TO "},
		{" YOU'D "," YOU WOULD "},
		{" YOU'RE "," YOU ARE "},
		{" YOU RE "," YOU ARE "},
		{" YOU'VE "," YOU HAVE "},
		{" YOU VE "," YOU HAVE "},
		{" YOU'LL "," YOU WILL "},
		{" YOU LL "," YOU WILL "},
		{" YOURE "," YOU ARE "},
		{" DIDNT "," DID NOT "},
		{" DIDN'T "," DID NOT "},
		{" DID'NT "," DID NOT "},
		{" COULDN'T "," COULD NOT "},
		{" COULDN T "," COULD NOT "},
		{" DIDN'T "," DID NOT "},
		{" DIDN T "," DID NOT "},
		{" AIN'T "," IS NOT "},
		{" AIN T "," IS NOT "},
		{" ISN'T "," IS NOT "},
		{" ISN T "," IS NOT "},
		{" ISNT "," IS NOT "},
		{" IT'S "," IT IS "},
		{" IT S "," IT IS "},
		{" ARE'NT "," ARE NOT "},
		{" ARENT "," ARE NOT "},
		{" AREN'T "," ARE NOT "},
		{" AREN T "," ARE NOT "},
		{" ARN T "," ARE NOT "},
		{" WHERE'S "," WHERE IS "},
		{" WHERE S "," WHERE IS "},
		{" HAVEN'T "," HAVE NOT "},
		{" HAVENT "," HAVE NOT "}, // normal_subst
		{" HASN'T "," HAS NOT "},
		{" HASN T "," HAS NOT "}, // normal_subst
		{" WEREN T "," WERE NOT "},
		{" WEREN'T "," WERE NOT "},
		{" WERENT "," WERE NOT "},
		{" CAN'T "," CAN NOT "},
		{" CAN T "," CAN NOT "},
		{" CANT "," CAN NOT "},
		{" CANNOT "," CAN NOT "},
		{" WHOS "," WHO IS "},
		{" HOW'S "," HOW IS "},
		{" HOW S "," HOW IS "},
		{" HOW'D "," HOW DID "},
		{" HOW D "," HOW DID "},
		{" HOWS "," HOW IS "},
		{" WHATS "," WHAT IS "},
		{" NAME'S "," NAME IS "},
		{" WHO'S "," WHO IS "},
		{" WHO S "," WHO IS "},
		{" WHAT'S "," WHAT IS "},
		{" WHAT S "," WHAT IS "},
		{" THAT'S "," THAT IS "},
		{" THERE'S "," THERE IS "},
		{" THERE S "," THERE IS "},
		{" THERES "," THERE IS "},
		{" THATS "," THAT IS "},
		{" WHATS "," WHAT IS "},
		{" DOESN'T "," DOES NOT "},
		{" DOESN T "," DOES NOT "},
		{" DOESNT "," DOES NOT "},
		{" DON'T "," DO NOT "},
		{" DON T "," DO NOT "},
		{" DONT "," DO NOT "},
		{" DO NT "," DO NOT "},
		{" DO'NT "," DO NOT "},
		{" WON'T "," WILL NOT "},
		{" WONT "," WILL NOT "},
		{" WON T "," WILL NOT "},
		{" LET'S "," LET US "}, // normal_subst
		{" THEY'RE "," THEY ARE "},
		{" THEY RE "," THEY ARE "},
		{" WASN'T "," WAS NOT "},
		{" WASN T "," WAS NOT "},
		{" WASNT "," WAS NOT "},
		{" HADN'T "," HAD NOT "},
		{" HADN T "," HAD NOT "},
		{" WOULDN'T "," WOULD NOT "},
		{" WOULDN T "," WOULD NOT "},
		{" WOULDNT "," WOULD NOT "},
		{" SHOULDN'T "," SHOULD NOT "},
		{" SHOULDNT "," SHOULD NOT "},
		{" FAVOURITE "," FAVORITE "},
		{" COLOUR "," COLOR "},
		{" WE'LL "," WE WILL "},
		{" WE LL "," WE WILL "},
		{" HE'LL "," HE WILL "},
		{" HE LL "," HE WILL "},
		{" I'LL "," I WILL "},
		{" ILL "," I WILL "},
		{" IVE "," I HAVE "},
		{" I'VE "," I HAVE "},
		{" I VE "," I HAVE "},
		{" I'D "," I WOULD "},
		{" I'M "," I AM "},
		{" I M "," I AM "},
		{" WE'VE "," WE HAVE "},
		{" WE'RE "," WE ARE "},
		{" SHE'S "," SHE IS "},
		{" SHES "," SHE IS "},
		{" SHE'D "," SHE WOULD "},
		{" SHE D "," SHE WOULD "},
		{" SHED "," SHE WOULD "},
		{" HE'D "," HE WOULD "},
		{" HE D "," HE WOULD "},
		{" HED "," HE WOULD "},
		{" HE'S "," HE IS "},  // normal_subst
		{" WE VE "," WE HAVE "},
		{" WE RE "," WE ARE "},
		{" SHE S "," SHE IS "},
		{" HE S "," HE IS "},
		{" IAMA "," I AM A "},
		{" IAMASKING "," I AM ASKING "},
		{" IAMDOING "," I AM DOING "},
		{" IAMFROM "," I AM FROM "},
		{" IAMIN "," I AM IN "},
		{" IAMOK "," I AM OK "},
		{" IAMSORRY "," I AM SORRY "},
		{" IAMTALKING "," I AM TALKING "},
		{" IAMTIRED "," I AM TIRED "},
		{" DOWN LOAD "," DOWNLOAD "},
		{" REMEBER "," REMEMBER "},
		{" WAHT "," WHAT "},
		{" WALLANCE "," WALLACE "},
		{" YOU R "," YOU ARE "},
		{" U "," YOU "},
		{" UR "," YOUR "},
	/*	{"a","AE"},  //broke during import...
		{"A","AE"},
		{"o","OE"},
		{"O","OE"},
		{"u","UE"},
		{"U","UE"},*/
		{"'"," "}
	}; // normal_subst


        /**
          person substitutions
          That includes person_subst, person2_subst and gender_subst
          A risk to reverse a substitution with another as the scan
          thru the list progresses do exists.
          So all tables has a transformation where the spaces are
          distorted on purpose into #.
          i.e.
              " HE "  ---(transformed)---> " SHE "
              but later
              " SHE " ---(transformed)---> " HE "

              so instead
              " HE "  ---(transformed)---> "#SHE#"
              the transformed token doesn't hit " SHE " later and
              the transformation is not reversed.

          After all transformations has been made a global change
          from "#" into " " MUST be performed.
        */

	static String person2_subst[][] = {
                {" I WAS ","#HE#WAS#"},
                {" I AM ","#HE#IS#"},
                {" I ","#HE#"},
                {" ME ","#HIM#"},
                {" MY ","#HIS#"},
                {" MYSELF ","#HIMSELF#"},
                {" MINE ","#HIS#"},
                {" WITH YOU ","#WITH#ME#"},
                {" TO YOU ","#TO#ME#"},
                {" FOR YOU ","#FOR#ME#"},
                {" ARE YOU ","#AM#I#"},
                {" YOU ","#I#"},
                {" YOUR ","#MY#"},
                {" YOURS ","#MINE#"},
                {" YOURSELF ","#MYSELF#"},
		//German Substitutions, note the comma I added at the end of the previous line when using cut and paste!
                {" ICH WAR ","#ER#WAR#"},
                {" ICH BIN ","#ER#IST#"},
                {" ICH ","#ER#"},
                {" MEIN ","#SEIN#"},
                {" MEINS ", "#SEINS#"},
                {" MIT DIR ","#MIT#MIR#"},
                {" DIR ", "#MIR#"},
                {" FUER DICH ","#FUER#MICH#"},
                {" BIST DU ","#BIN#ICH#"},
                {" DU ","#ICH#"},
                {" DEIN ","#MEIN#"},
                {" DEINS ","#MEINS#"}
		//End of German Substitutions
	}; // person2_subst
	
	static String gender_subst[][] = {
                {" HE ","#SHE#"},
                {" HIM ","#HER#"},
                {" HIS ","#HER#"},
                {" HIMSELF ","#HERSELF#"},
                {" SHE ","#HE#"},
                {" HER ","#HIS#"},
                {" HERSELF ","#HIMSELF#"},
		//German substitutions
                {" ER ","#SIE#"},
                {" IHM ","#IHR#"},
                {" SEIN ","#IHR#"},
                {" IHN ", "#SIE#"}
		//End of German Substitutions
	}; // gender_subst
	
	static String person_subst[][] = {
		//phase I;
                {" WITH YOU ","#WITH#<name/>#"},
                {" TO YOU ","#TO#<name/>#"},
                {" OF YOU ","#OF#<name/>#"},
                {" FOR YOU ","#FOR#<name/>#"},
                {" GIVE YOU ","#GIVE#<name/>#"},
                {" GIVING YOU ","#GIVING#<name/>#"},
                {" GAVE YOU ","#GAVE#<name/>#"},
                {" MAKE YOU ","#MAKE#<name/>#"},
                {" MADE YOU ","#MADE#<name/>#"},
                {" TAKE YOU ","#TAKE#<name/>#"},
                {" SAVE YOU ","#SAVE#<name/>#"},
                {" TELL YOU ","#TELL#<name/>#"},
                {" TELLING YOU ","#TELLING#<name/>#"},
                {" TOLD YOU ","#TOLD#<name/>#"},
                {" ARE YOU ","#IS#<name/>#"},
                {" YOU ARE ","#<name/>#IS#"},
                {" YOU ","#<name/>#"},
                {" YOUR ","#<name/>S#"},
                {" YOURS ","#<name/>SS#"},
                {" YOURSELF ","#<name/>SSELF#"},
                {" I WAS ","#YOU#WERE#"},
                {" I AM ","#YOU#ARE#"},
                {" I ","#YOU#"},
                {" ME ","#YOU#"},
                {" MY ","#YOUR#"},
                {" MYSELF ","#YOURSELF#"},
                {" MINE ","#YOURS#"},
		// phase II:
                {" TELL ME ","#TELL#YOU#"},
                {" TOLD ME ","#TOLD#YOU#"},
                {" TELLING ME ","#TELLING#YOU#"},
                {" GIVE ME ","#GIVE#YOU#"},
                {" MAKE ME ","#MAKE#YOU#"},
                {" TAKE ME ","#TAKE#YOU#"},
		
                {" WITH <name/> ","#WITH#ME#"},
                {" IS <name/> ","#AM#I#"},
                {" TO <name/> ","#TO#ME#"},
                {" FOR <name/> ","#FOR#ME#"},
                {" OF <name/> ","#OF#ME#"},
                {" GIVE <name/> ","#GIVE#ME#"},
                {" GAVE <name/> ","#GAVE#ME#"},
                {" MAKE <name/> ","#MAKE#ME#"},
                {" TAKE <name/> ","#TAKE#ME#"},
                {" SAVE <name/> ","#SAVE#ME#"},
                {" MADE <name/> ","#MADE#ME#"},
                {" TELL <name/> ","#TELL#ME#"},
                {" TOLD <name/> ","#TOLD#ME#"},
                {" <name/> IS ","#I#AM#"},
                {" <name/> WAS ","#I#WAS#"},
                {" <name/> ","#I#"},
                {" <name/>S ","#MY#"},
                {" <name/>SS ","#MINE#"} ,
                {" <name/>SSELF ","#MYSELF#"},
                {" I ARE ","#I#AM#"},
                {" TO I ","#TO#ME#"},
                {" OF I ","#OF#ME#"},
                {" WITH I ","#WITH#ME#"}
	}; // person_subst
	
	static String pretty_subst[][] = {
		//phase I;
		{" i "," I "},
		{" s ","'s "}
	}; // pretty_subst
	
	
        /**
          Main method is for testing purposes only
        */
	public static void main (String argv[]) {
		
		// normalize puts the input into the "normal form"
	}

        /**
         The replace() method is a straightforward
         generalization of the String.replace() method
         where the arguments are strings instead of characters.
	
	length = 5
	startindex = 1
	
	XABCDEXXXXXXXXXXXXXXXXXXXXXXXX
	^length+startindex
	^   ^
	length+startindex-1
	startIndex
	^ startindex-1
	
	
	"<sr/>"
	^startindex=1
	
	*/
	
	public static String replace(String substx, String substy, String norm) {
		int index = -1;
		int startidx=0; // SUH 19.5.00 to avoid recursion, i.e. norm = replace("a","aa",norm);
		if (substx.compareTo(substy)==0) return norm;
		try {
			if (substx.length() > 0) {
				int len = substx.length();
				while ((index = norm.indexOf(substx,startidx)) >= 0) {// SUH 19.5.00 to avoid recursion, i.e. norm = replace("a","aa",norm);
					if (index > norm.length()) {
						System.out.println("'"+substx+"' '"+substy+"' '"+norm+"'"+index);
						return (norm);
					}
					String head = norm.substring(0, index);
					String tail = 
						(index+len < norm.length()) ? norm.substring(index + len) : "";
					norm = head + substy + tail;
					startidx = head.length()+substy.length()+1;// SUH 19.5.00 to avoid recursion, i.e. norm = replace("a","aa",norm);
				}
			}
		} catch (Exception e) {
			System.out.println("Substituter Exception "+e); 
			System.out.println("'"+substx+"' '"+substy+"' '"+norm+"'"+index);
		}
		return norm;
	}
	
        /**the overloaded replace() method
           replaces an entire array of substitutions
           in the input string
        */
	public static String replace(String[][] subst, String input) {
		input = input.trim();
		input = " "+input+" ";
		for (int i = 0; i < subst.length; i++)
			input = replace(subst[i][0], subst[i][1], input);
		input = input.trim();
		return input;
	} // replace (2 args)
	
        /** the "cleanup" in cleanup_http means converting
            GET method character substitutions back to
            a human-readable form
        */
	public static String cleanup_http(String arg) {
		//  System.out.println("BEFORE: "+arg);
		arg = arg.replace('+',' ');
		if (arg.indexOf("HTTP/") >= 0) 
			arg = arg.substring(0, arg.indexOf("HTTP/"));
		arg = replace(http_subst, arg);
		arg = replace(http_subst, arg); // need two passes
		//  System.out.println("AFTER: "+arg);
		return(arg);
	} // cleanup_http

        /**
          format_http is intended to perform reverse text to HTML
          substitutions.
        */
	public static String format_http(String arg) {
		arg = replace(http_reverse_subst, arg);
		return(arg);
	} // cleanup_http

        /**
         supress_html suppress HTML code from the string
        */
	public static String suppress_html(String line) {
		line = replace("&nbsp;"," ",line);
		line = replace("<BR>"," ",line);
		line = replace("<br>"," ",line);
		line = replace("<br/>"," ",line);
		line = replace("<BR/>"," ",line);
		line = replace("<br />"," ",line);
		line = replace("<BR />"," ",line);
		line = replace("<TR>","\n",line);
		line = replace("<tr>","\n",line);
		line = replace("</TR>"," ",line);
		line = replace("</tr>"," ",line);
		line = replace("<TD>"," ",line);
		line = replace("<td>"," ",line);
		line = replace("</TD>"," ",line);
		line = replace("</td>"," ",line);
		line = replace("</li>","\n",line);
		line = replace("</LI>","\n",line);
		line = replace("\""," ",line);
		line = replace("  "," ",line);
		while (line.indexOf('<') >= 0) {
			//       System.out.println("Line = "+line);
			int start = line.indexOf('<');
			int end = start;
			if (line.indexOf('>') > end) end = line.indexOf('>');
			String tail;
			if (end+1 >= line.length()) tail = ""; // when ">" is last char
			else tail = line.substring(end+1); // ordinary case
			line = line.substring(0,start) + tail;
		}
		// System.out.println("Return Line = "+line);
		line = replace("\n\n","\n",line);
		line = replace("  "," ",line);
		line = replace("&gt;",">",line);
		line = replace("&lt;","<",line);
		while (line.startsWith("\n")) line = line.substring(1, line.length()); 
		return line;
	}  // suppress_html
	
        /** normalize used to transform the input in a common
            way to allow the pattern matching
        */
	public static String normalize(String input) {
		if (input != null) {
			input = input.toUpperCase();
			return(replace(normal_subst, input));
		} else {
			return "*";
		}
	} // normalize
	
        /**Remove unsightly periods
           from abbreviations etc.
        */
	public static String deperiodize(String input) {
		// two passes ensures most strange combinations detected:
		input = replace(period_subst, input); 
		return(input);
	} // deperiodize

//Add 4.1.1 b1 PEC 09-2001
        /**
         person
         This function produces the person shift from 1st to 3rd.
         The actual shift is controlled by the array named person_subst[][]
         This variant might return AIML as part of the substitution and
         it will be resolved by the PersonProcessor receiving it
        */

        public static String person(String input) {

         /*
           No point to continue if empty
         */
          if(input.equals("")) {
            return "";
          }
         String response = replace(person_subst,input.toUpperCase()).toLowerCase();
         response = replace("#"," ",response);
         response = response.trim();
         response = pretty(response);
         return response;

        }

        /**
         person2
         This function produces the person2 shift
         The actual shift is controlled by the array named person_subst2[][]
         This variant might return AIML as part of the substitution and
         it will be resolved by the Person2Processor receiving it
        */

        public static String person2(String input) {

         /*
           No point to continue if empty
         */
          if(input.equals("")) {
            return "";
          }
         String response = replace(person2_subst,input.toUpperCase()).toLowerCase();
         response = replace("#"," ",response);
         response = response.trim();
         response = pretty(response);
         return response;

        }

        /**
         gender
         This function produces the gender shift
         The actual shift is controlled by the array named gender_subst2[][]
         This variant might return AIML as part of the substitution and
         it will be resolved by the GenderProcessor receiving it
        */
	public static String gender(String input) {
         /*
           No point to continue if empty
         */
          if(input.equals("")) {
            return "";
          }

         //System.out.println("*** GENDER SHIFT: FROM("+input+") ***");
         String response = replace(gender_subst,input.toUpperCase()).toLowerCase();
         //System.out.println("*** GENDER SHIFT: TO("+response+") ***");
         response = replace("#"," ",response);
         //System.out.println("*** GENDER SHIFT: TOB("+response+") ***");
         response = response.trim();
         response = pretty(response);
         return response;
	} // gender

//End of Add


        /** pretty "Pretty-fy" the output
        */
	public static String pretty(String reply) {
		reply = reply.toLowerCase();
		return(replace(pretty_subst, reply));
	} //pretty
	
	public static String capitalize(String reply) {
		String sentence = reply;
		sentence = sentence.trim();
		if (sentence.length() > 1) {
			sentence = sentence.substring(0,1).toUpperCase() +
                                   sentence.substring(1, sentence.length());
		}
		return sentence;
	}

        //Fix (04.0.3 b1) PEC 09-2001 sentance------VVVVVVVV
        public static String capitalizeWords(String sentence) {
		StringBuffer buffer = new StringBuffer();
               //Fix (04.0.3 b1) PEC 09-2001 sentance----VVVVVVVV
                StringTokenizer st = new StringTokenizer(sentence);
		int wordcount = 0;
		while (st.hasMoreTokens()) {
			wordcount++;
			if (wordcount > 1) buffer.append(" ");
			buffer.append(capitalize(st.nextToken()));
		}
		return buffer.toString();
	}
	
	
	public static String formal(String reply) {
		StringTokenizer st = new StringTokenizer(reply, " ");
		String out = "";  
		try {
			int n = st.countTokens();
			for (int i = 0; i < n; i++) {
				String sent = st.nextToken();
				sent = sent.substring(0,1).toUpperCase() +
					sent.substring(1).toLowerCase();
				out = out + sent + " ";
			}
		} catch (Exception e) {System.out.println("FORMAL: "+e);}
		out = out.trim();
		return out;
	}

        /** by Andrew:
        */
	public static String wrapText(String memo, int columns) {
		
		// the system newline character
		char newline = '\n';
		
		// the new output
		String tempLine = "";
		String tempMemo = "";
		
		// the character count
		int cc = 0;
		
		// split up into lines
		StringTokenizer lines = new StringTokenizer(memo, String.valueOf(newline));
		int noOfLines = lines.countTokens();
		for (int i = 0; i < noOfLines; i++) {
			String line = lines.nextToken();
			
			// split up into words
			StringTokenizer words = new StringTokenizer(line, " ");
			int noOfWords = words.countTokens();
			for (int j = 0;  j < noOfWords; j++) {
				String word = words.nextToken();
				
				// the process is yours
				if ((tempLine.length() + word.length() + 1) <= columns) { 
					// the 1 is foir the space
					if (tempLine.length() != 0)
						tempLine = tempLine + " " + word;
					else
						tempLine = "" + word;
				} else {
					tempMemo = tempMemo + newline + tempLine;
					tempLine = "" + word;
				}
			}
			
			// this adds the last line if it did not wrap
			if (tempLine.length() != 0) {
				tempMemo = tempMemo + newline + tempLine;
				tempLine = "";
			}
		}
		while (tempMemo.length() > 1 && tempMemo.startsWith("\n")) 
			tempMemo = tempMemo.substring(1, tempMemo.length());
		return tempMemo;
		
	} // wrapText()
	
	public static String stripHTML(String line) {
		StringBuffer sb = new StringBuffer(line);
		String out = "";
		
		for (int i=0; i < sb.length()-1; i++) {
			if (sb.charAt(i) == '<') {
				// Most tags
				if (sb.charAt(i+1) == '/'
					|| (sb.charAt(i+1) >= 'a' && sb.charAt(i+1) <= 'z')
					|| (sb.charAt(i+1) >= 'A' && sb.charAt(i+1) <= 'Z')) {
					for (int j=i+1; j < sb.length(); j++) {
						if (sb.charAt(j) == '>') {
							sb = sb.replace(i,j+1,"");
							i--;
							break;
						}
					}
				} else if (sb.charAt(i+1) == '!') { // Comments
					for (int j=i+1; j < sb.length(); j++) {
						if (sb.charAt(j) == '>'
							&& sb.charAt(j-1) == '-'
							&& sb.charAt(j-2) == '-') {
							sb = sb.replace(i,j+1,"");
							i--;
							break;
						}
					}
				}
			}
		}
		out = sb.toString();
		return out;
		
	}  // stripHTML
	
}// end of class Substituter

