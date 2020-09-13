package controllers;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.JSONObject;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class TicTacToeControllerTest {

	private Context ctx = mock(Context.class);
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

		JSONObject boardAsJson = tttcontroller.gameBoardToJSON();

		String expected = "{\"winner\":0,\"boardState\":" + "[[\"\\u0000\",\"\\u0000\",\"\\u0000\"],[\"\\u0000\","
				+ "\"\\u0000\",\"\\u0000\"],[\"\\u0000\",\"\\u0000\",\"\\u0000\"]],"
				+ "\"gameStarted\":false,\"turn\":1,\"isDraw\":false}";

		JSONAssert.assertEquals(expected, boardAsJson, false);
	}

}
