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
    More fixes (4.1.3 [02] - November 2001, Noel Bush
    - changed *Predicate*() methods to *PredicateValue*()
    - added getInternalResponse(), getReply() and getReplies()
    - changed a server property name
    - introduced use of NoSuchPredicateException
    - added access methods for createUser(), checkUser() and changePassword()
*/

package org.alicebot.server.core;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.responder.Responder;

/**
 *  <p>
 *  Implements access to a {@link Multiplexor} of
 *  configurable type.  Uses a variant of the Singleton pattern.
 *  </p>
 *  <p>
 *  This is used to allow all clients of a <code>Multiplexor</code>
 *  to simply use method calls on <code>ActiveMultiplexor</code>,
 *  without having to care about which kind of <code>Multiplexor</code>
 *  is actually in use.
 *  </p>
 *
 *  @since 4.1.3
 *
 *  @author Noel Bush
 */
public class ActiveMultiplexor implements Multiplexor
{

    /** The {@link Multiplexor} managed by the instance of this class. */
    private static Multiplexor multiplexor;

    /** The private field that (partially) ensures <code>ActiveMultiplexor</code> is a singleton. */
    private static final ActiveMultiplexor myself = new ActiveMultiplexor(Globals.getProperty("programd.multiplexor", "org.alicebot.server.core.Classifier"));


    /**
     *  Private constructor that initializes the <code>ActiveMultiplexor</code>
     *  with an implementation of {@link Multiplexor}.
     *
     *  @param className    the name of the subclass of {@link Multiplexor} that should be used
     */
    private ActiveMultiplexor(String className)
    {
        try
        {
            multiplexor = (Multiplexor)Class.forName(className).newInstance();
        }
        catch (Exception e)
        {
            Log.devfail(e.getMessage(), Log.ERROR);
        }
    }


    /**
     *  Prohibits cloning this class.
     */
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }


    public static class StaticSelf
    {
        public static void initialize()
        {
            multiplexor.initialize();
        }


        public static synchronized String getResponse(String input, String userid)
        {
            return multiplexor.getResponse(input, userid);
        }


        public static synchronized String getResponse(String input, String userid, Responder robot)
        {
            return multiplexor.getResponse(input, userid, robot);
        }


        public static synchronized String getInternalResponse(String input, String userid)
        {
            return multiplexor.getInternalResponse(input, userid);
        }


        public static synchronized String setPredicateValue(String name, String value, String userid)
        {
            return multiplexor.setPredicateValue(name, value, userid);
        }


        public static synchronized String setPredicateValue(String name, int index, String value, String userid)
        {
            return multiplexor.setPredicateValue(name, index, value, userid);
        }


        public static synchronized String pushPredicateValue(String name, String value, String userid)
        {
            return multiplexor.pushPredicateValue(name, value, userid);
        }


        public static synchronized String getPredicateValue(String name, String userid) throws NoSuchPredicateException
        {
            return multiplexor.getPredicateValue(name, userid);
        }


        public static synchronized String getPredicateValue(String name, int index, String userid) throws NoSuchPredicateException
        {
            return multiplexor.getPredicateValue(name, index, userid);
        }


        public static synchronized boolean createUser(String userid, String password, String secretKey)
        {
            return multiplexor.createUser(userid, password, secretKey);
        }


        public static synchronized boolean checkUser(String userid, String password, String secretKey)
        {
            return multiplexor.checkUser(userid, password, secretKey);
        }


        public static synchronized boolean changePassword(String userid, String password, String secretKey)
        {
            return multiplexor.changePassword(userid, password, secretKey);
        }
    }


    /**
     *  <p>
     *  Defers to the corresponding method in the
     *  {@link #multiplexor} managed by this class.
     *  </p>
     *  <p>
     *  This does <i>not</i> check whether the
     *  {@link #multiplexor} is initialized!
     *  </p>
     *
     *  @since 4.1.3
     */
    public void initialize()
    {
        multiplexor.initialize();
    }


    /**
     *  <p>
     *  Defers to the corresponding method in the
     *  {@link #multiplexor} managed by this class.
     *  </p>
     *  <p>
     *  This does <i>not</i> check whether the
     *  {@link #multiplexor} is initialized!
     *  </p>
     *
     *  @since 4.1.3
     *
     *  @param input    the input
     *  @param userid   the user identifier
     *
     *  @return the response
     */
    public String getResponse(String input, String userid)
    {
        return multiplexor.getResponse(input, userid);
    }


    /**
     *  <p>
     *  Defers to the corresponding method in the
     *  {@link #multiplexor} managed by this class.
     *  </p>
     *  <p>
     *  This does <i>not</i> check whether the
     *  {@link #multiplexor} is initialized!
     *  </p>
     *
     *  @since 4.1.3
     *
     *  @param input    the input
     *  @param userid   the user identifier
     *  @param robot    the robot who should receive the response
     *
     *  @return the response
     */
    public String getResponse(String input, String userid, Responder robot)
    {
        return multiplexor.getResponse(input, userid, robot);
    }


    /**
     *  <p>
     *  Defers to the corresponding method in the
     *  {@link #multiplexor} managed by this class.
     *  </p>
     *  <p>
     *  This does <i>not</i> check whether the
     *  {@link #multiplexor} is initialized!
     *  </p>
     *
     *  @since 4.1.3
     *
     *  @param input    the input
     *  @param userid   the user identifier
     *
     *  @return the response
     */
    public String getInternalResponse(String input, String userid)
    {
        return multiplexor.getInternalResponse(input, userid);
    }


    /**
     *  <p>
     *  Defers to the corresponding method in the
     *  {@link #multiplexor} managed by this class.
     *  </p>
     *  <p>
     *  This does <i>not</i> check whether the
     *  {@link #multiplexor} is initialized!
     *  </p>
     *
     *  @since 4.1.3
     *
     *  @param name         predicate name
     *  @param value        value to assign to the <code>predicate</code>
     *  @param userid       user identifier
     *
     *  @return the new value of the predicate, or its name (if it is return-value-when-set)
     */
    public String setPredicateValue(String name, String value, String userid)
    {
        return multiplexor.setPredicateValue(name, value, userid);
    }


    /**
     *  <p>
     *  Defers to the corresponding method in the
     *  {@link #multiplexor} managed by this class.
     *  </p>
     *  <p>
     *  This does <i>not</i> check whether the
     *  {@link #multiplexor} is initialized!
     *  </p>
     *
     *  @since 4.1.3
     *
     *  @param name         predicate name
     *  @param index        index of the <code>predicate</code> value to set
     *  @param value        value to assign to the <code>predicate</code> value at <code>index</code>
     *  @param userid       user identifier
     *
     *  @return the new value of the predicate, or its name (if it is return-value-when-set)
     */
    public String setPredicateValue(String name, int index, String value, String userid)
    {
        return multiplexor.setPredicateValue(name, index, value, userid);
    }


    /**
     *  <p>
     *  Defers to the corresponding method in the
     *  {@link #multiplexor} managed by this class.
     *  </p>
     *  <p>
     *  This does <i>not</i> check whether the
     *  {@link #multiplexor} is initialized!
     *  </p>
     *
     *  @since 4.1.3
     *
     *  @param predicate    name of the <code>predicate</code> to set
     *  @param value        value to push onto the stack
     *  @param userid       user identifier
     */
    public String pushPredicateValue(String name, String value, String userid)
    {
        return multiplexor.pushPredicateValue(name, value, userid);
    }


    /**
     *  <p>
     *  Defers to the corresponding method in the
     *  {@link #multiplexor} managed by this class.
     *  </p>
     *  <p>
     *  This does <i>not</i> check whether the
     *  {@link #multiplexor} is initialized!
     *  </p>
     *
     *  @since 4.1.3
     *
     *  @param name         predicate name
     *  @param userid       user identifier
     *
     *  @return value of the <code>predicate</code>, or an empty string if not set / no value
     *
     *  @throws NoSuchPredicateException if there is no predicate with the given name
     */
    public String getPredicateValue(String name, String userid) throws NoSuchPredicateException
    {
        return multiplexor.getPredicateValue(name, userid);
    }


    /**
     *  <p>
     *  Defers to the corresponding method in the
     *  {@link #multiplexor} managed by this class.
     *  </p>
     *  <p>
     *  This does <i>not</i> check whether the
     *  {@link #multiplexor} is initialized!
     *  </p>
     *
     *  @since 4.1.3
     *
     *  @param name         predicate name
     *  @param index        index of the <code>predicate</code> value desired
     *  @param userid       user identifier
     *
     *  @return value of the <code>predicate</code> at <code>index</code>
     *
     *  @throws NoSuchPredicateException if there is no predicate with the given name
     */
    public String getPredicateValue(String name, int index, String userid) throws NoSuchPredicateException
    {
        return multiplexor.getPredicateValue(name, index, userid);
    }


    /**
     *  <p>
     *  Defers to the corresponding method in the
     *  {@link #multiplexor} managed by this class.
     *  </p>
     *  <p>
     *  This does <i>not</i> check whether the
     *  {@link #multiplexor} is initialized!
     *  </p>
     *
     *  @since 4.1.3
     *
     *  @param userid       the userid to use
     *  @param password     the password to assign
     *  @param secretKey    the secret key that should authenticate this request
     *
     *  @return whether the creation was successful
     */
    public boolean createUser(String userid, String password, String secretKey)
    {
        return multiplexor.createUser(userid, password, secretKey);
    }


    /**
     *  <p>
     *  Defers to the corresponding method in the
     *  {@link #multiplexor} managed by this class.
     *  </p>
     *  <p>
     *  This does <i>not</i> check whether the
     *  {@link #multiplexor} is initialized!
     *  </p>
     *
     *  @since 4.1.3
     *
     *  @param userid       the userid to check
     *  @param password     the password to check
     *  @param secretKey    the secret key that should authenticate this request
     *
     *  @return whether the userid and password combination is valid
     */
    public boolean checkUser(String userid, String password, String secretKey)
    {
        return multiplexor.checkUser(userid, password, secretKey);
    }


    /**
     *  <p>
     *  Defers to the corresponding method in the
     *  {@link #multiplexor} managed by this class.
     *  </p>
     *  <p>
     *  This does <i>not</i> check whether the
     *  {@link #multiplexor} is initialized!
     *  </p>
     *
     *  @since 4.1.3
     *
     *  @param userid       the userid
     *  @param password     the new password
     *  @param secretKey    the secret key that should authenticate this request
     *
     *  @return whether the change was successful
     */
    public boolean changePassword(String userid, String password, String secretKey)
    {
        return multiplexor.changePassword(userid, password, secretKey);
    }
 }