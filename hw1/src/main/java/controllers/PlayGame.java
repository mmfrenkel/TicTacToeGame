package controllers;

import io.javalin.Javalin;
import java.io.IOException;
import java.util.Queue;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DbServiceException;
import util.TicTacToeSqliteDbService;

public class PlayGame {

  private static final int PORT_NUMBER = 8080;

  private static Javalin app;
  
  private static TicTacToeController tttcontroller;
  
  private static TicTacToeSqliteDbService dbService;

  private static Logger logger = LoggerFactory.getLogger(PlayGame.class);

  /**
   * Main method of the application.
   * 
   * @param args Command line arguments
   * @throws DbServiceException  when there is an issue establishing the database
   */
  public static void main(final String[] args) throws DbServiceException {

    logger.info("Starting application...");

    dbService = new TicTacToeSqliteDbService();
    
    logger.info("Creating database tables, if necessary...");
    dbService.createDatabasesTables();
    
    tttcontroller = new TicTacToeController();

    app = Javalin.create(config -> {
      config.addStaticFiles("/public");
      config.enableDevLogging();
    }).start(PORT_NUMBER);
    
    app.before(ctx -> {
      tttcontroller.loadGameBoard();
    });

    app.get("/", ctx -> {
      ctx.redirect("/newgame");
    });

    app.get("/newgame", ctx -> {
      logger.info("Received request to start a new game. This will reset the game board.");
      tttcontroller.serveNewGame(ctx);
    });

    app.post("/startgame", ctx -> {
      logger.info("Received request to add a first player.");
      tttcontroller.startGame(ctx);
    });

    // Warning: Often takes a long time for the web page for Player 2 to fully
    // resolve itself and show that Player 1 has the first move
    app.get("/joingame", ctx -> {
      logger.info("Received request to add a second player.");
      tttcontroller.addSecondPlayer(ctx);
      sendGameBoardToAllPlayers(tttcontroller.getGameBoardAsJson());
    });

    app.post("/move/:playerId", ctx -> {
      logger.info("Received request to log a move for a player.");
      tttcontroller.processPlayerMove(ctx);
      sendGameBoardToAllPlayers(tttcontroller.getGameBoardAsJson());
    });
    
    app.get("/gameboardstatus", ctx -> {
      ctx.result(tttcontroller.getGameBoardAsJson());
    });
    
    app.get("/test", ctx -> {
      ctx.status(200);
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
