package controllers;

import io.javalin.Javalin;
import java.io.IOException;
import java.util.Queue;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PlayGame {

	private static final int PORT_NUMBER = 8080;

	private static Javalin app;

	private static Logger logger = LoggerFactory.getLogger(PlayGame.class);

	/**
	 * Main method of the application.
	 * 
	 * @param args Command line arguments
	 */
	public static void main(final String[] args) {

		logger.info("Starting application...");
		
		app = Javalin.create(config -> {
			config.addStaticFiles("/public");
		}).start(PORT_NUMBER);

		// Test Echo Server
		app.post("/echo", ctx -> {
			ctx.result(ctx.body());
		});

		app.get("/", ctx -> {
			ctx.redirect("/tictactoe.html");
		});

		/**
		 * Please add your end points here.
		 * 
		 * 
		 * 
		 * 
		 * Please add your end points here.
		 * 
		 * 
		 * 
		 * 
		 * Please add your end points here.
		 * 
		 * 
		 * 
		 * 
		 * Please add your end points here.
		 * 
		 */

		// Web sockets - DO NOT DELETE or CHANGE
		app.ws("/gameboard", new UiWebSocket());
	}

	/**
	 * Send message to all players.
	 * 
	 * @param gameBoardJson Gameboard JSON
	 * @throws IOException Websocket message send IO Exception
	 */
	private static void sendGameBoardToAllPlayers(final String gameBoardJson) {
		Queue<Session> sessions = UiWebSocket.getSessions();
		for (Session sessionPlayer : sessions) {
			try {
				sessionPlayer.getRemote().sendString(gameBoardJson);
			} catch (IOException e) {
				// Add logger here
			}
		}
	}

	public static void stop() {
		app.stop();
	}
}
