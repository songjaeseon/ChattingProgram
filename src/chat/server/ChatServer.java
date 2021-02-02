package chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.utils.StringUtil;

public class ChatServer {

	public static void main(String[] args) {
		
		// serverSocket을 사용해서 서버의 특정포트에서 클라이언트의 연결요청을 처리할 준비를 한다.
		// List를 통해서 현재 통신중인 client들에 대해서 다룬다.
		List<PrintWriter> listWriters = new ArrayList<PrintWriter>();
		Map<String, PrintWriter> map = new HashMap<String, PrintWriter>();

		// try-resource
		// try블럭을 나갈때 자동으로 closed해준다.
		try (ServerSocket serverSocket = new ServerSocket();) {
			String IP = InetAddress.getLocalHost().getHostAddress();
			int PORT = 5000;
			
			serverSocket.bind(new InetSocketAddress(IP, PORT));
			StringUtil.serverconsoleLog("연결 기다림-", IP, ":", Integer.toString(PORT));

			// client에서 connect하기전까지 대기시간을 갖는다.
			// 무한반복과 new thread를 통해서 다수의 client를 받을 수 있도록 한다.
			// 하나의 serverthread당 하나의 client
			while (true) {
				Socket socket = serverSocket.accept();
				StringUtil.serverconsoleLog("Client nickname 입력 대기중...");
				new ChatServerProcessThread(socket, listWriters, map).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("서버 연결 실패. [message: " + e.getMessage());
		}
	}
}