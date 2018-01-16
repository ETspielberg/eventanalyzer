package unidue.ub.batch.eventanalyzer;

import unidue.ub.media.monographs.Item;

import java.util.regex.Pattern;

/**
 * Filter to limit the items retrieved from the Aleph database to only given
 * collections and materials.
 *
 * @author Frank L\u00FCtzenkirchen
 * @version 1
 */
class ItemFilter {

    private Pattern collectionPattern = Pattern.compile("...");

    private String materials;

    /**
     * builds a new instance of a <code>ItemFilter</code>-object by defining a
     * set of collections and materials.
     *
     * @param collections a string with the collections to be taken into account,
     *                    separated by blanks
     * @param materials   a string with the materials to be taken into account,
     *                    separated by blanks
     */

    ItemFilter(String collections, String materials) {
        this.materials = materials == null ? "" : materials.trim();

        if (!collections.isEmpty()) {
            while (collections.length() < 3)
                collections = collections + "?";
            collections = collections.toUpperCase().replace('?', '.');
            collections = collections.replaceAll("[\\s,]+", "|");
            collectionPattern = Pattern.compile(collections);
        }
    }

    /**
     * checks whether an item matches the filter definitions.
     *
     * @param item an item to be tested
     * @return boolean true, if item matches filter
     */
    boolean matches(Item item) {
        return materialMatches(item.getMaterial()) && collectionMatches(item.getCollection());
    }

    private boolean materialMatches(String material) {
        return (material == null) || materials.isEmpty() || materials.contains(material.trim());
    }

    private boolean collectionMatches(String collection) {
        return collectionPattern.matcher(collection).matches();
    }
}
