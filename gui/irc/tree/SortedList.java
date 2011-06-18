/*****************************************************/
/*          This java file is a part of the          */
/*                                                   */
/*           -  Plouf's Java IRC Client  -           */
/*                                                   */
/*   Copyright (C)  2002 - 2004 Philippe Detournay   */
/*                                                   */
/*         All contacts : theplouf@yahoo.com         */
/*                                                   */
/*  PJIRC is free software; you can redistribute     */
/*  it and/or modify it under the terms of the GNU   */
/*  General Public License as published by the       */
/*  Free Software Foundation; version 2 or later of  */
/*  the License.                                     */
/*                                                   */
/*  PJIRC is distributed in the hope that it will    */
/*  be useful, but WITHOUT ANY WARRANTY; without     */
/*  even the implied warranty of MERCHANTABILITY or  */
/*  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU   */
/*  General Public License for more details.         */
/*                                                   */
/*  You should have received a copy of the GNU       */
/*  General Public License along with PJIRC; if      */
/*  not, write to the Free Software Foundation,      */
/*  Inc., 59 Temple Place, Suite 330, Boston,        */
/*  MA  02111-1307  USA                              */
/*                                                   */
/*****************************************************/

package irc.tree;

import java.util.*;

//class GroupItem
//{
//  //private Hashtable t;
//  //Object[] t;
//  Object s;
//
//  public GroupItem(Object o)
//  {
//    //t=new Hashtable();
//    //t=new Object
//    //t=null;
//    s=null;
//    add(o);
//  }
//
//  public void add(Object o)
//  {
//    if(s==null) s=o;
//    else
//    {
//      /*if(t==null)
//      {
//        t=new Object[] {o};
//      }
//      else
//      {
//      }*/
//      throw new RuntimeException("Didn't expect similar keys!!");
//    }
//    //t.put(o,o);
//  }
//
//  public void remove(Object o)
//  {
//    //t.remove(o);
//    s=null;
//  }
//
//  public int size()
//  {
//    //return t.size();
//    if(s==null) return 0;
//    //if(t==null) return 1;
//    return 1;
//    //return t.length+1;
//  }
//
//  public Object getFirstItem()
//  {
//    //return t.elements().nextElement();
//    return s;
//  }
//
//  //public Enumeration elements()
//  //{
//    //return t.elements();
//  //}
//}

/**
 * TreeNode.
 */
class TreeNode
{
  /**
   * Left node.
   */
  public TreeNode left;
  /**
   * Right node.
   */
  public TreeNode right;
  //public GroupItem item;
  //public Object objects[];
  /**
   * Objects in this node.
   */
  public Vector<Object> objects;
  //private int count;
  private Comparator _comparator;

  /**
   * Create a new TreeNode
   * @param itm first item.
   * @param comparator item comparator.
   */
  public TreeNode(Object itm,Comparator comparator)
  {
    _comparator=comparator;
    //item=itm;
    objects=new Vector<Object>(1,0);
    objects.insertElementAt(itm,objects.size());
    //count=1;
    left=new TreeNode(comparator);
    right=new TreeNode(comparator);
  }

  /**
   * Create a new TreeNode
   * @param comparator comparator.
   */
  public TreeNode(Comparator comparator)
  {
    _comparator=comparator;
    //item=null;
    objects=new Vector<Object>(1,0);
    left=null;
    right=null;
    //count=0;
  }

  /**
   * Returns true if node is external.
   * @return true if node is external, false otherwise.
   */
  public boolean external()
  {
    return((left==null) || (right==null));
  }

  /**
   * Remove the given item.
   * @param itm item to remove.
   * @return resulting tree node.
   * @throws Exception
   */
  public TreeNode remove(Object itm) throws Exception
  {
    if(external()) throw new Exception();

    int compare=_comparator.compare(itm,/*item.getFirstItem()*/objects.elementAt(0));
    if(compare==0)
    {
      //item.remove(itm);
      objects.removeElement(itm);
      //count--;
      if(objects.size()==0)
      {
        if(left.external()) return right;
        if(right.external()) return left;
        return right.addTree(left);
      }
      return this;
    }
    else if(compare<0)
    {
      left=left.remove(itm);
      return this;
    }
    else
    {
      right=right.remove(itm);
      return this;
    }
  }

