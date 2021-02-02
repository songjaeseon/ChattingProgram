package chat.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {

	/**
	 * string 매개변수를 합친 후 반환한다.
	 * 
	 * @param strings
	 * @return
	 */
	public static String attachString(String... strings) {
		StringBuilder sb = new StringBuilder();
		for (String s : strings) {
			sb.append(s);
		}
		
		return sb.toString();
	}

	/**
	 * console창에 출력한다.
	 * 
	 * @param log
	 */
	public static void serverconsoleLog(String... strings) {
		Date from = new Date();
		SimpleDateFormat transFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
		String date = transFormat.format(from);

		String serverConsole = attachString(date, attachString(strings));
		consoleLog(serverConsole);
	}

	public static void consoleLog(String log) {
		System.out.println(log);
	}

	public static void consoleLog(String... strings) {
		consoleLog(attachString(strings));
	}

	public static boolean equals(String m1, String m2) {
		if (m1 == null || m2 == null) {
			return false;
		} else if (m1.equals(m2)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean equalsIgnoreCase(String m1, String m2) {
		if (m1 == null || m2 == null) {
			return false;
		} else if (m1.toLowerCase().equals(m2.toLowerCase())) {
			return true;
		} else {
			return false;
		}
	}
}
