package org.aitools.programd.server;

import org.aitools.programd.Core;

/**
 * This is an object that provides an interface to a Program D bot to be used from a server context such as a JSP page.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class BotAccess {

  protected Core _core;

  protected String botid;

  protected String userid;

  /**
   * Creates a new <code>Bot</code> with the given core, bot id and user id.
   * 
   * @param core
   * @param bot the id of the bot
   * @param user the id of the user
   */
  public BotAccess(Core core, String bot, String user) {
    this._core = core;
    this.botid = bot;
    this.userid = user;
  }

  /**
   * @return the underlying bot object
   */
  public org.aitools.programd.Bot getBot() {
    return this._core.getBot(this.botid);
  }

  /**
   * @return the botid
   */
  public String getBotId() {
    return this.botid;
  }

  /**
   * Returns a response to the given input, for the assigned botid and userid.
   * 
   * @param input the user input
   * @return the response to the input
   */
  public String getResponse(String input) {
    return this._core.getResponse(input, this.userid, this.botid);
  }

  /**
   * Sets a predicate value.
   * 
   * @param name
   * @param value
   * @return the result of the set operation (the predicate name or the value, depending on predicate type)
   */
  public String set(String name, String value) {
    return this._core.getPredicateMaster().set(name, value, this.userid, this.botid);
  }
}
