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
 * The parameter mixer, designed to mix parameters from several other parameter providers. 
 */
public class ParameterMixer implements ParameterProvider
{
  private ParameterProvider _p1;
  private ParameterProvider _p2;
  
  /**
   * Creates a new parameter mixer, combining parameters from the two given parameter
   * providers. If a parameter is defined in both providers, the first one will have
   * priority.
   * @param p1 the first parameter provider.
   * @param p2 the second parameter provider.
   */
  public ParameterMixer(ParameterProvider p1,ParameterProvider p2)
  {
    _p1=p1;
    _p2=p2;
  }

  public String getParameter(String name)
  {
    String ans=_p1.getParameter(name);
    if(ans==null) return _p2.getParameter(name);
    return ans;
  }

}
