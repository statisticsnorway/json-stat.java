package org.jsonstat.v1.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CollectionUtils {
    private CollectionUtils() {}
    public static <A> String join(Iterable<A> combination, String sep) {
        StringBuilder sb = new StringBuilder();
        for (A s : combination) {
            if (sb.length() > 0) {
                sb.append(sep);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public static List<String[]> product(List<List<String>> lists)
    {
        List<String[]> results = new ArrayList<>();
        product(results, lists, 0, new String[lists.size()]);
        return results;
    }

    private static void product(List<String[]> results, List<List<String>> lists, int depth, String[] current)
    {
        for (int i = 0; i < lists.get(depth).size(); i++) {
            current[depth] = lists.get(depth).get(i);
            if (depth < lists.size() - 1)
                product(results, lists, depth + 1, current);
            else{
                results.add(Arrays.copyOf(current, current.length));
            }
        }
    }

}
