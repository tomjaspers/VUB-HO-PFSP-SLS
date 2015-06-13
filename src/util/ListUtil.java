package util;
import java.util.Arrays;
import java.util.List;

/**
 * Collection of various utility (auxilary) functions to be used on lists.
 * @author Tom
 *
 */
public class ListUtil {
	
	/**
	 * Returns a modified list with the ith item moved to the jth position, emulating an insert
	 */
	public static final <T> void move(List<T> l, int i, int j) {
		T t = l.remove(i);
		l.add(j, t);
	}

	/**
	 * Returns a hard copy of an simple 2 dimensional array
	 */
	public static int[][] copyOf(int[][] original) {
		int[][] copy = new int[original.length][];
		for (int i = 0; i < original.length; i++) {
			copy[i] = Arrays.copyOf(original[i], original.length);
		}
		return copy;
	}
}
