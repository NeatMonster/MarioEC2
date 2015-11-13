package fr.neatmonster.labs;

import java.util.Arrays;
import java.util.Random;

import com.mojang.mario.LevelScene;
import com.mojang.mario.MarioComponent;
import com.mojang.mario.level.Level;
import com.mojang.mario.sprites.BulletBill;
import com.mojang.mario.sprites.Enemy;
import com.mojang.mario.sprites.FireFlower;
import com.mojang.mario.sprites.Fireball;
import com.mojang.mario.sprites.Mario;
import com.mojang.mario.sprites.Mushroom;
import com.mojang.mario.sprites.Shell;
import com.mojang.mario.sprites.Sprite;

import fr.neatmonster.labs.Tile.TileVal;
import fr.neatmonster.neato.Individual;

@SuppressWarnings("serial")
public abstract class MarioEC2 extends MarioComponent {
    public static final int LEVELS     = 5;
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
        fitness[0] = kills;
        fitness[1] = coins;
        fitness[2] = powerup + LEVELS - damage;
        if (fitness[2] < 0)
            fitness[2] = 0;
        fitness[3] = dist;
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

            final TileVal value;
            if (sprite instanceof BulletBill || sprite instanceof Enemy
                    || sprite instanceof FireFlower || sprite instanceof Shell)
                value = TileVal.ENEMY;
            else if (sprite instanceof Mario)
                value = TileVal.MARIO;
            else if (sprite instanceof Fireball)
                value = TileVal.FIREBALL;
            else if (sprite instanceof Mushroom)
                value = TileVal.POWER_UP;
            else
                value = TileVal.EMPTY_CELL;

            for (final Tile tile : Tile.values())
                if (tile.contains(x, y))
                    tile.setInput(receptField, value);
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
                    if (tile.contains(x, y))
                        tile.setInput(receptField, value);
            }

        final double[] input = new double[62];
        for (int i = 0; i < receptField.length; ++i)
            input[i] = receptField[i].value;
        input[61] = 1.0;

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
