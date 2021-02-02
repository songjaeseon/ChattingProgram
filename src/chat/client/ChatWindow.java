package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;

import chat.utils.StringUtil;

public class ChatWindow {

	private Socket socket;
	private String nickname;
	private Scanner scanner = new Scanner(System.in);

	public ChatWindow(Socket socket) {
		this.socket = socket;
	}

	/**
	 * chat을 시작한다.
	 */
	public void startChat() {
		inputNickname();
		new ChatClientReceiveThread(socket, nickname).start();
		inputMessage();
	}

	/**
	 * message를 입력한다. 입력한 message를 확인한다.
	 */
	private void inputMessage() {
		while (true) {
			String message = scanner.nextLine();

			if (StringUtil.equals(message, "")) {
				// 빈 문자일 경우 server에게 보내지 않고 continue한다.
			} else {
				String data = attachString(this.nickname, ":", message);
				sendMessage(data);
				// quit를 입력한 경우 프로그램 종료
				if (StringUtil.equals(message, "/q")) {
					StringUtil.consoleLog("채팅방을 나가셨습니다.");
					scanner.close();
					System.exit(0);
				}
			}
		}

	}

	/**
	 * nickname을 입력한다.
	 */
	private void inputNickname() {
		// nickname을 입력받도록 한다.
		try {
			StringUtil.consoleLog("nickname을 입력해주세요.");
			while (true) {
				nickname = scanner.nextLine();
				String pattern = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]*$";
				boolean result = Pattern.matches(pattern, nickname);

				if (nickname.length() == 0 || nickname.length() > 10 || !result) {
					// nickname에 들어가면 안되는 문자를 검증한다.
					StringUtil.consoleLog("1자이상 10자이하로 한글,영문,숫자 조합으로만 입력해주세요.");
				} else {
					// checkingnick을 통해서 받은 nickname이 존재한지 안한지 찾은 후에 옳고 그름을 찾는다.
					if (checkNick(socket, nickname)) {
						break;
					} else {
						StringUtil.consoleLog("다시 입력해주세요.");
					}
				}
			}
			StringUtil.consoleLog("채팅방에 입장하였습니다.");
		} catch (IOException e) {

		}
	}

	/**
	 * 입력받은 nickname 사용여부를 확인한다.
	 * 
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	private static boolean checkNick(Socket socket, String nickname) throws IOException {
		OutputStream out = socket.getOutputStream();
		PrintWriter writer = new PrintWriter(out, true);

		InputStream input = socket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));

		writer.println(nickname);
		String result = reader.readLine();

		// server에서 nickname여부를 판단한 값을 client에서 받아
		// 받은 값에 따라 동작을 다르게 수행한다.
		if (StringUtil.equalsIgnoreCase(result, "None")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * string 매개변수를 합친다.
	 * 
	 * @param strings
	 * @return
	 */
	private String attachString(String... strings) {
		StringBuilder sb = new StringBuilder();
		for (String s : strings) {
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * 입력된 message를 writer에 쓴다.
	 * 
	 * @param message
	 */
	private void sendMessage(String message) {
		try {
			OutputStream out = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(out, true);
			writer.println(message);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}