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

package irc;

/**
 * LinkedListNode.
 */
class LinkedListNode
{
  /**
   * Next node.
   */
  public LinkedListNode next=null;
  /**
   * Previous node.
   */
  public LinkedListNode prev=null;
  /**
   * Node content.
   */
  public Object item=null;
}

/**
 * A very simple linked list.
 */
public class LinkedList
{
  private int _size;
  private LinkedListNode _head;
  private LinkedListNode _tail;

  /**
   * Create a new, empty linked list.
   */
  public LinkedList()
  {
    _head=new LinkedListNode();
    _tail=new LinkedListNode();
    _head.next=_tail;
    _tail.prev=_head;
    _size=0;
  }

  /**
   * Get the size of the list.
   * @return list size.
   */
  public int size()
  {
    return _size;
  }

  /**
   * Add the given object at the end of the list.
   * @param obj object to add.
   */
  public void addLast(Object obj)
  {
    LinkedListNode node=new LinkedListNode();
    node.item=obj;
    node.next=_tail;
    node.prev=_tail.prev;
    node.prev.next=node;
    node.next.prev=node;
    _size++;
  }

  /**
   * Remove and return the first object of the list.
   * @return removed object.
   * @throws RuntimeException if list is empty.
   */
  public Object removeFirst()
  {
    if(_size==0) throw new RuntimeException("List empty");
    LinkedListNode node=_head.next;

    node.next.prev=_head;
    _head.next=node.next;
    _size--;
    return node.item;
  }

}
