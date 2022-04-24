package rl.main;

public class ValidationEception extends Exception {

    private final String column;
    private final Object value;
    private final String error;

    public ValidationEception(String col, Object value, String error) {
        this.column = col;
        this.value = value;
        this.error = error;
    }

    public String getColumn() {
        return column;
    }

    public Object getValue() {
        return value;
    }

    public String getError() {
        return error;
    }


}
