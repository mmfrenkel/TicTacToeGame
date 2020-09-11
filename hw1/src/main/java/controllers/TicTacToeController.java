package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.http.Context;
import io.javalin.http.Handler;

import models.GameBoard;
import models.Player;

public class TicTacToeController {

	private GameBoard gameBoard;

	private static Logger logger = LoggerFactory.getLogger(PlayGame.class);
	
	public TicTacToeController() {
		this.gameBoard = new GameBoard();
	}
	
	public static Handler serveNewGame = ctx -> {
		logger.info("Received request to serve main page.");
		ctx.redirect("/tictactoe.html");
    };
    
    public Context startGame(Context ctx) {
    	Player player1 = new Player('X', 1);
    	
    	gameBoard.setPlayer1(player1);
    	
    	ctx.json(this.gameBoard); 
    	
    	return ctx;
    }

}
