package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import commoninterface.network.messages.EntitiesMessage;
import commoninterface.network.messages.Message;
import gui.panels.CommandPanel;

public class CommandSender extends Thread {

	private static int COMMAND_PORT = 10103;

	private ArrayList<String> ips;
	private Message message;
	private CommandPanel cp;
	private boolean dynamicActiveId = false;

	public CommandSender(Message message, ArrayList<String> ips, CommandPanel bp, boolean dynamicActiveId) {
		this.ips = ips;
		this.message = message;
		this.cp = bp;
		this.dynamicActiveId = dynamicActiveId;
	}

	@Override
	public void run() {

		SenderThread[] sts = new SenderThread[ips.size()];
		int i = 0;
		for (String ip : ips) {
			System.out.println(ip);
			try {
				sts[i] = new SenderThread(ip, i);
				sts[i].start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}

		int count = 0;

		for (SenderThread st : sts) {
			if (st == null)
				continue;
			try {
				st.join();
				count++;
				cp.setText("Deploying... " + count + "/" + sts.length);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		cp.setText("Deployed!");
	}

	class SenderThread extends Thread {

		private Socket socket;
		private Message myMessage;
		private String ip;

		public SenderThread(String ip, int number) throws IOException {
			socket = new Socket(ip, COMMAND_PORT);
			this.ip = ip;
			myMessage = message.getCopy();

			if (dynamicActiveId && myMessage instanceof EntitiesMessage) {
				((EntitiesMessage) myMessage).setActiveId(number);
			}
		}

		@Override
		public void run() {
			try {
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

				Message m = myMessage.getCopy();
				synchronized (this) {
					out.writeObject(m);
					out.reset();
					out.flush();
					Object o = in.readObject();

					if (o == null || !(o instanceof Message)) {
						System.out.printf("[%s] Didn't receive the right confirmation!", getClass().getName());
					}
				}

				out.close();
				in.close();
				socket.close();

			} catch (Exception e) {
				System.out.printf("[%s] PROBLEM IN IP %s%n", getClass().getName(), ip);
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}