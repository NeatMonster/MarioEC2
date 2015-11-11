package fr.neatmonster.labs;

import static fr.neatmonster.neato.Population.INPUTS;
import static fr.neatmonster.neato.Population.OUTPUTS;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.VolatileImage;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.mario.Art;
import com.mojang.mario.LevelScene;
import com.mojang.mario.level.LevelGenerator;

import fr.neatmonster.neato.Ensemble;
import fr.neatmonster.neato.Individual;
import fr.neatmonster.neato.Neuron;
import fr.neatmonster.neato.Synapse;

@SuppressWarnings("serial")
public class MarioReplay extends MarioEC2 {

    public static class Box {
        public int          x;
        public int          y;
        public int          h;
        public int          w;
        public final double value;

        public Box(final int x, final int y, final int w, final int h,
                final double value) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.value = value;
        }
    }

    public static final String[] BUTTONS = new String[] { "JUMP", "SPEED", "UP",
            "DOWN", "LEFT", "RIGHT" };

    public static final int XMIN = 225;

    public static final int XMAX = 565;

    public static final int YMIN = 25;

    public static final int YMAX = 215;

    public static void main(final String[] args) {
        final JFrame frame = new JFrame("Mario Replay");

        final FileDialog fd = new FileDialog(frame,
                "Choose the ensemble to replay", FileDialog.LOAD);
        fd.setDirectory(".");
        fd.setFile("*.json");
        fd.setVisible(true);

        final String filename = fd.getFile();
        if (filename == null)
            System.exit(0);

        final MarioReplay replay = new MarioReplay(
                new File(fd.getDirectory(), filename));

        frame.setContentPane(replay);
        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final Dimension screenSize = Toolkit.getDefaultToolkit()
                .getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2,
                (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

        replay.start();
    }

    public volatile boolean reset = false;

    public int nextEl = 0;

    public MarioReplay(final File file) {
        super();

        try {
            final GsonBuilder gsonBuild = new GsonBuilder();
            gsonBuild.setPrettyPrinting();
            gsonBuild.registerTypeAdapter(Ensemble.class,
                    new EnsembleDeserializer());
            final Gson gson = gsonBuild.create();
            final String ens = new String(Files.readAllBytes(file.toPath()));
            creature = gson.fromJson(ens, Ensemble.class);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void newGame(final boolean victory) {
        final LevelScene levelScene = (LevelScene) scene;
        dist += (int) (levelScene.mario.x / 16);
        time += levelScene.timeLeft;
        if (++nextLevel >= LEVELS) {
            nextLevel = 0;
            dist = time = 0.0;
            resetStatic();
        }
        startLevel(RANDOM.nextLong(), DIFFICULTY,
                LevelGenerator.TYPE_OVERGROUND);
        evaluate();
    }

    public void paintNetwork(final Graphics g) {
        try {
            final Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.scale(0.5, 0.5);

            g2d.setColor(new Color(0x80ffffff, true));
            g2d.fillRoundRect(15, 15, 610, 230, 6, 6);

            g2d.setFont(new Font("Rockwell", Font.PLAIN, 50));

            final List<Individual> elements = ((Ensemble) creature).elements;
            final Individual current = elements
                    .get(nextEl++ / 20 % elements.size());

            final Map<Integer, Box> cells = new HashMap<Integer, Box>();
            for (int i = 0; i < INPUTS; ++i) {
                final Neuron neuron = current.inputs.get(i);
                if (i == INPUTS - 1)
                    cells.put(i, new Box(205, 225, 10, 10, neuron.value));
                else {
                    final Tile tile = Tile.values()[i];
                    cells.put(i, new Box(115 + 10 * tile.x, 115 + 10 * tile.y,
                            10 * tile.w, 10 * tile.h, neuron.value));
                }
            }

            for (int i = 0; i < OUTPUTS; ++i) {
                final Neuron neuron = current.outputs.get(i);
                cells.put(INPUTS + i,
                        new Box(575, 25 + 40 * i, 10, 10, neuron.value));
                final int color = neuron.value > 0.5 ? 0x80000000 : 0x30000000;
                g2d.setColor(new Color(color, true));
                g2d.setFont(g2d.getFont().deriveFont(9f));
                g2d.drawString(BUTTONS[i], 590, 33 + 40 * i);
            }

            for (final Neuron neuron : current.hidden)
                cells.put(neuron.neuronId, new Box((XMIN + XMAX) / 2,
                        (YMIN + YMAX) / 2, 10, 10, neuron.value));

            for (int n = 0; n < 4; ++n)
                for (final Synapse connect : current.connects)
                    if (connect.enabled) {
                        final Box in = cells.get(connect.input);
                        final Box out = cells.get(connect.output);

                        if (connect.input.neuronId >= INPUTS + OUTPUTS) {
                            in.x = (int) (0.75 * in.x + 0.25 * out.x);
                            if (in.x >= out.x)
                                in.x -= 40;
                            if (in.x < XMIN)
                                in.x = XMIN;
                            if (in.x > XMAX)
                                in.x = XMAX;
                            in.y = (int) (0.75 * in.y + 0.25 * out.y);
                        }

                        if (connect.output.neuronId >= INPUTS + OUTPUTS) {
                            out.x = (int) (0.25 * in.x + 0.75 * out.x);
                            if (in.x >= out.x)
                                out.x += 40;
                            if (out.x < XMIN)
                                out.x = XMIN;
                            if (out.x > XMAX)
                                out.x = XMAX;
                            out.y = (int) (0.25 * in.y + 0.75 * out.y);
                        }
                    }

            for (final Synapse connect : current.connects)
                if (connect.enabled) {
                    final Box in = cells.get(connect.input.neuronId);
                    final Box out = cells.get(connect.output.neuronId);

                    final float value = (float) Math
                            .abs(Neuron.sigmoid(connect.weight));
                    final int opacity = in.value == 0 ? 0x30 : 0x80;
                    final Color color;
                    if (connect.weight > 0.0)
                        color = Color.getHSBColor(2f / 3f, 1f, value);
                    else
                        color = Color.getHSBColor(0f, 1f, value);

                    g2d.setColor(new Color(color.getRed(), color.getGreen(),
                            color.getBlue(), opacity));
                    g2d.drawLine(in.x + in.w - 2, in.y + in.h / 2, out.x + 2,
                            out.y + out.h / 2);
                }

            for (final Box cell : cells.values()) {
                final float value = (float) Math.abs(cell.value);
                final int opacity = cell.value == 0 ? 0x30 : 0x80;
                final Color color;
                if (cell.value > 0.0)
                    color = Color.getHSBColor(2f / 3f, 1f, value);
                else
                    color = Color.getHSBColor(0f, 1f, value);

                g2d.setColor(new Color(color.getRed(), color.getGreen(),
                        color.getBlue(), opacity));
                g2d.fillRect(cell.x, cell.y, cell.w, cell.h);

                g2d.setColor(g2d.getColor().darker().darker());
                g2d.drawRect(cell.x, cell.y, cell.w, cell.h);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        graphicsConfiguration = getGraphicsConfiguration();

        scene = new LevelScene(graphicsConfiguration, this, RANDOM.nextLong(),
                DIFFICULTY, LevelGenerator.TYPE_OVERGROUND);
        scene.setSound(sound);

        Art.init(graphicsConfiguration, sound);

        final VolatileImage image = createVolatileImage(320, 240);
        final Graphics g = getGraphics();
        final Graphics og = image.getGraphics();

        int lastTick = -1;

        double time = System.nanoTime() / 1000000000.0;
        double now = time;
        double averagePassedTime = 0;

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_R)
                    reset = true;
            }
        });
        addFocusListener(this);

        boolean naiveTiming = true;

        scene.init();

        while (true) {
            final double lastTime = time;
            time = System.nanoTime() / 1000000000.0;
            final double passedTime = time - lastTime;

            if (passedTime < 0)
                naiveTiming = false;
            averagePassedTime = averagePassedTime * 0.9 + passedTime * 0.1;

            if (naiveTiming)
                now = time;
            else
                now += averagePassedTime;

            final int tick = (int) (now * TICKS_PER_SECOND);
            if (lastTick == -1)
                lastTick = tick;
            while (lastTick < tick) {
                scene.tick();
                lastTick++;
            }

            final float alpha = (float) (now * TICKS_PER_SECOND - tick);
            sound.clientTick(alpha);

            og.setColor(Color.WHITE);
            og.fillRect(0, 0, 320, 240);

            scene.render(og, alpha);

            paintNetwork(image.createGraphics());

            if (width != 320 || height != 240) {
                if (useScale2x)
                    g.drawImage(scale2x.scale(image), 0, 0, null);
                else
                    g.drawImage(image, 0, 0, 640, 480, null);
            } else
                g.drawImage(image, 0, 0, null);

            try {
                Thread.sleep(5L);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            final LevelScene ls = (LevelScene) scene;
            if (ls.tick % 5 == 0)
                evaluate();

            if (reset) {
                reset = false;
                newGame(false);
            }
        }
    }
}
