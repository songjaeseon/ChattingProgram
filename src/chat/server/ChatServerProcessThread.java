package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.utils.StringUtil;

public class ChatServerProcessThread extends Thread {
	private Socket socket = null;
	private String nickname = null;
	List<PrintWriter> listWriters = null;
	HashMap<String, PrintWriter> map = null;
	Map<String, PrintWriter> wisperMode = new HashMap<String, PrintWriter>();
	StringBuilder sb = null;

	/**
	 * 
	 * @param socket
	 * @param listWriters
	 * @param map
	 */
	public ChatServerProcessThread(Socket socket, List<PrintWriter> listWriters, Map<String, PrintWriter> map) {
		this.socket = socket;
		this.listWriters = listWriters;
		this.map = (HashMap<String, PrintWriter>) map;
	}

	/**
	 * 채팅을 하기전 필요한 과정을 거친다.
	 */
	@Override
	public void run() {
		PrintWriter writer = null;
		try {

			// InputStream - 클라이언트에서 보낸 메세지 읽기위해 쓰인다.
			InputStream input = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));

			// OutputStream - 서버에서 클라이언트로 메세지 보내기위해 쓰인다.
			OutputStream out = socket.getOutputStream();
			writer = new PrintWriter(out, true);

			// nickname check후에 채팅을 쓸 수 있다.
			nickname = checkNick(socket, map);

			// nickname의 중복여부를 확인 후에 입장문구를 다른 client에게 알린다.
			doMessage(nickname, "님이 채팅방에 입장했습니다.");

			// list에 writer추가한다.
			addWriter(writer);

			// client로부터 msg를 받아서 입력값에 따른 동작을 수행한다.
			while (true) {
				String request = reader.readLine();
				String data[] = request.split(":");

				String nickname = data[0];
				String message = data[1];

				if (nickname != null && message != null) {
					if (StringUtil.equals(message, "/q")) {
						// client에서 종료를 원한 경우
						doQuit(writer);
						break;
					} else if (StringUtil.equals(message, "/p")) {
						// 현재 인원 출력
						sendCurrentPerson(request, map);
					} else {
						// message 전달
						StringUtil.serverconsoleLog(request);
						doMessage(request);
					}
				}

			}

		} catch (IOException e) {
			// client에서 강제로 종료한 경우
			doQuit(writer);
		}
	}

	/**
	 * nickname의 존재여부를 확인한다.
	 * 
	 * @param socket
	 * @param map
	 * @return
	 * @throws IOException
	 */
	private String checkNick(Socket socket, Map<String, PrintWriter> map) throws IOException {
		InputStream input = socket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));

		OutputStream out = socket.getOutputStream();
		PrintWriter writer = new PrintWriter(out, true);

		String nick;

		while (true) {
			nick = reader.readLine();

			// client에서 입력한 nickname이 존재여부를 다시 client에게 알려
			// 존재하는 nickname이라면 재입력을 요구하고
			// 존재하지 않는 nickname이라면 server에 채팅문구를 띄운다.
			if (!map.containsKey(nick)) {
				map.put(nick, writer);
				writer.println("None");

				String data = StringUtil.attachString(nick, "님이 채팅방에 들어오셨습니다.");
				StringUtil.serverconsoleLog(data);
				break;
			} else {
				writer.println("Exist");
			}
		}
		return nick;

	}


	/**
	 * 모든 client에 퇴장문구를 띄운다.
	 * 
	 * @param writer
	 */
	private void doQuit(PrintWriter writer) {
		removeWriter(writer);
		map.remove(nickname);

		if (nickname == null) {
			// nickname 입력 과정중에 강제종료한 경우 문구를 띄우지 않고 종료
		} else {
			String data = StringUtil.attachString(nickname, "님이 퇴장했습니다.");
			StringUtil.serverconsoleLog(data);
			doMessage(data);
		}
	}

	/**
	 * writer를 삭제한다.
	 * 
	 * @param writer
	 */
	private void removeWriter(PrintWriter writer) {
		synchronized (listWriters) {
			listWriters.remove(writer);
		}
	}

	/**
	 * List에 현재 writer를 추가한다.
	 * 
	 * @param writer
	 */
	private void addWriter(PrintWriter writer) {
		synchronized (listWriters) {
			listWriters.add(writer);
		}

	}

	/**
	 * 메시지값을 broadcast함수에 넘긴다.
	 * 
	 * @param request
	 */
	private void doMessage(String... request) {
		String data = StringUtil.attachString(request);
		broadcast(data);
	}

	/**
	 * 모든 client에게 메시지를 보낸다.
	 * 
	 * @param request
	 */
	private void broadcast(String request) {
		synchronized (listWriters) {
			PrintWriter writer = null;

			for (int i = 0; i < listWriters.size(); i++) {
				writer = listWriters.get(i);
				writer.println(request);
				writer.flush();

			}
		}
	}

	/**
	 * 현재 인원수를 조회한다.
	 * 
	 * @param request
	 * @param map
	 */
	private void sendCurrentPerson(String request, Map<String, PrintWriter> map) {
		String[] data = request.split(":");
		String nickname = data[0];
		String message = data[1];

		if (nickname != null && message != null) {
			String currentPerson = StringUtil.attachString("/p:현재 채팅방에 ", Integer.toString(map.size()), "명이 참여중입니다.");
			StringUtil.serverconsoleLog(currentPerson);
			unicast(currentPerson, map.get(nickname));
		}
	}

	/**
	 * 명령어를 입력한 client에게만 정보를 제공한다.
	 * 
	 * @param currentPerson
	 * @param writer
	 */
	private void unicast(String request, PrintWriter writer) {
		writer.println(request);
		writer.flush();
	}

}
