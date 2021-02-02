package chat.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import chat.utils.StringUtil;

public class ChatClientApp {
	public static void main(String[] args) {

		// socket을 생성하여
		// server와의 통신을 위해서 지정한 ip와 port를 connect함수로 server와 연결한다.
		// 채팅방 입장 문구를 띄우고 chatwindow의 show함수를 실행하게된다.
		Socket socket = null;
		Scanner scanner = new Scanner(System.in);

		try {
			// client가 실행되면 ip와 port를 입력받도록 한다.
			while (true) {
				StringUtil.consoleLog("IP를 입력해주세요");
				String CLIENT_IP = scanner.nextLine();
				StringUtil.consoleLog("PORT를 입력해주세요 (0 ~ 65535범위안에서 입력해주세요)");
				String CLIENT_PORT = scanner.nextLine();

				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress(CLIENT_IP, Integer.parseInt(CLIENT_PORT)));
					if (socket.isConnected()) {
						break;
					}
				} catch (SocketException se) {
					// socket이 close인 경우
					StringUtil.consoleLog("닫혀있는 socket입니다. 다시 입력해주세요.");
					socket.close();
				} catch (UnknownHostException uhe) {
					// 해당 ip를 찾지못한 경우
					StringUtil.consoleLog("입력하신 ip를 찾지 못했습니다. 다시 입력해주세요.");
				} catch (IllegalArgumentException ia) {
					// port넘버 범위 초과일 경우
					StringUtil.consoleLog("잘못된 port번호 입니다.");
				}
			}
			
			// 채팅방에 입장하게 되면 show함수를 실행하여 입력을 할 수 있도록 한다.
			new ChatWindow(socket).startChat();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
