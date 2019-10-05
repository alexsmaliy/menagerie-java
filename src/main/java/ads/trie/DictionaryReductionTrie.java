package ads.trie;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

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
        // or return empty result if prefix is not found in trie.
        for (int i = 0; i < b.length - 8; i++) {
            if (b[i] && trie.t != null) {
                trie = trie.t;
            } else if (!b[i] && trie.f != null) {
                trie = trie.f;
            } else {
                return Collections.emptyList();
            }
        }
        List<String> ret = new ArrayList<>();
        boolean[][] suffixes = dfs(trie);
        for (int i = 0; i < suffixes.length; i++) {
            ret.add(s + unreduce(suffixes[i]));
        }
        return ret;
    }

    private static boolean[][] dfs(DictionaryReductionTrie trie) {
        Deque<List<DictionaryReductionTrie>> dq = new ArrayDeque<>();
        List<List<DictionaryReductionTrie>> suffixes = new ArrayList<>();
        List<DictionaryReductionTrie> root = new ArrayList<>();
        root.add(trie);
        dq.add(root);
        while (!dq.isEmpty()) {
            List<DictionaryReductionTrie> candidate = dq.pollFirst();
            DictionaryReductionTrie t = candidate.get(candidate.size() - 1);
            if (t.t == null && t.f == null) {
                suffixes.add(candidate);
                continue;
            }
            if (t.t != null) {
                List<DictionaryReductionTrie> newCandidate = new ArrayList<>(candidate);
                newCandidate.add(t.t);
                dq.add(newCandidate);
            }
            if (t.f != null) {
                List<DictionaryReductionTrie> newCandidate = new ArrayList<>(candidate);
                newCandidate.add(t.f);
                dq.add(newCandidate);
            }
        }
        boolean[][] ret = new boolean[suffixes.size()][];
        for (int i = 0; i < suffixes.size(); i++) {
            List<DictionaryReductionTrie> suffix = suffixes.get(i);
            boolean[] ith = new boolean[suffix.size() - 1];
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
        // Ignore the final `\0` char.
        char[] c = new char[numChars - 1];
        for (int i = 0; i < numChars - 1; i++) {
            c[i] = (char) (
                (b[i * 8] ? 1 : 0)
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
