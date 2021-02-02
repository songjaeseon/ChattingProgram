package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.regex.Pattern;

import chat.utils.StringUtil;

public class ChatWindow {

	private Socket socket;
	private String nickname;
	Scanner scanner = new Scanner(System.in);

	// 생성자를 거치면서
	// chatclientreceiveThread를 start하게 된다.
	public ChatWindow(Socket socket) {
		this.socket = socket;
	}

	public void readyChat() {
		inputNickname();
		new ChatClientReceiveThread(socket, nickname).start();
		messageInput();
	}

	/**
	 * message를 입력한다. 입력한 message를 확인한다.
	 */
	private void messageInput() {
		Scanner scanner = new Scanner(System.in);

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
					if (checkingNick(socket, nickname)) {
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
	private static boolean checkingNick(Socket socket, String nickname) throws IOException {
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

	/**
	 * Server에서 받은 message를 console창에 출력한다.
	 * 
	 * @author 82107
	 *
	 */
	private class ChatClientReceiveThread extends Thread {
		Socket socket = null;
		String sendNickname = null;

		ChatClientReceiveThread(Socket socket, String nickname) {
			this.socket = socket;
			this.sendNickname = nickname;
		}

		/**
		 * Server로부터 받은 message를 출력한다.
		 */
		@Override
		public void run() {
			try {
				InputStream input = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));

				String recieveNickname = null;
				String message = null;

				// msg를 읽어 다른 client로부터 입력받은 값을 자신의 console창에 띄우고
				// 자신이 보낸 값인 경우는 받지 않도록 한다.
				while (true) {
					String msg = reader.readLine();

					String[] data = msg.split(":");
					if (data[0] != null) {
						recieveNickname = data[0];
					}
					if (data.length > 1 && data[1] != null) {
						message = data[1];
					}
					if (StringUtil.equals(recieveNickname, "/p")) {
						// 현재 인원수를 나의 console창에 띄운다.
						StringUtil.consoleLog(message);
					} else if (isEqualMymessage(recieveNickname, sendNickname) && sendNickname != null) {
						// 내가 보낸 message를 다시 받지 않도록 한다.
						StringUtil.consoleLog(msg);
					} else {
						continue;
					}
				}
			} catch (SocketException se) {
				StringUtil.consoleLog("server가 종료되었습니다. 프로그램을 닫습니다.");
				System.exit(0);
			} catch (IOException e) {
				StringUtil.consoleLog("server와의 연결이 끊어졌습니다. 프로그램을 닫습니다.");
				System.exit(0);
				// e.printStackTrace();
			}
		}

		/**
		 * message를 보낸 nickname과 현재 client의 nickname을 비교한다.
		 * 
		 * @param recieveNickname
		 * @param sendNickname
		 * @return
		 */
		private boolean isEqualMymessage(String recieveNickname, String sendNickname) {
			return !StringUtil.equals(recieveNickname, sendNickname);
		}
	}

}