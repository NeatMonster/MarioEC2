package fr.neatmonster.labs;

import static fr.neatmonster.neato.Population.POPULATION;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.mario.Art;
import com.mojang.mario.LevelScene;
import com.mojang.mario.level.LevelGenerator;
import com.mojang.sonar.FakeSoundEngine;

import fr.neatmonster.neato.Individual;
import fr.neatmonster.neato.Population;

@SuppressWarnings("serial")
public class MarioEvol extends MarioEC2 {
    public static final int THREADS = 8;

    public static BlockingQueue<Individual> queue;
    public static CountDownLatch            countdown;

    public static void main(final String[] args) {
        final UUID uuid = UUID.randomUUID();
        final File dir = new File(uuid.toString());
        try {
            if (!dir.exists())
                dir.mkdir();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        queue = new ArrayBlockingQueue<Individual>(POPULATION);

        for (int i = 0; i < THREADS; ++i)
            new Thread(new MarioEvol(), "Mario-" + i).start();

        final Population pop = new Population();
        long start = System.currentTimeMillis();

        int generation = 0;
        while (true) {
            ++generation;

            try {
                countdown = new CountDownLatch(POPULATION);

                for (int i = 0; i < POPULATION; ++i)
                    queue.put(pop.population.get(i));

                countdown.await();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            String log = "# Generation " + generation + "\n";
            log += "Best distance: " + pop.bestFitness + "\n";
            log += "Average distance: " + pop.avgFitness + "\n";
//            final Individual best = pop.population.get(0);
//            log += "Distance: " + best.fitness[0] + "\n";
//            log += "Kills: " + best.fitness[1] + "\n";
//            log += "Coins: " + best.fitness[2] + "\n";
//            log += "Damage: " + best.fitness[3] + "\n";
//            log += "Time: " + best.fitness[4] + "\n";

            for (int i = 0; i < 5; ++i)
                try {
                    final GsonBuilder gsonBuild = new GsonBuilder();
                    gsonBuild.setPrettyPrinting();
                    gsonBuild.registerTypeAdapter(Individual.class,
                            new IndividualSerializer());
                    final Gson gson = gsonBuild.create();
                    final String ind = gson.toJson(pop.population.get(i));
                    final PrintWriter writer = new PrintWriter(
                            new File(uuid.toString(),
                                    "gen" + generation + "ind" + i + ".json"));
                    writer.println(ind);
                    writer.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }

            pop.newGeneration();

            log += "Calculated in " + (System.currentTimeMillis() - start)
                    + "ms";
            System.out.println(log);
            start = System.currentTimeMillis();

            try {
                final PrintWriter out = new PrintWriter(new BufferedWriter(
                        new FileWriter(new File(dir, "log.txt"), true)));
                out.print(log);
                out.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    public double maxDistance = 0;
    public int    staleness   = 0;

    @Override
    public void newGame(final boolean victory) {
        final LevelScene levelScene = (LevelScene) scene;
        dist += (int) (levelScene.mario.x / 16);
        time += levelScene.tick;

        if (++nextLevel >= LEVELS) {
            creature.fitness = getFitness();

            countdown.countDown();
            try {
                creature = queue.take();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            nextLevel = 0;
            dist = time = 0.0;
            resetStatic();
        } else {
            large = false;
            fire = true;
        }
        staleness = 0;
        maxDistance = 0.0;

        startLevel(RANDOM.nextLong(), DIFFICULTY,
                LevelGenerator.TYPE_OVERGROUND);
        evaluate();
    }

    @Override
    public void run() {
        graphicsConfiguration = new DummyGC();

        scene = new LevelScene(graphicsConfiguration, this, RANDOM.nextLong(),
                DIFFICULTY, LevelGenerator.TYPE_OVERGROUND);
        scene.setSound(sound = new FakeSoundEngine());

        Art.init(graphicsConfiguration, sound);
        Art.sequencer = null;

        scene.init();

        try {
            creature = queue.take();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        nextLevel = 0;
        dist = time = 0.0;
        resetStatic();

        evaluate();

        while (true) {
            final LevelScene ls = (LevelScene) scene;

            ls.tick();

            if (ls.mario.winTime > 0)
                levelWon();

            else if (ls.mario.deathTime > 0)
                levelFailed();

            final double distance = ls.mario.x;
            if (distance > maxDistance) {
                maxDistance = distance;
                staleness = 0;
            } else {
                ++staleness;
                if (staleness > 180)
                    levelFailed();
            }

            if (ls.tick % 5 == 0)
                evaluate();
        }
    }
}
