package ads.trie;

import java.util.Collection;

public interface Trie {
    /**
     * Is the exact string specified in the trie?
     */
    boolean checkExactMatch(String s);

    /**
     * Do any strings in the trie start with this prefix?
     */
    boolean checkPrefix(String s);

    /**
     * All strings in the trie that match the specified prefix.
     */
    Collection<String> getAllMatchingPrefix(String s);

    /**
     * Idempotently insert the specified string into the trie.
     * @return {@code true} if the trie did not already contain this string,
     *         {@code false} otherwise.
     */
    boolean insert(String s);

    /**
     * Remove the exact string from the trie, if it was present.
     * @return {@code true} if the string to remove was in the trie,
     *         {@code false} otherwise.
     */
    boolean removeExactMatch(String s);

    /**
     * Remove all strings that start with the prefix from the trie, if any exist.
     * @return {@code true} if any strings in the trie started with the specified prefix,
     *         {@code false} otherwise.
     */
    boolean removeByPrefix(String prefix);
}
