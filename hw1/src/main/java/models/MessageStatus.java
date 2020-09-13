package models;

/*
 * Idea for enum implementation from Bhesh Gurung at 
 * https://stackoverflow.com/questions/8811815/is-it-possible-to-assign-numeric-value-to-an-enum-in-java
 */
public enum MessageStatus {
    SUCCESS(100), 
    POSITION_NOT_ALLOWED(410), 
    WINNING_MOVE(110); 

    private int value;

    MessageStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
