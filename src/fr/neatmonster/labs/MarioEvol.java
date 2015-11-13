package fr.neatmonster.labs;

import static fr.neatmonster.neato.Population.POPULATION;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
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

import fr.neatmonster.neato.Ensemble;
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
            new Thread(new MarioEvol(), "Worker-" + i).start();

        int hasEnsemble = 0, generation = 0;
        final Population pop = new Population();
        long start = System.currentTimeMillis();
        while (true) {
            ++generation;

            try {
                countdown = new CountDownLatch(POPULATION + hasEnsemble);

                if (hasEnsemble > 0)
                    queue.put(pop.ensemble);

                pop.listAll();
                for (int i = 0; i < POPULATION; ++i)
                    queue.put(pop.phenotypes.get(i));

                countdown.await();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            if (hasEnsemble > 0) {
                String log = "# Generation " + generation + "\n";
                final double[] fitness = pop.ensemble.fitness;
                log += "Kills: " + fitness[0] + "\n";
                log += "Coins: " + fitness[1] + "\n";
                log += "Damage: " + fitness[2] + "\n";
                log += "Distance: " + fitness[3] + "\n";
                log += "Time: " + fitness[4] + "\n";
                System.out.print(log);
                try {
                    final PrintWriter out = new PrintWriter(new BufferedWriter(
                            new FileWriter(new File(dir, "log.txt"), true)));
                    out.print(log);
                    out.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }

                try {
                    final GsonBuilder gsonBuild = new GsonBuilder();
                    gsonBuild.setPrettyPrinting();
                    gsonBuild.registerTypeAdapter(Ensemble.class,
                            new EnsembleSerializer());
                    final Gson gson = gsonBuild.create();
                    final String ens = gson.toJson(pop.ensemble);
                    final PrintWriter writer = new PrintWriter(new File(
                            uuid.toString(), "ens" + generation + ".json"));
                    writer.println(ens);
                    writer.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

            pop.newGeneration();

            hasEnsemble = 1;
            System.out.println("Calculated in "
                    + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();
        }
    }

    @Override
    public void newGame(final boolean victory) {
        final LevelScene levelScene = (LevelScene) scene;
        dist += (int) (levelScene.mario.x / 16);
        time += levelScene.timeLeft;

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
        }

        startLevel(RANDOM.nextLong(), DIFFICULTY,
                LevelGenerator.TYPE_OVERGROUND);
        evaluate();
    }

    @Override
    public void run() {
        final GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        final GraphicsDevice[] gs = ge.getScreenDevices();
        graphicsConfiguration = gs[0].getConfigurations()[0];

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

            if (ls.tick % 5 == 0)
                evaluate();
        }
    }
}
