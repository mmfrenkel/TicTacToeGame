# Public Assignment for COMS W4156

### Tic-Tac-Toe
The project enables a simple tic-tac-toe game between two users. Tic-tac-toe is a game where two players compete to fulfill a row, column or horizontal position on a 3 x 3 board with their game piece, either an 'X' or an 'O'. Player's alternate taking turns, trying to strategically position their moves so that they get closer to winning without the other player winning first. The first complete a row, column or horizontal wins.

The following rules also apply:
* The game cannot start until there are two players.
* Player 1 must always make the first move.
* Players cannot make a move if it is not their turn.
* Players cannot play a position that is already occupied.
* Moves must be made within the board limits and only with player type X and O.

### Design

This project utiltizes a Model-View-Controller architecture. HTML, CSS and JavaScript support the Views in the front-end of this application. In the backend, Models and Controllers are supported by the lightweight web framework Javalin using the Java programming language. The project uses Maven for build support and dependency management.

The project includes the following endpoints, which can be utilized for testing purposes or for API interaction with the game:
* `GET /newgame`: Resets the board to start a new game.
* `POST /startgame`: Adds player 1 to the gameboard, with the type specified by the `type` parameter passed within the request body.
* `GET /joingame`: Allows player 2 to join the gameboard, assigned whatever piece player 1 did not take. This offically allows the game to commence. 
* `POST /move/:playerId`: Allows a player specified by their `playerId` to make a move on the gameboard. 

### Development

This project was developed on macOS Catalina (Version 10.15.6) with IDE support from Eclipse using Java 11.0.2.

### Credits

Starter code for this project, including all of the HTML, CSS and JS code as well as code for web socket interaction and model/controller templates, was provided by Shirish Singh and Professor Gail Kaiser as part of the Fall 2020 Advanced Software Engineering course at Columbia University (COMS 4156). 

