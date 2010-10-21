package irc.tree;

/**
 * The comparator interface.
 */
public interface Comparator
{
  /**
   * Compare two objects.
   * @param a first object.
   * @param b second object.
   * @return negative if a lesser than b, positive if a greater than b, and zero if a equals b.
   */
  public int compare(Object a,Object b);
}

