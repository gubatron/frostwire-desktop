package irc;

/**
 * The text provider.
 */
public interface TextProvider
{

  /**
   * First user message.
   */
  public static final int USER_BASE=0x8000;
  /**
   * Undefined string.
   */
  public static final int ERROR_NOT_DEFINED=0xffff;

	/**
	 * Get the formatted string.
	 * @param formattedCode string code.
	 * @param param parameters.
	 * @return formatted string.
	 */
	public String getString(int formattedCode,String param[]);

  /**
   * Get the formatted string.
   * @param code string code.
   * @return formatted string.
   */
	public String getString(int code);

  /**
   * Get the formatted string.
   * @param code string code.
   * @param param1 first parameter.
   * @return formatted string.
   */
  public String getString(int code,String param1);

  /**
   * Get the formatted string.
   * @param code string code.
   * @param param1 first parameter.
   * @param param2 second parameter.
   * @return formatted string.
   */
  public String getString(int code,String param1,String param2);

  /**
   * Get the formatted string.
   * @param code string code.
   * @param param1 first parameter.
   * @param param2 second parameter.
   * @param param3 third parameter.
   * @return formatted string.
   */
  public String getString(int code,String param1,String param2,String param3);

}

