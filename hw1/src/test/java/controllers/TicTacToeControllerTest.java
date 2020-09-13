package controllers;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

class TicTacToeControllerTest {

	private Context ctx = mock(Context.class);
	
	private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create(); 
	
	private TicTacToeController tttcontroller;

	@BeforeEach
	void setController() {
		tttcontroller = new TicTacToeController();
	}

	@Test()
	@DisplayName("Invalid request; first player can only select 'X' or 'O'.")
	void POST_to_create_player_one_invalid_type() {

		when(ctx.formParam("type")).thenReturn("P"); // P is not a valid selection

		Assertions.assertThrows(BadRequestResponse.class, () -> {
			tttcontroller.startGame(ctx);
		});
	}

	@Test()
	@DisplayName("Valid request to create first player.")
	void POST_to_create_player_one_valid_type() {

		when(ctx.formParam("type")).thenReturn("X");

		tttcontroller.startGame(ctx);
		verify(ctx).status(200);
	}

	@Test()
	@DisplayName("Gameboard conversion to JSON failed to produce expected format.")
	void convert_gameboard_to_json() {

		JsonParser parser = new JsonParser();
		
		String boardAsJson = gson.toJson(tttcontroller.getGameBoard());

		String expectedBoardAsJson = "{\"gameStarted\":false,\"turn\":1,"
				+ "\"boardState\":[[\"\\u0000\",\"\\u0000\",\"\\u0000\"],"
				+ "[\"\\u0000\",\"\\u0000\",\"\\u0000\"],[\"\\u0000\",\"\\u0000\",\"\\u0000\"]],"
				+ "\"winner\":0,\"isDraw\":false}";

		assertEquals(parser.parse(expectedBoardAsJson), parser.parse(boardAsJson));
	}
}
