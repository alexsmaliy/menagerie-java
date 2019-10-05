package ads.trie;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public final class DictionaryReductionTrie {

    private DictionaryReductionTrie t;
    private DictionaryReductionTrie f;
    int maxDepth;

    public boolean insert(String s) {
        boolean[] b = reduce(s);
        DictionaryReductionTrie trie = this;
        boolean isNew = false;
        for (int i = 0; i < b.length; i++) {
            if (b[i]) {
                if (trie.t == null) {
                    trie.t = new DictionaryReductionTrie();
                    isNew = true;
                }
                trie.maxDepth = Math.max(trie.maxDepth, b.length - i);
                trie = trie.t;
            } else {
                if (trie.f == null) {
                    trie.f = new DictionaryReductionTrie();
                    isNew = true;
                }
                trie.maxDepth = Math.max(trie.maxDepth, b.length - i);
                trie = trie.f;
            }
        }
        return isNew;
    }

    public boolean removeExactMatch(String s) {
        boolean[] b = reduce(s);
        DictionaryReductionTrie trie = this;
        DictionaryReductionTrie deletionCandidate = this;
        List<DictionaryReductionTrie> path = new ArrayList<>();
        path.add(this);
        boolean deletionDirection = b[0];
        for (int i = 0; i < b.length; i++) {
            if (b[i]) {
                // trie does not contain value to delete
                if (trie.t == null) return false;
                if (trie.f != null) {
                    deletionCandidate = trie;
                    deletionDirection = true;
                }
                path.add(trie);
                trie = trie.t;
            } else {
                // trie does not contain value to delete
                if (trie.f == null) return false;
                if (trie.t != null) {
                    deletionCandidate = trie;
                    deletionDirection = false;
                }
                path.add(trie);
                trie = trie.f;
            }
        }
        if (deletionDirection) {
            deletionCandidate.t = null;
            deletionCandidate.maxDepth = deletionCandidate.f == null
                ? 0
                : deletionCandidate.f.maxDepth + 1;
        } else {
            deletionCandidate.f = null;
            deletionCandidate.maxDepth = deletionCandidate.t == null
                ? 0
                : deletionCandidate.t.maxDepth + 1;
        }
        for (int i = path.size() - 2; i >= 0; i--) {
            DictionaryReductionTrie ith = path.get(i);
            ith.maxDepth = ith.f == null
                ? (ith.t == null ? 0 : ith.t.maxDepth + 1)
                : (ith.t == null ? ith.f.maxDepth + 1 : Math.max(ith.f.maxDepth, ith.t.maxDepth) + 1);
        }
        return true;
    }

    // Return `true` iff an exact match has been inserted into the trie.
    // Namely, we check if `s + '\0'` is in the trie.
    public boolean checkExactMatch(String s) {
        return checkInternal(reduce(s), 0);
    }

    // Checks the trie for the string, ignoring the final `\0` character.
    public boolean checkPrefix(String s) {
        return checkInternal(reduce(s), 8);
    }

    public Collection<String> getAllMatchingPrefix(String s) {
        boolean[] b = reduce(s);
        DictionaryReductionTrie trie = this;
        // Descend to the root node of all suffixes,
        // or return empty result if the prefix is not found in the trie.
        for (int i = 0; i < b.length - 8; i++) {
            if (b[i] && trie.t != null) {
                trie = trie.t;
            } else if (!b[i] && trie.f != null) {
                trie = trie.f;
            } else {
                return Collections.emptyList();
            }
        }

        // DFS to get all suffixes from the node we reached.
        List<String> ret = new ArrayList<>();
        boolean[][] suffixes = dfs(trie);

        // Translate all the suffixes into strings.
        for (int i = 0; i < suffixes.length; i++) {
            ret.add(s + unreduce(suffixes[i]));
        }
        return ret;
    }

    // Generate all suffixes of a node using simple DFS using a LIFO queue.
    // Since node values are not distinct (namely, only true and false),
    // we store lists of the nodes themselves as we traverse them. Lists
    // are ultimately translated into boolean arrays, and then remapped
    // back to strings by the caller.
    private static boolean[][] dfs(DictionaryReductionTrie trie) {
        Queue<List<DictionaryReductionTrie>> q = new ArrayDeque<>();
        List<List<DictionaryReductionTrie>> suffixes = new ArrayList<>();

        // We start the queue with the root node that the caller provided.
        // We do not include the root's boolean value as part of the return.
        // We only use it to traverse each suffix to determine whether each
        // node is its parent's `t` or `f` child.
        List<DictionaryReductionTrie> root = new ArrayList<>();
        root.add(trie);
        q.add(root);

        // DFS itself.
        while (!q.isEmpty()) {
            List<DictionaryReductionTrie> candidate = q.poll();

            // Get the last noe in the partial suffix.
            DictionaryReductionTrie t = candidate.get(candidate.size() - 1);
            // The suffix is complete if its last node has no children.
            // Add it to the list of suffixes and add nothing back into the queue.
            if (t.t == null && t.f == null) {
                suffixes.add(candidate);
                continue;
            }
            // If there is a `t` child, extend the suffix and add it back to the queue.
            if (t.t != null) {
                List<DictionaryReductionTrie> newCandidate = new ArrayList<>(candidate);
                newCandidate.add(t.t);
                q.add(newCandidate);
            }
            // Same for the scenario when there is an `f` child.
            if (t.f != null) {
                List<DictionaryReductionTrie> newCandidate = new ArrayList<>(candidate);
                newCandidate.add(t.f);
                q.add(newCandidate);
            }
        }

        // Traverse each suffix to translate it into a boolean array.
        boolean[][] ret = new boolean[suffixes.size()][];
        for (int i = 0; i < suffixes.size(); i++) {
            List<DictionaryReductionTrie> suffix = suffixes.get(i);
            // The boolean array for the ith suffix is everything
            // in the suffix, except the root that every suffix shares.
            boolean[] ith = new boolean[suffix.size() - 1];
            // Iterate backwards through each node in the suffix,
            // checking if it is its predecessor's `t` or `f` child.
            for (int j = suffix.size() - 1; j > 0; j--) {
                if (suffix.get(j) == suffix.get(j - 1).t) {
                    ith[j - 1] = true;
                } else {
                    ith[j - 1] = false;
                }
            }
            ret[i] = ith;
        }
        return ret;
    }

    // A convenience method to check whether `boolean[] b` is in the trie,
    // up to a prefix of some ignored length.
    private boolean checkInternal(boolean[] b, int ignoredSuffixLength) {
        DictionaryReductionTrie trie = this;
        for (int i = 0; i < b.length - ignoredSuffixLength; i++) {
            if (b[i] && trie.t != null) {
                trie = trie.t;
            } else if (!b[i] && trie.f != null) {
                trie = trie.f;
            } else {
                return false;
            }
        }
        return true;
    }

    private static boolean[] reduce(String s) {
        boolean[] ret = new boolean[s.length() * 8 + 8];
        // Translate each char to 8 booleans.
        for (int i = 0; i < s.length(); i++) {
            char si = s.charAt(i);
            ret[i * 8    ] = (si &   1) != 0;
            ret[i * 8 + 1] = (si &   2) != 0;
            ret[i * 8 + 2] = (si &   4) != 0;
            ret[i * 8 + 3] = (si &   8) != 0;
            ret[i * 8 + 4] = (si &  16) != 0;
            ret[i * 8 + 5] = (si &  32) != 0;
            ret[i * 8 + 6] = (si &  64) != 0;
            ret[i * 8 + 7] = (si & 128) != 0;
        }
        // Terminate string with `\0`.
        ret[s.length() * 8    ] = false;
        ret[s.length() * 8 + 1] = false;
        ret[s.length() * 8 + 2] = false;
        ret[s.length() * 8 + 3] = false;
        ret[s.length() * 8 + 4] = false;
        ret[s.length() * 8 + 5] = false;
        ret[s.length() * 8 + 6] = false;
        ret[s.length() * 8 + 7] = false;
        return ret;
    }

    private static String unreduce(boolean[] b) {
        int numChars = b.length >> 3;
        // Ignore the final `\0` char that `reduce()` introduces.
        char[] c = new char[numChars - 1];
        for (int i = 0; i < numChars - 1; i++) {
            c[i] = (char) (
                  (b[i * 8    ] ?   1 : 0)
                + (b[i * 8 + 1] ?   2 : 0)
                + (b[i * 8 + 2] ?   4 : 0)
                + (b[i * 8 + 3] ?   8 : 0)
                + (b[i * 8 + 4] ?  16 : 0)
                + (b[i * 8 + 5] ?  32 : 0)
                + (b[i * 8 + 6] ?  64 : 0)
                + (b[i * 8 + 7] ? 128 : 0)
            );
        }
        return String.valueOf(c);
    }
}
