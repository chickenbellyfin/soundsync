package soundsync;

public class Command {
	
	public static final String PROTOCOL_VERSION = "1";
	
	public static final String CMD_DELIM = "|";
	public static final String CMD_DELIM_REGEX = "\\" + Command.CMD_DELIM;
	
	public static final String CLIENT_PLAY = "PLAY";
	public static final String CLIENT_STOP = "STOP";
	public static final String CLIENT_LOAD = "LOAD";
	public static final String CLIENT_CLEAR_QUEUE = "CLRQ";
	public static final String CLIENT_TIME = "TIME";
	public static final String CLIENT_ADD = "ADDURL";
	public static final String CLIENT_REMOVE = "DELURL";
	
	public static final String SERVER_READY = "READY";
	public static final String SERVER_ADD = "ADD";
	
	public static final String PING = "PING";
	public static final String GOOD = "GOOD";
	public static final String BAD = "BAD";
	
	public static String formatCmd(String cmd, Object... args) {
		String s = cmd;
		for (Object arg : args) {
			s += Command.CMD_DELIM + arg.toString();
		}
		return s;
	}
	
}
