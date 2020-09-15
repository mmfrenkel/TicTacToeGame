package controllers;

import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJson;
import models.Message;

import java.io.IOException;
import java.util.Queue;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
		TicTacToeController tttcontroller = new TicTacToeController();
		
		// configure controller to use Gson instead of Jackson for object->json mapping
		// JavalinJson.setToJsonMapper(gson::toJson);
		// JavalinJson.setFromJsonMapper(gson::fromJson);

		app = Javalin.create(config -> {
			config.addStaticFiles("/public");
			config.enableDevLogging();
		}).start(PORT_NUMBER);

		app.get("/", ctx -> {
			ctx.redirect("/newgame");
		});

		app.get("/newgame", ctx -> {
			tttcontroller.serveNewGame(ctx);
		});

		app.post("/startgame", ctx -> {
			tttcontroller.startGame(ctx);
		});

		app.get("/joingame", ctx -> {
			logger.info("Received request to add a second player.");
			tttcontroller.addSecondPlayer(ctx);
			sendGameBoardToAllPlayers(tttcontroller.getGameBoardAsJson());
		});

		app.post("/move/:playerId", ctx -> {
			tttcontroller.processPlayerMove(ctx);
			sendGameBoardToAllPlayers(tttcontroller.getGameBoardAsJson());
		});

		// Web sockets - DO NOT DELETE or CHANGE
		app.ws("/gameboard", new UiWebSocket());
	}

	/**
	 * Send message to all players.
	 * 
	 * @param gameBoardJson Game board JSON
	 * @throws IOException Web socket message send IO Exception
	 */
	private static void sendGameBoardToAllPlayers(final String gameBoardJson) {
		Queue<Session> sessions = UiWebSocket.getSessions();
		for (Session sessionPlayer : sessions) {
			try {
				sessionPlayer.getRemote().sendString(gameBoardJson);
			} catch (IOException e) {
				logger.error("Encountered exception sending game board to all players: ", e);
			}
		}
	}

	public static void stop() {
		app.stop();
	}
}
