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

/*
    4.1.4 [00] - December 2001, Noel Bush
    - moved all get/set methods to PredicateMaster
    - changed this now to require "save" and "load"
*/

package org.alicebot.server.core;

import org.alicebot.server.core.parser.AIMLParser;
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
 *  has been changed into an implementation of this interface. There are two
 *  new implementations called {@link FlatFileMultiplexor} and {@link DBMultiplexor}.
 *  </p>
 *
 *  @since 4.1.3
 *
 *  @author Noel Bush
 *
 *  @see {@link FlatFileMultiplexor}
 *  @see {@link DBMultiplexor}
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
     *  @param parser   the parser object in use
     *
     *  @return the response to the input
     */
    public String getInternalResponse(String input, String userid, AIMLParser parser);

        
    /**
     *  Saves a predicate for a given <code>userid</code>.  This only applies to
     *  Multiplexors that provide long-term storage (others may just do nothing).
     *
     *  @since 4.1.4
     *
     *  @param name     predicate name
     *  @param value    predicate value
     *  @param userid   user identifier
     */
    public void savePredicate(String name, String value, String userid);


    /**
     *  Loads a predicate into memory
     *  for a given <code>userid</code>.  This only applies to
     *  Multiplexors that provide long-term storage (others may just do nothing).
     *
     *  @since 4.1.4
     *
     *  @param name         predicate name
     *  @param userid       user identifier
     *
     *  @return the predicate value
     *
     *  @throws NoSuchPredicateException if there is no predicate with this name
     */
    public String loadPredicate(String name, String userid) throws NoSuchPredicateException;


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
 }
