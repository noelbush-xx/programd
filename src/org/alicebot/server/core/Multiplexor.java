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
    - added createUser(), checkUser() and changePassword()
*/

package org.alicebot.server.core;

import org.alicebot.server.core.responder.Responder;

/**
 *  <p>
 *  &quot;To multiplex&quot; means &quot;to select one from many inputs&quot;.
 *  A <code>Multiplexor</code> multiplexes the clients of a bot
 *  and keeps track of all their predicate values.
 *  </p>
 *  <p>
 *  The following metaphor was supplied by Richard Wallace:
 *  The <code>Multiplexor</code> controls a
 *  short &quot;carnival ride&quot; for each user. The Multiplexor puts the
 *  client in his/her seat, hands him/her an id card, and closes the door.
 *  The client gets one &quot;turn of the crank&quot;.  He/she enters
 *  his/her id, multiline query, and then receives the reply.  The door opens,
 *  the Multiplexor ushers him/her out, and seats the next client.
 *  </p>
 *  <p>
 *  Historically, the functionality specified by this class was
 *  implemented in {@link Classifier}. However, {@link Classifier} evolved
 *  to include database access methods that were not desirable for all
 *  implementations. Furthermore, {@link Classifier} lost part of its
 *  original purpose as a &quot;classifier of user inputs into categories&quot;.
 *  Hence, the Program D {@link Classifier} has been left as-is, except it
 *  has been changed into an implementation of this interface. You may wish
 *  to implement another <code>Multiplexor</code> that does not use a database,
 *  or that uses a database in a more efficient way than {@link Classifier}.
 *  </p>
 *
 *  @since 4.1.3
 *
 *  @author Noel Bush
 */
public interface Multiplexor
{
    /**
     *  Initializes a <code>Multiplexor</code> (if necessary).
     *  This may be implemented as an empty method.
     */
    public void initialize();


    /**
     *  Prepares the input via <a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-input-normalization">normalization</a>,
     *  then produces the response to the input for a given <code>userid</code>.
     *
     *  @since 4.1.3
     *
     *  @param input    the input
     *  @param userid   the user identifier
     *
     *  @return the response
     */
    public String getResponse(String input, String userid);


    /**
     *  <p>
     *  Same as {@link #getResponse(String, String)}, except it takes a {@link org.alicebot.server.core.responder.Responder Responder}
     *  as a parameter, and should append the response to the <code>Responder</code>
     *  in addition to returning the response.
     *  </p>
     *  <p>
     *  Applications that do not use a <code>Responder</code> should
     *  perhaps implement the {@link #getResponse(String)} or {@link #getResponse(String, String)}
     *  form (as appropriate) and just call it from here, or throw an exception.
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
    public String getResponse(String input, String userid, Responder robot);


    /**
     *  Gets a response from an input that is the result of an
     *  internal match, such as a srai.  The input is assumed to
     *  be a single sentence with all substitutions already performed.
     *
     *  @param input    the input
     *  @param userid   the userid requesting the response
     *
     *  @return the response to the input
     */
    public String getInternalResponse(String input, String userid);

        
    /**
     *  Sets a predicate name (unindexed) with a specific value,
     *  for a given <code>userid</code>.
     *
     *  @since 4.1.3
     *
     *  @param name     predicate name
     *  @param value    value to assign to the name
     *  @param userid   user identifier
     *
     *  @return the new value of the predicate, or its name (if it is return-value-when-set)
     */
    public String setPredicateValue(String name, String value, String userid);


    /**
     *  Sets an indexed value of a <code>predicate</code> with a specific value,
     *  for a given <code>userid</code>.
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
    public String setPredicateValue(String name, int index, String value, String userid);


    /**
     *  Pushes a new value of an indexed <code>predicate</code>
     *  onto the stack of indexed values for this <code>predicate</code>
     *  for a given <code>userid</code>.
     *
     *  @since 4.1.3
     *
     *  @param name         predicate name
     *  @param value        value to push onto the stack
     *  @param userid       user identifier
     */
    public String pushPredicateValue(String name, String value, String userid);


    /**
     *  Gets the value of an unindexed <code>predicate</code>
     *  for a given <code>userid</code>.
     *
     *  @since 4.1.3
     *
     *  @param name         predicate name
     *  @param userid       user identifier
     *
     *  @return value of the <code>predicate</code>
     *
     *  @throws NoSuchPredicateException if there is no predicate with this name
     */
    public String getPredicateValue(String name, String userid) throws NoSuchPredicateException;


    /**
     *  Gets an indexed value of a <code>predicate</code>
     *  for a given <code>userid</code>.
     *
     *  @since 4.1.3
     *
     *  @param name         predicate name
     *  @param index        index of the <code>predicate</code> value desired
     *  @param userid       user identifier
     *
     *  @return value of the <code>predicate</code> at <code>index</code>, or an empty string if not set / no value
     */
    public String getPredicateValue(String name, int index, String userid) throws NoSuchPredicateException;


    /**
     *  Creates a new user entry, given a userid and password.
     *  Multiplexors for which this makes no sense should just
     *  return true.
     *
     *  @param userid       the userid to use
     *  @param password     the password to assign
     *  @param secretKey    the secret key that should authenticate this request
     *
     *  @return whether the creation was successful
     */
    public boolean createUser(String userid, String password, String secretKey);


    /**
     *  Checks whether a given userid and password combination is valid.
     *  Multiplexors for which this makes no sense should just
     *  return true.
     *
     *  @param userid       the userid to check
     *  @param password     the password to check
     *  @param secretKey    the secret key that should authenticate this request
     *
     *  @return whether the userid and password combination is valid
     */
    public boolean checkUser(String userid, String password, String secretKey);
    

    /**
     *  Changes the password associated with a userid.
     *  Multiplexors for which this makes no sense should just
     *  return true.
     *
     *  @param userid       the userid
     *  @param password     the new password
     *  @param secretKey    the secret key that should authenticate this request
     *
     *  @return whether the change was successful
     */
    public boolean changePassword(String userid, String password, String secretKey);
    

    /**
     *  Implementations of <code>Multiplexor</code> may provide
     *  static versions of the above required methods within here,
     *  but this is not guaranteed.
     */
    public static class StaticSelf
    {
    }
 }