/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

/*
    Code cleanup (4.1.3 [00] - October 2001, Noel Bush)
    - formatting cleanup
    - general grammar fixes
    - complete javadoc (except for some straightforward implementations of Multiplexor methods)
    - made all imports explicit
    - removed unused methods
    - removed cleanValue (it effectively duplicated setValue/setPredicate)
    - removed unused class variables (interpreter, asize)
    - removed unnecessary temporary variables
    - removed useless passing of depth parameter all over the place
    - moved database-independent methods to the new AbstractClassifier
    - created backward-compatible methods for the old Classifier methods
    - removed arbitrary normalizations of predicates called "name" and "date"
    - omitted mysterious checks for "+my+" and "+your+" in predicate names
    - created class field botName to store bot name instead of repeatedly looking it up
    - eliminated useless conjunction of multiple predicate values from database
    - moved out random number generator (was only used in random)
    - removed "implements Serializable"
*/

/*
    Further fixes and optimizations (4.1.3 [01] - November 2001, Noel Bush)
    - removed calls to Substituter.deperiodize and Substituter.suppress_html
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush
    - changed *Predicate*() methods to *PredicateValue*()
    - added getInternalResponse(), getReply() and getReplies()
    - inserted default methods for createUser(), checkUser() and changePassword()
      (although these are not supported, and probably not worth supporting
      given the weird database structure)
    - changed some server property names
    - fixed setPredicateValue so it does not return encoded value (rather original)
    - fixed getPredicateValue so it does not return encoded value (rather decoded)
*/

package org.alicebot.server.core;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.parser.AIMLParser;
import org.alicebot.server.core.responder.Responder;
import org.alicebot.server.core.util.InputNormalizer;
import org.alicebot.server.core.util.Substituter;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.sql.pool.DbAccess;
import org.alicebot.server.sql.pool.DbAccessRefsPoolMgr;

/**
 *  <p>
 *  A database-oriented {@link Multiplexor}. Uses a database for storage
 *  and retrieval of predicates.
 *  </p>
 *  <p>
 *  The approach currently used has some disadvantages; for instance,
 *  two pairs of &lt;that/&gt; and &lt;input/&gt; are stored in /
 *  retrieved from the database at every turn! Watch out.
 *  </p>
 *  <p>
 *  Since 4.1.3, this implements {@link Multiplexor}, with the goal
 *  of writing another implementation that is less database-dependent
 *  (or completely indepdendent of a database).
 *  </p>
 *
 *  @author Richard Wallace, Jon Baer
 *  @author Thomas Ringate/Pedro Colla
 *  @version 4.1.3
 */
public class Classifier extends AbstractClassifier
{
    /** A manager for database access. */
    private static DbAccessRefsPoolMgr dbManager;

    /** The maximum length for a predicate value (should be configurable). */
    private static final int MAX_PREDICATE_VALUE_LENGTH = 1024;

    /** The bot name (this will go away for multi-bot support) */
    private static final String botName = Globals.getBotName();


    public String setPredicateValue(String name, String value, String userid)
    {
        return StaticSelf.setPredicateValue(name, value, userid);
    }


    public String getPredicateValue(String name, String userid)
    {
        return StaticSelf.getPredicateValue(name, userid);
    }


    /**
     *  Always returns true (Classifier
     *  does not support authentication).
     */
    public boolean createUser(String userid, String password, String secretKey)
    {
        return true;
    }


    /**
     *  Always returns true (Classifier
     *  does not support authentication).
     */
    public boolean checkUser(String userid, String password, String secretKey)
    {
        return true;
    }


    /**
     *  Always returns true (Classifier
     *  does not support authentication).
     */
    public boolean changePassword(String userid, String password, String secretKey)
    {
        return true;
    }


    /**
     *  Loads the database properties from the server configuration.
     */
    public void initialize()
    {
        Log.devinfo("Classifier: Opening database pool.", new String[] {Log.DATABASE, Log.STARTUP});
        dbManager = new DbAccessRefsPoolMgr(Globals.getProperty("programd.database.driver", ""),
                                            Globals.getProperty("programd.database.url", ""),
                                            Globals.getProperty("programd.database.user", ""),
                                            Globals.getProperty("programd.database.password", ""));
        Log.devinfo("Classifier: Populating database pool.", new String[] {Log.DATABASE, Log.STARTUP});
        dbManager.populate(Integer.parseInt(Globals.getProperty("programd.database.connections", "25")));
    }


