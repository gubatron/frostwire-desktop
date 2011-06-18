package irc;

import java.util.*;

/**
 * RuleItem.
 */
class RuleItem
{
  /**
   * Handlers.
   */
  public ListHandler[] handlers;
  /**
   * Value.
   */
  public Object value;
}

/**
 * Implements a set of rules.
 */
public class RuleList
{
  private Vector<RuleItem> _list;
  private Object _def;

  /**
   * Create a new, empty, set of rules.
   */
  public RuleList()
  {
    _list=new Vector<RuleItem>();
    _def=null;
  }

  /**
   * Set the default value. By default, this value is null.
   * @param v new default value.
   */
  public void setDefaultValue(Object v)
  {
    _def=v;
  }

  /**
   * Get the default value.
   * @return the default value.
   */
  public Object getDefaultValue()
  {
    return _def;
  }

  /**
   * Add a new rule. The rule uses the given array of list patterns (see ListHandler)
   * and has the given value.
   * @param pattern array of pattern.
   * @param value rule value.
   */
  public void addRule(String[] pattern,Object value)
  {
    ListHandler[] handlers=new ListHandler[pattern.length];
    for(int i=0;i<pattern.length;i++) handlers[i]=new ListHandler(pattern[i]);
    RuleItem item=new RuleItem();
    item.handlers=handlers;
    item.value=value;
    _list.insertElementAt(item,_list.size());
  }

  private boolean match(RuleItem item,String[] pattern)
  {
    ListHandler[] handlers=item.handlers;
    if(pattern.length!=handlers.length) return false;
    for(int i=0;i<handlers.length;i++)
      if(!handlers[i].inList(pattern[i])) return false;
    return true;
  }

  /**
   * Find the first matching rule for the given values.
   * @param values array of values to be tested againts patterns.
   * @return value of the first matching rule, or default value if no matcing
   * rule is found.
   */
  public Object findValue(String[] values)
  {
    int l=_list.size();
    for(int i=0;i<l;i++)
    {
      RuleItem item=(RuleItem)_list.elementAt(i);
      if(match(item,values)) return item.value;
    }
    return _def;
  }
}
