package irc.plugin.buttons;

import irc.*;
import irc.plugin.*;

/**
 * Smiley Picker plugin.
 */
public class Smileys extends Plugin 
{
	private IRCConfiguration _ircConfig;
	private SmileyButtons _buttons=null;

	/**
	 * Mandatory constructor for all plugins.
	 * @param config the IRCConfiguration instance.
	 */
	public Smileys(IRCConfiguration config)
	{
		super(config);
		_ircConfig=config;
	}
	
	public void sourceCreated(Source source,Boolean bring)
	{
		if (_buttons == null)
		{
			//Create and open the ButtonPicker window.
			_buttons=new SmileyButtons(_ircConfig, _ircApplication);
		}

		//If this source is a channel...
		if(source instanceof Channel)
		{
			//...then we install our new interpretor, chaining it with the previous one.
			SmileysInterpretor inter=new SmileysInterpretor(_ircConfig,source.getInterpretor(),_ircApplication, _buttons);
			source.setInterpretor(inter);
		}
	}
	
	public void sourceRemoved(Source source)
	{
		//If this source is a channel...
		if(source instanceof Channel)
		{
			//...then we restore the original interpretor.
			SmileysInterpretor inter=(SmileysInterpretor)source.getInterpretor();
			source.setInterpretor(inter.getNextInterpretor());
		}
	}
}