  private TreeNode addTree(TreeNode tree) throws Exception
  {
    if(external()) return tree;
    if(tree.external()) return this;
    
    int compare=_comparator.compare(tree.objects.elementAt(0),objects.elementAt(0));
    if(compare==0)
    {
      throw new Exception();
    }
    else if(compare<0)
    {
      left=left.addTree(tree);
      return this;
    }
    else
    {
      right=right.addTree(tree);
      return this;
    }
  }

  /**
   * Add an object. 
   * @param itm object to tadd.
   * @return resulting tree node.
   * @throws Exception
   */
  public TreeNode add(Object itm) throws Exception
  {
    if(external())
    {
      return new TreeNode(itm,_comparator);
    }

    int compare=_comparator.compare(itm,objects.elementAt(0));
    if(compare==0)
    {
      //item.add(itm);
      objects.insertElementAt(itm,objects.size());
      //throw new RuntimeException("Duplicate key");
      return this;
    }
    else if(compare<0)
    {
      left=left.add(itm);
      return this;
    }
    else
    {
      right=right.add(itm);
      return this;
    }
  }

  /**
   * Start an inorder tree traversal.
   * @param lis traversal listener.
   * @param param user parameter.
   */
  public void inorder(TreeTraversalListener lis,Object param)
  {
    if(external()) return;
    left.inorder(lis,param);
    //Enumeration e=item.elements();
    //while(e.hasMoreElements()) lis.nextItem(e.nextElement(),param);
    //lis.nextItem(object,param);
    for(int i=0;i<objects.size();i++) lis.nextItem(objects.elementAt(i),param);
    right.inorder(lis,param);
  }
}

/**
 * A Sorted List.
 */
public class SortedList implements TreeTraversalListener
{
  private TreeNode _root;
  private Vector<Object> _items;
  private Comparator _comparator;
  private boolean _upToDate;

  /**
   * Create a new SortedList, using the given Comparator for the order definition.
   * @param comparator comparator to be used for ordering.
   */
  public SortedList(Comparator comparator)
  {
    _comparator=comparator;
    _root=new TreeNode(_comparator);
    _items=new Vector<Object>();
    _upToDate=false;
  }

  /**
   * Get the amount of items in the list.
   * @return list size.
   */
  public int getSize()
  {
    if(!_upToDate) computeVector();
    return _items.size();
  }

  /**
   * Add an item in the list.
   * @param item item to add.
   */
  public void add(Object item)
  {
    try
    {
      _root=_root.add(item);
    }
    catch(Exception e)
    {
    }
    _upToDate=false;
  }

  /**
   * Remove the given item from the list.
   * @param item item to remove from the list.
   */
  public void remove(Object item)
  {
    try
    {
      _root=_root.remove(item);
    }
    catch(Exception e)
    {
    }
    _upToDate=false;
  }

  public void begin(Object param)
  {
    _items=new Vector<Object>();
  }

  public void nextItem(Object item,Object param)
  {
    _items.insertElementAt(item,_items.size());
  }

  public void end(Object param)
  {
    _upToDate=true;
  }

  private void computeVector()
  {
    getItems(this,null);
  }

  /**
   * Get a sorted enumeration of items.
   * @return a sorted enumeration of items in the list.
   */
  public Enumeration<Object> getItems()
  {
    if(!_upToDate) computeVector();
    return _items.elements();
  }

  /**
   * Get the i'th element in the list. Index are ordered.
   * @param i index.
   * @return object at i'th position.
   */
  public Object getItemAt(int i)
  {
    if(!_upToDate) computeVector();
    return _items.elementAt(i);
  }

  /**
   * Begin a new traversal.
   * @param lis traversal listener.
   * @param param user parameter.
   */
  public void getItems(TreeTraversalListener lis,Object param)
  {
    lis.begin(param);
    _root.inorder(lis,param);
    lis.end(param);
  }

  /**
   * Clear the list.
   */
  public void clear()
  {
    _root=new TreeNode(_comparator);
    _items=new Vector<Object>();
    _upToDate=false;
  }
  
}

