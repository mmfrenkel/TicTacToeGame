package controllers;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.http.Context;
import models.GameBoard;
import models.Player;

public class TicTacToeController {

	private GameBoard gameBoard;

	private static Logger logger = LoggerFactory.getLogger(PlayGame.class);
	
	public TicTacToeController() {
		this.gameBoard = new GameBoard();
	}
	
	public static Context serveNewGame(Context ctx) {
		logger.info("Received request to serve main page.");
		ctx.redirect("/tictactoe.html");
		return ctx;
    };
    
    public Context startGame(Context ctx) {
    	Player player1 = new Player('X', 1);
    	gameBoard.setP1(player1);
  
    	JSONObject boardAsJson = gameBoard.asJson();
    	ctx.result(boardAsJson.toString()).contentType("application/json");
    	return ctx;
    }

}
