package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import chat.utils.StringUtil;

public class ChatClientReceiveThread extends Thread {
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
