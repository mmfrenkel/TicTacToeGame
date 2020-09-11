package controllers;

import models.GameBoard;

public class TicTacToeController {

	private GameBoard board;

	public TicTacToeController() {
		this.board = new GameBoard();
	}

	public void resetBoard() {
		this.board = new GameBoard();
	}
}
