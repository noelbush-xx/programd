/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.aiml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.util.math.MersenneTwisterFast;
import org.apache.commons.collections.map.LRUMap;

/**
 * <p>
 * Handles a <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-random">random</a></code> element.
 * </p>
 * <p>
 * To achieve the kind of randomness expected by
 * users and AIML authors, the following requirements exist:
 * </p> 
 * <ul>
 *     <li>Each <code>random</code> element should have its own &quot;space&quot;. This means that, for example,
 *     if <code>random</code> element <code>A</code> contains five <code>li</code> children, and
 *     <code>random</code> element <code>B</code> contains seven <code>li</code> children, the probability that
 *     any given <code>li</code> of <code>A</code> will be chosen when <code>A</code> is activated should con-
 *     sistently be 1:5, and the probability that any given <code>li</code> of <code>B</code> will be chosen when
 *     <code>B</code> is chosen should consistently be 1:7. Essentially, each <code>random</code> must have its
 *     own unique series of random numbers.</li>
 *     <li>A &quot;unique space&quot; requirement exists as well on a per-user basis: each user should have an
 *     equivalent experience of randomness for each <code>random</code> element, independent of any other users.</li>
 *     <li>The individual bot also has a uniqueness requirement, multiplying the previous two. In effect, if there
 *     are <code>m</code> bots, <code>n</code> users, and <code>p</code> random elements, there are (potentially)
 *     <code>m * n * n</code> independent random number series.</li>
 * </ul>
 * <p>
 * As an alternative to the first point, it is possible (since 4.7) to set Program D to process <code>random</code>
 * elements in a kind of stack-based fashion, so no list item will be repeated (within the same per-user, per-bot
 * space) until all others have been chosen.
 * </p>
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author Jay Myers
 */
public class RandomProcessor extends AIMLProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "random";

    /** The tag name for a listitem element. */
    public static final String LI = "li";

    /**
     * The map in which MersenneTwisterFast random number generators will be stored for each unique botid + userid +
     * random element.
     */
    private LRUMap generators = new LRUMap(100);
    
    /**
     * The map in which indices not-yet-used listitems will be stored if
     * non-repeating random choosing is enabled.  (We store indices rather
     * than references to the listitems themselves, because the DOM Element doesn't
     * appear to implement equals() in a way that makes List operations like remove()
     * work.
     */
    private Map<String, List<Integer>> availableIndices = new HashMap<String, List<Integer>>();

    /**
     * Creates a new RandomProcessor using the given Core.
     * 
     * @param core the Core object to use
     */
    public RandomProcessor(Core core)
    {
        super(core);
        this.availableIndices = core.getStoredObject(RandomProcessor.class.getName(), "availableIndices", this.availableIndices);
        this.generators = core.getStoredObject(RandomProcessor.class.getName(), "generators", this.generators);
    }

    /**
     * @see AIMLProcessor#process(Element, TemplateParser)
     */
    @SuppressWarnings({ "boxing", "unchecked" })
    @Override
    public String process(Element element, TemplateParser parser) throws ProcessorException
    {
        // Construct the identifying string (botid + userid + element
        // contents).
        String userid = parser.getUserID();
        String identifier = parser.getBotID() + userid + element.hashCode();

        // Does the generators map already contain this one?
        MersenneTwisterFast generator = (MersenneTwisterFast)this.generators.get(identifier);
        if (generator == null)
        {
            generator = new MersenneTwisterFast(System.currentTimeMillis());
            this.generators.put(identifier, generator);
        }

        List<Element> listitems = element.getChildren();
        int nodeCount = listitems.size();

        // Only one <li></li> child means we don't have to pick anything.
        if (nodeCount == 1)
        {
            return parser.evaluate(listitems.get(0).getChildren());
        }

        // Otherwise, select a random element of the listitem (if strategy is pure-random).
        if (this._core.getSettings().getRandomStrategy() == CoreSettings.RandomStrategy.PURE_RANDOM)
        {
            return parser.evaluate(listitems.get(generator.nextInt(nodeCount)).getContent());
        }
        
        // If we get here, then the no-repeat strategy is wanted.
        List<Integer> indices;
        Integer choice = null;
        
        // Check whether this random + userid + botid has been selected before.
        if (this.availableIndices.containsKey(identifier))
        {
            // If it has, get the remaining available sets.
            indices = this.availableIndices.get(identifier);
            
            // Note that, because of the logic below, this set will never get to size 0.
            assert indices.size() > 0 : "Random strategy logic failed.";

            /*
             * If it is complete, then we've been through all before, and
             * the last index in the list was the last one chosen (see below),
             * so make sure this first choice does not repeat the last one.
             */
            if (indices.size() == nodeCount)
            {
                choice = indices.get(generator.nextInt(indices.size() - 1));
            }
            else
            {
                // Otherwise just make a random choice from the indices.
                choice = indices.get(generator.nextInt(indices.size()));
            }
        }
        else
        {
            // If it has not (been selected before), create a new set containing an index for each listitem.
            indices = makeIncrementingList(nodeCount);
            this.availableIndices.put(identifier, indices);

            // Make a random choice from the indices.
            choice = indices.get(generator.nextInt(indices.size()));
        }
        
        // Remove the chosen index.
        indices.remove(choice);
        
        // If this has reduced the size to zero,
        if (indices.size() == 0)
        {
            // Reconstitute the list,
            indices.addAll(makeIncrementingList(nodeCount));
            
            // but remove the last choice,
            indices.remove(choice);
            
            // and place it last, so we can avoid repeating it next go-round (see above).
            indices.add(choice);
        }
        
        // Evaluate the node corresponding to the chosen index.
        return parser.evaluate(listitems.get(choice).getContent());
    }
    
    @SuppressWarnings("boxing")
    private static List<Integer> makeIncrementingList(int size)
    {
        List<Integer> result = new ArrayList<Integer>();
        for (int index = 0; index < size; index++)
        {
            result.add(index);
        }
        return result;
    }
}
