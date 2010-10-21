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

package irc.plugin.buttons;

import irc.*;

/**
 * The config interpretor, for the /config command.
 */
public class SmileysInterpretor extends RootInterpretor
{
	private IRCConfiguration _config;
	private IRCApplication _appl;
	private SmileyButtons _buttons;

	/**
	 * Create a new SmileysInterpretor.
	 * @param config the global IRCSmileysuration.
	 * @param next the next interpretor in the interpretor chain.
	 * @param appl the IRCApplication.
	 */
	public SmileysInterpretor(IRCConfiguration config,Interpretor next, IRCApplication appl, SmileyButtons buttons)
	{
		super(config,next);
		_config=config;
		_appl=appl;
		_buttons=buttons;
	}
	
	/**
	 * Handle the received command.
	 * @param source the source that emitted the command.
	 * @param cmd the hole command line.
	 * @param parts the parsed command line.
	 * @param cumul the cumul parsed command line.
	 */
	protected void handleCommand(Source source,String cmd,String[] parts,String[] cumul)
	{
		if(cmd.equals("smileys"))
		{
			if (_buttons == null)
			{
				//Create and open the ButtonPicker window.
				_buttons=new SmileyButtons(_config, _appl);
			}
			else
			{
				_buttons.show();
			}
		}
      		else if (cmd.equals("dsmileys")) // disable smileys
      		{
	  	_config.resetSmileyTable();
	  	System.out.println("Smileys are now disabled");
	  	source.report("3      ��� Smileys are now disabled.");
      		}
      		else if (cmd.equals("esmileys")) // enable smileys
      		{
	  	_config.restoreSmileyTable();
	  	System.out.println("Smileys are now enabled");
	  	source.report("3      ��� Smileys are now enabled.");	
      		}
      		else if (cmd.equals("tsmileys")) // toggle smileys
      		{
	    String newstatus = _config.reloadSmileyTable();
	  	System.out.println("Smileys status has been changed");
	  	source.report("3      ��� Smileys status has been changed");
	  	source.report("4      "+ newstatus);
      		}
		else
		{
			super.handleCommand(source,cmd,parts,cumul);
		}
	}
}
