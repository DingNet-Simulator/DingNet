package util;

import java.util.List;
import java.util.NoSuchElementException;

public class ListHelper {

    public static <N> N getLast(List<N> list) {
        if (list == null) {
            throw new NoSuchElementException();
        }
        return list.get(list.size()-1);
    }
}
