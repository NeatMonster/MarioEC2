package fr.neatmonster.labs;

import java.util.Arrays;
import java.util.Random;

import com.mojang.mario.LevelScene;
import com.mojang.mario.MarioComponent;
import com.mojang.mario.level.Level;
import com.mojang.mario.sprites.BulletBill;
import com.mojang.mario.sprites.Enemy;
import com.mojang.mario.sprites.FireFlower;
import com.mojang.mario.sprites.Mario;
import com.mojang.mario.sprites.Mushroom;
import com.mojang.mario.sprites.Shell;
import com.mojang.mario.sprites.Sprite;

import fr.neatmonster.labs.Tile.TileVal;
import fr.neatmonster.neato.Individual;

@SuppressWarnings("serial")
public abstract class MarioEC2 extends MarioComponent {
    public static final int LEVELS     = 20;
    public static final int DIFFICULTY = 3;

    public static final Random RANDOM = new Random();

    public Individual creature;
    public int        nextLevel = 0;
    public double     dist      = 0.0;
    public double     time      = 0.0;

    public MarioEC2() {
        super(640, 480);
    }

    public void evaluate() {
        final double[] input = getInput();
        creature.setInput(input);
        creature.feedForward();

        final double[] output = creature.getOutput();
        scene.toggleKey(Mario.KEY_JUMP, output[0] > 0.5);
        scene.toggleKey(Mario.KEY_SPEED, output[1] > 0.5);
        scene.toggleKey(Mario.KEY_UP, output[2] > 0.5);
        scene.toggleKey(Mario.KEY_DOWN, output[3] > 0.5);
        scene.toggleKey(Mario.KEY_LEFT, output[4] > 0.5);
        scene.toggleKey(Mario.KEY_RIGHT, output[5] > 0.5);
    }

    public double[] getFitness() {
        final double[] fitness = new double[5];
        fitness[0] = dist;
        fitness[1] = kills;
        fitness[2] = coins;
        fitness[3] = damage;
        fitness[4] = time;
        return fitness;
    }

    public double[] getInput() {
        final TileVal[] receptField = new TileVal[61];
        Arrays.fill(receptField, TileVal.EMPTY_CELL);

        final LevelScene ls = (LevelScene) scene;
        final Level level = ls.level;

        final int xMario = (int) (ls.mario.x / 16);
        final int yMario = (int) (ls.mario.y / 16);

        for (final Sprite sprite : ls.sprites) {
            final int xSprite = (int) (sprite.x / 16);
            final int ySprite = (int) (sprite.y / 16);

            final int x = xSprite - xMario;
            final int y = ySprite - yMario;

            TileVal value = TileVal.EMPTY_CELL;
            if (sprite instanceof BulletBill) {
                final BulletBill bill = (BulletBill) sprite;
                if (!bill.dead)
                    value = TileVal.STOMPABLE_ENEMY;
            } else if (sprite instanceof Enemy) {
                final Enemy enemy = (Enemy) sprite;
                if (enemy.deadTime == 0)
                    switch (enemy.type) {
                    case Enemy.ENEMY_RED_KOOPA:
                    case Enemy.ENEMY_GREEN_KOOPA:
                    case Enemy.ENEMY_GOOMBA:
                        value = TileVal.STOMPABLE_ENEMY;
                        break;
                    case Enemy.ENEMY_SPIKY:
                    case Enemy.ENEMY_FLOWER:
                        value = TileVal.ENEMY;
                        break;
                    }
            } else if (sprite instanceof Shell) {
                final Shell shell = (Shell) sprite;
                if (!shell.carried && !shell.dead && shell.deadTime == 0)
                    value = TileVal.STOMPABLE_ENEMY;
            } else
                if (sprite instanceof FireFlower || sprite instanceof Mushroom)
                value = TileVal.POWER_UP;

            for (final Tile tile : Tile.values())
                if (tile.contains(x, y) && value.priority < receptField[tile
                        .ordinal()].priority)
                    receptField[tile.ordinal()] = value;
        }

        for (int y = -9; y <= 10; ++y)
            for (int x = -9; x <= 10; ++x) {
                final int type = level.getBlock(xMario + x, yMario + y) & 0xff;
                final int behavior = Level.TILE_BEHAVIORS[type];

                final TileVal value;
                if ((behavior & Level.BIT_PICKUPABLE) > 0)
                    value = TileVal.COIN;
                else if ((behavior & Level.BIT_BLOCK_ALL) > 0
                        || (behavior & Level.BIT_BLOCK_UPPER) > 0
                        || (behavior & Level.BIT_BLOCK_LOWER) > 0
                        || type == 145)
                    value = TileVal.LEVEL_OBJECT;
                else
                    value = TileVal.EMPTY_CELL;

                for (final Tile tile : Tile.values())
                    if (tile.contains(x, y) && value.priority < receptField[tile
                            .ordinal()].priority)
                        receptField[tile.ordinal()] = value;
            }

        final double[] input = new double[61];
        for (int i = 0; i < receptField.length; ++i)
            input[i] = receptField[i].value;

        return input;
    }

    @Override
    public void levelFailed() {
        newGame(false);
    }

    @Override
    public void levelWon() {
        newGame(true);
    }

    public abstract void newGame(final boolean victory);

    @Override
    public abstract void run();
}
