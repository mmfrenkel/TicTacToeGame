package unit.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import models.Message;
import models.MessageStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MessageTest {

  @Test
  @DisplayName("Message Constructor #1 should assign moveValidity")
  void testCreateMessage1() {

    Message msg = new Message(false, 200, "this is a test message");
    assertEquals(false, msg.isMoveValidity());
  }

  @Test
  @DisplayName("Message Constructor #1 should assign code")
  void testCreateMessage2() {

    Message msg = new Message(false, 200, "this is a test message");
    assertEquals(200, msg.getCode());
  }

  @Test
  @DisplayName("Message Constructor #1 should assign message")
  void testCreateMessage3() {

    Message msg = new Message(false, 200, "this is a test message");
    assertEquals("this is a test message", msg.getMessage());
  }

  @Test
  @DisplayName("Message Constructor #2 should be able to assign "
      + "code from MessageStatus")
  void testCreateMessage4() {

    Message msg = new Message(false, MessageStatus.SUCCESS, 
        "this is a test message");
    assertEquals(100, msg.getCode());
  }

  @Test
  @DisplayName("setMoveValidity() on Message instance should correctly "
      + "set validity of move")
  void testSetMoveValidity() {

    Message msg = new Message(false, 200, "this is a test message");
    msg.setMoveValidity(true);
    assertEquals(true, msg.isMoveValidity());
  }

  @Test
  @DisplayName("setCode() on Message instance should correctly set code of "
      + "result for move")
  void testSetMoveCode() {

    Message msg = new Message(false, 200, "this is a test message");
    msg.setCode(100);
    assertEquals(100, msg.getCode());
  }
  
  @Test
  @DisplayName("setMessage() on Message instance should correctly set message of "
      + "result for move")
  void testsetMessage() {

    Message msg = new Message(false, 200, "this is a test message");
    msg.setMessage("here is another message!");
    assertEquals("here is another message!", msg.getMessage());
  }

  @Test
  @DisplayName("toSting() method for Message should reflect the state of "
      + "the Message instance")
  void testToString() {

    Message msg = new Message(false, 200, "this is a test message");
    String expected = "Message [moveValidity=false, code=200, " 
        + "message=this is a test message]";
    assertEquals(expected, msg.toString());
  }
}