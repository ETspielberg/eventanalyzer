package unidue.ub.batch.eventanalyzer;

import unidue.ub.media.monographs.Event;

import java.util.regex.Pattern;

/**
 * Filter to limit the events retrieved from the Aleph database to only given
 * user groups.
 *
 * @author Eike Spielberg
 * @version 1
 */
public class EventFilter {

    private Pattern usergroupsPattern = Pattern.compile("...");

    /**
     * builds a new instance of a <code>EventFilter</code>-object by defining a
     * set of user groups.
     *
     * @param userGroups a string with the collections to be taken into account,
     *                   separated by blanks
     */
    public EventFilter(String userGroups) {
        if (!userGroups.isEmpty()) {
            userGroups = userGroups.toUpperCase().replace('?', '.');
            userGroups = userGroups.replaceAll("[\\s,]+", "|");
            usergroupsPattern = Pattern.compile(userGroups);
        }
    }

    /**
     * checks whether an event matches the filter definitions.
     *
     * @param event an event to be tested
     * @return boolean true, if event matches filter
     */
    public boolean matches(Event event) {
        return usergroupsPattern.matcher(event.getBorrowerStatus()).matches();
    }
}
