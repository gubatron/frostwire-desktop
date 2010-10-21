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

import java.util.*;

/**
 * Audio configuration class.
 */
public class AudioConfiguration
{
  private SoundHandler _sound;

	private String _query;
	private String _beep;
	private Hashtable _word;

  /**
   * Create a new AudioConfiguration, using the given SoundHandler.
   * @param sound the SoundHandler to use.
   */
  public AudioConfiguration(SoundHandler sound)
	{
	  _sound=sound;
		_query=null;
		_beep=null;
		_word=new Hashtable();
	}

  /**
   * Play the given sound.
   * @param snd the sound to be played.
   */
  public void play(String snd)
  {
    _sound.playSound(snd);
  }

  /**
   * Set the query sound.
   * @param snd sound name.
   */
  public void setQuery(String snd)
  {
    _query=snd;
  }

	/**
	 * Set the beep sound.
	 * @param snd sound name.
	 */
	public void setBeep(String snd)
	{
	  _beep=snd;
	}

  /**
   * Set the word sound.
   * @param word the word.
   * @param snd the sound to play for the given word.
   */
  public void setWord(String word,String snd)
  {
    _word.put(word,snd);
  }

  /**
   * Play the sound associated with the new query.
   */
	public void onQuery()
	{
    if(_query!=null) _sound.playSound(_query);
	}

	/**
	 * Play the beep sound.
	 */
	public void beep()
	{
	  if(_beep!=null) _sound.playSound(_beep);
	}

	/**
	 * Play the word sound.
	 * @param word word sound to play.
	 */
	public void onWord(String word)
	{
	  String snd=(String)_word.get(word);
	  if(snd!=null) _sound.playSound(snd);
  }

  /**
   * Get an enumeration of all know sound words.
   * @return an enumeration of string.
   */
  public Enumeration getSoundWords()
  {
    return _word.keys();
  }
}