    /**
     *  Contains static synchronized versions of
     *  {@link Multiplexor} methods, to ensure thread-safety.
     */
    public static class StaticSelf
    {
        /**
         *  <p>
         *  Stores a predicate in a database.
         *  </p>
         *  <p>
         *  The structure of the database assumed here is non-optimal.
         *  Future implementations will provide a better structure.
         *  </p>
         *
         *  @see Multiplexor#setPredicate(String, String, String)
         */
        public static synchronized String setPredicateValue(String name, String value, String userid)
        {
            if (name.equals(THAT))
            {
                // Get the last sentence for <that/> storage.
                ArrayList sentences = InputNormalizer.sentenceSplit(value);
                value = (String)sentences.get(sentences.size() - 1);

                if (value.length() > MAX_PREDICATE_VALUE_LENGTH)
                {
                    value = value.substring(0, MAX_PREDICATE_VALUE_LENGTH - 1);
                }
                else if (value.length() <= 0)
                {
                    value = THAT;
                }
            }
            
            // URLEncoder conveniently escapes things that would otherwise be problematic.
            String encodedValue = URLEncoder.encode(value.trim());

            try
            {
                DbAccess dbaRef = dbManager.takeDbaRef();
                ResultSet records = dbaRef.executeQuery("select value from properties where ip = '" + userid + "' and bot = '" + botName + "' and property = '" + name + "'");
                int count = 0;
                while (records.next())
                {
                    count++;
                }
                if (count > 0)
                {
                    dbaRef.executeUpdate("update properties set value = '" + value + "' where ip = '" + userid + "' and property = '" + name + "'");
                }
                else
                {
                    dbaRef.executeUpdate("insert into properties values ('" + botName + "','" + userid + "', null, null, null, null, '" + name + "','" + encodedValue + "')");
                }
                records.close();
                dbManager.returnDbaRef(dbaRef);
            }
            catch (Exception e) {
                Log.log("Database error: " + e, Log.ERROR);
            }
            return value;
        }


        /**
         *  <p>
         *  Gets the value of a predicate from a database.
         *  </p>
         *  <p>
         *  The structure of the database assumed here is non-optimal.
         *  Future versions will replace this with a better structure.
         *  </p>
         *
         *  @see Multiplexor#getPredicate(String, String)
         */
        public static synchronized String getPredicateValue(String name, String userid)
        {
            String result = EMPTY_STRING;
            boolean foundIt = false;
            try
            {
                DbAccess dbaRef = dbManager.takeDbaRef();
                ResultSet records = dbaRef.executeQuery("select value from properties where ip = '" + userid + "' and bot = '" + botName + "' and property = '" + name + "'");
                int returnCount = 0;
                while (records.next())
                {
                    foundIt = true;
                    returnCount++;
                    result = URLDecoder.decode(records.getString(VALUE).trim());
                }
                records.close();
                dbManager.returnDbaRef(dbaRef);
            }
            catch (Exception e)
            {
                Log.log("Database error: " + e, Log.ERROR);
                result = Globals.getBotPredicateEmptyDefault();
                return result;
            }
            if (foundIt == false)
            {
                result = Globals.getBotPredicateEmptyDefault();
                return result;
            }
            return URLDecoder.decode(result);
        }
    }


    /**
     *  A backward-compatibility method for {@link #getResponse(String, String)}.
     *
     *  @deprected since 4.1.3
     *  @see {@link #getResponse(String, String)}
     */
    public static String doRespond(String input, String userid, int depth)
    {
        return AbstractClassifier.StaticSelf.getResponse(input, userid);
    }

    
    /**
     *  Backward-compatibility method for
     *  {@link setPredicate(String, String, String)}.
     *
     *  @deprecated since 4.1.3
     *  @see {@link setPredicateValue(String, String, String)}
     */
    public static synchronized void setValue(String name, String userid, String value)
    {
        StaticSelf.setPredicateValue(name, value, userid);
    }
    

    /**
     *  Backward-compatibility method for
     *  {@link getPredicate(String, String)}.
     *
     *  @deprecated since 4.1.3
     *  @see {@link getPredicate(String, String)}
     */    
    public static String getValue(String name, String userid)
    {
        return StaticSelf.getPredicateValue(name, userid);
    }


    /**
     *  Double-backward-compatibility method. It's unclear what
     *  <code>conjunction</code> was supposed to be used for,
     *  but rest assured this method will make a hasty departure.
     *
     *  @deprecated since 4.1.3
     *  @see {@link getPredicateValue(String, String)}
     */
    public static synchronized String getValue(String name, String userid, String conjunction)
    {
        return StaticSelf.getPredicateValue(name, userid);
    }
    

    /**
     *  Backward-compatibility method for
     *  {@link setPredicateValue(String, int, String, String)}.
     *
     *  @deprecated since 4.1.3
     *  @see setPredicate(String, int, String, String)
     */
    public static String setValueIndex(String name, int index, String userid, String value)
    {
        return AbstractClassifier.StaticSelf.setPredicateValue(name, index, value, userid);
    }


    /**
     *  Backward-compatibility method for
     *  {@link #pushPredicate(String, String, String)}.
     *
     *  @deprecated since 4.1.3
     *  @see #pushPredicateValue(String, String, String)
     */
    public static String pushValueIndex(String name, String userid, String value)
    {
        return AbstractClassifier.StaticSelf.pushPredicateValue(name, value, userid);
    }
        

    /**
     *  Backward-compatibility method for
     *  {@link #getResponse(String, String)}.
     *
     *  @deprecated since 4.1.3
     *  @see #getResponse(String, String)
     */
    public static String doRespond(String input, String userid)
    {
        return AbstractClassifier.StaticSelf.getResponse(input, userid);
    }
    

    /**
     *  Backward-compatibility method for
     *  {@link #getResponse(String, String, String)}.
     *
     *  @deprecated since 4.1.3
     *  @see #getResponse(String, String, String)
     */
    public static synchronized String doResponse(String input, String userid, Responder robot)
    {
        return AbstractClassifier.StaticSelf.getResponse(input, userid, robot);
    }
} 
