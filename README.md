#  Tic-Tac-Toe Web Application

## About Tic-Tac-Toe
The project enables a simple tic-tac-toe game between two users. Tic-tac-toe is a game where two players compete to fulfill a row, column or horizontal position on a 3 x 3 board with their game piece, either an 'X' or an 'O'. Player's alternate taking turns, trying to strategically position their moves so that they get closer to winning without the other player winning first. The first complete a row, column or horizontal wins.

The following rules also apply:
* The game cannot start until there are two players.
* Player 1 must always make the first move.
* Players cannot make a move if it is not their turn.
* Players cannot play a position that is already occupied.
* Moves must be made within the board limits and only with player type X and O.

## Application Design

This project utiltizes a Model-View-Controller architecture. HTML, CSS and JavaScript support the Views in the front-end of this application. In the backend, Models and Controllers are supported by the lightweight web framework Javalin using the Java programming language. The project uses Maven for build support and dependency management.

The project includes the following endpoints, which can be utilized for testing purposes or for API interaction with the game:
* `GET /newgame`: Resets the board to start a new game and redirects user to `tictactoe.html`.
* `POST /startgame`: Adds player 1 to the gameboard, with the type specified by the `type` parameter passed within the request body (e.g., `type=O` or `type=X`). Returns the gameboard configuration as JSON, in the format below:
```
{
  "p1": {
    "type": "X",
    "id": 1
  },
  "gameStarted": false,
  "turn": 1,
  "boardState": [
    [
      "\u0000",
      "\u0000",
      "\u0000"
    ],
    [
      "\u0000",
      "\u0000",
      "\u0000"
    ],
    [
      "\u0000",
      "\u0000",
      "\u0000"
    ]
  ],
  "winner": 0,
  "isDraw": false
}
```
If player 1 already exists on the gameboard then the user is notified that they need to go to either `/joingame`, ask Player 1 for the join url, or start a new game `/newgame`.

* `GET /joingame`: Allows player 2 to join the gameboard, assigns whatever piece player 1 did not take, and redirects player 2 to their game board. This offically allows the game to commence, as the updated gameboard configuration is broadcast to both users. If there are already 2 players on the board, the user will be notified that the game board is full and they should visit `/newgame` to start a new game.
* `POST /move/:playerId`: Allows a player specified by their `playerId` to make a move on the gameboard, where the move itself is specified in the following format:`x=0&y=0`, where this specifies a move to (0, 0). Once the player has made a move, erronous moves are reported back to the user and updates to the gameboard configuration are broadcast to both users.

Note that the gameboard state is saved after every request to a SQLite database to allow users to resume their game if the server crashes for any reason.

## Development

This project was developed on macOS Catalina (Version 10.15.6) with IDE support from Eclipse using Java 11.0.2. The UI was tested using Postman and both Safari and Chrome as web browsers.

You can get started working with this project via:
```
$ mvn clean
$ mvn install
```
Note that the `install` step will also the tests outlined below.

To run this project, there is no need to install a server; it is easiest to run the program simply by right-clicking on the `PlayGame.java` file in Eclipse and running it as a Java application.

There is a test suite built for this project, which includes both Junit unit tests and Unirest-Java API integration tests. Both sets of tests can be run in series by issuing the following command in the command line (or by using the Eclipse UI):
```
$ mvn test
```
All tests (currently 121 total, 103 unit tests and 18 integration tests) should pass. Code testing coverage was checked using the Emma plugin in Eclipse and was calculated as 97%. The remaining 3% is (a) code that is either (1) unreachable (not possible to test) or (b) template code and/or previously implemented code as part of this assignment (i.e., web socket code).

This project uses Checkstyle as a tool for ensuring that this code adheres to industry coding standards. Run the checkstyle tool by issuing the command below and reviewing the output found within `target/site/images/checkstyle.html`.
```
$ mvn checkstyle:checkstyle
```
Finally, this project utilized the SpotBugs plugin in Eclipse to identify any instances in this code base that have bug patterns. "Spot bugs" detected were resolved only in the main source code; warnings flagged in the test directory were ignored. You can generate a SpotBugs report at any time, available in `target/site/images/spotbugs.html` , by issuing:
```
$ mvn site
```
Note that sometimes the SpotBug report doesn't generate until the  `mvn install` step is conducted.

## Credits

The conceptualization and starter code for this project, including all of the HTML, CSS and JS code as well as code for web socket interaction and model/controller templates, were provided by Shirish Singh and Professor Gail Kaiser as part of the Fall 2020 Advanced Software Engineering course at Columbia University (COMS 4156).

