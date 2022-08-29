package hiccup.hiccupstore.board.util;


public enum BoardCategory {
    REVIEW("review",1),
    PRODUCT("product",2),
    SYSTEM("system",3);

    private final String valueString;
    private final int valueNum;

    BoardCategory(String valueString, int valueNum) {
        this.valueString = valueString;
        this.valueNum = valueNum;
    }
    public String getValueString() {
        return this.valueString;
    }
    public int getValueNum() {
        return this.valueNum;
    }

}