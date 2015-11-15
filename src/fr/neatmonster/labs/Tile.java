package fr.neatmonster.labs;

public enum Tile {
    // @formatter:off
    TILE0(0,0,1,1),
    TILE1(1,0,1,1),
    TILE2(1,-1,1,1),
    TILE3(0,-1,1,1),
    TILE4(-1,-1,1,1),
    TILE5(-1,0,1,1),
    TILE6(-1,1,1,1),
    TILE7(0,1,1,1),
    TILE8(1,1,1,1),
    TILE9(2,1,1,1),
    TILE10(2,0,1,1),
    TILE11(2,-1,1,1),
    TILE12(2,-2,1,1),
    TILE13(1,-2,1,1),
    TILE14(0,-2,1,1),
    TILE15(-1,-2,1,1),
    TILE16(-2,-2,1,1),
    TILE17(-2,-1,1,1),
    TILE18(-2,0,1,1),
    TILE19(-2,1,1,1),
    TILE20(-2,2,1,1),
    TILE21(-1,2,1,1),
    TILE22(0,2,1,1),
    TILE23(1,2,1,1),
    TILE24(2,2,1,1),
    TILE25(3,1,2,2),
    TILE26(3,-1,2,2),
    TILE27(3,-3,2,2),
    TILE28(3,-5,2,2),
    TILE29(1,-4,2,2),
    TILE30(-1,-4,2,2),
    TILE31(-3,-4,2,2),
    TILE32(-5,-4,2,2),
    TILE33(-4,-2,2,2),
    TILE34(-4,0,2,2),
    TILE35(-4,2,2,2),
    TILE36(-4,4,2,2),
    TILE37(-2,3,2,2),
    TILE38(0,3,2,2),
    TILE39(2,3,2,2),
    TILE40(4,3,2,2),
    TILE41(5,1,2,2),
    TILE42(5,-1,2,2),
    TILE43(5,-3,2,2),
    TILE44(1,-6,2,2),
    TILE45(-1,-6,2,2),
    TILE46(-3,-6,2,2),
    TILE47(-6,-2,2,2),
    TILE48(-6,0,2,2),
    TILE49(-6,2,2,2),
    TILE50(-2,5,2,2),
    TILE51(0,5,2,2),
    TILE52(2,5,2,2),
    TILE53(4,5,6,5),
    TILE54(7,-3,3,8),
    TILE55(5,-9,5,6),
    TILE56(-3,-9,8,3),
    TILE57(-9,-9,6,5),
    TILE58(-9,-4,3,8),
    TILE59(-9,4,5,6),
    TILE60(-4,7,8,3);
    // @formatter:on

    public static enum TileVal {
        // @formatter:off
        ENEMY(1, -100),
        STOMPABLE_ENEMY(2, -50),
        EMPTY_CELL(8, 0),
        LEVEL_OBJECT(5, 30),
        COIN(4, 70),
        POWER_UP(3, 100);
        // @formatter:on

        public int priority;
        public double value;

        private TileVal(final int priority, final double value) {
            this.priority = priority;
            this.value = value;
        }
    }

    public final int x;
    public final int y;
    public final int w;
    public final int h;

    private Tile(final int x, final int y, final int w, final int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public boolean contains(final int x, final int y) {
        return this.x <= x && x <= this.x + w && this.y <= y && y <= this.y + h;
    }
}
