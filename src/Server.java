import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;

public class Server {
	double tickspeed = 30;
	GameState gamestate;
	private boolean visual = true;
	int width = 800, height = 600;
	
	JFrame serverFrame;
	ServerPanel serverPanel;
	ServerSocket serverSocket;
	ArrayList<Connection> connections;
	
	public static void main(String[] args) {
		new Server();
	}
	
	Server() {
		gamestate = new GameState();
		gamestate.generatePlayers();
		connections = new ArrayList<Connection>();
		
		if(visual) {
			serverFrame = new JFrame();
			serverFrame.setSize(width, height);
			serverFrame.setResizable(false);
			serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			serverFrame.setAlwaysOnTop(true);
			serverFrame.setVisible(true);
			serverPanel = new ServerPanel(gamestate, width, height);
			serverFrame.add(serverPanel);
			serverPanel.setPreferredSize(new Dimension(width,height));
			serverFrame.pack();
		}

		try {
			serverSocket = new ServerSocket(8000);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		new Thread(new Connector(this)).start();
		
		// Gameloop
		long time = System.nanoTime();
		boolean running = true;
		while(running) {
			if(visual)
				serverPanel.repaint();
			long newtime = System.nanoTime();
			double delta = (newtime - time) / 1000000000;
			
			if (delta < 1/tickspeed) {
				this.update(1/tickspeed);
				try {
					Thread.sleep((long) ((1/tickspeed - delta)*1000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				this.update(delta);
			}
			time = System.nanoTime();
		}
	}
	
	private void update(double delta) {
		for (Player player : gamestate.players) {
			player.update(delta);
		}
		for (Player player : gamestate.players) {
			player.attack(delta, gamestate.players);
		}
		for (Player player : gamestate.players) {
			player.units.removeIf(unit -> unit.health <= 0);
		}
	}
	
	class Connector implements Runnable {
		
		Server server;
		
		Connector(Server server) {
			this.server = server;
		}

		@Override
		public void run() {
			while(true) {
				try {
					Socket socket = serverSocket.accept();
					Connection connection = new Connection(socket, server);
					connections.add(connection);
					new Thread(connection).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class Connection implements Runnable {

		Socket socket;
		DataInputStream fromClient;
		DataOutputStream toClient;
		ObjectOutputStream objectToClient;
		Server server;
		
		Connection(Socket socket, Server server) {
			this.socket = socket;
			this.server = server;
		}
		
		@Override
		public void run() {
			try {
				fromClient = new DataInputStream(socket.getInputStream());
				toClient = new DataOutputStream(socket.getOutputStream());
				objectToClient = new ObjectOutputStream(toClient);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				while (true) {
					objectToClient.writeUnshared(server.gamestate);
					objectToClient.flush();
					objectToClient.reset();
					Thread.sleep((int)(1/tickspeed*1000));
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}
