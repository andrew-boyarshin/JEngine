package andrews.jengine;

import andrews.jengine.modules.Resources;
import andrews.jengine.modules.SaveGame;
import andrews.jengine.modules.SoundEngine;
import andrews.jengine.renders.BasicRender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static andrews.jengine.DB.db;

/**
 * @author STALKER_2010
 */
public class Game extends Canvas implements Runnable {
    private static final long serialVersionUID = -5445130295019858065L;
    private boolean running = false;
    public static Game instance;
    private JFrame frame;
    public String currentRoom = "";
    public Resources resources;
    public SoundEngine soundEngine;
    public SaveGame saveGame;
    public boolean unstableState = true;

    public Game() {
        instance = this;
    }

    public void start() {
        running = true;

        new Thread(this).start();
    }

    public static final int FPS = 25;
    private static final int FrameDuration = 1000 / FPS;
    private static final int MaxFrameSkip = 10;
    private long nextFrameTime = System.currentTimeMillis();

    public void run() {
        int loops;
        init();

        long start = System.currentTimeMillis();
        int fps = 0;
        while (running) {
            loops = 0;
            while (System.currentTimeMillis() > nextFrameTime
                    && loops < MaxFrameSkip) {

                if (unstableState) {
                    try {
                        update();
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                } else {
                    update();
                }

                nextFrameTime += FrameDuration;

                loops++;
            }
            render(db.rooms.get(currentRoom));
            fps++;
            if ((start + 1000) <= System.currentTimeMillis()) {
                frame.setTitle(NAME + " (" + fps + " FPS)");
                fps = 0;
                start = System.currentTimeMillis();
            }
        }
    }

    public void init() {
        resources = new Resources(this);
        soundEngine = new SoundEngine(this);
        saveGame = new SaveGame(this);
        render = new BasicRender(this);
        unstableState = false;
        addKeyListener(new Keyboard());
        addMouseListener(mouseListener);
        if (!render.init()) {
            System.err.println("Game: Render not initialized correctly");
        }
        db.onGameLoaded(this);
        currentRoom = "main_menu_room";
    }

    private BasicRender render;

    public void render(final Room room) {
        if (render != null) {
            if (unstableState) return;
            render.render(room);
        }
    }

    public void update() {
        db.rooms.get(currentRoom).update();
        for (GameObject o : db.objects.values()) {
            o.update();
        }
        for (Animation a : resources.animations.internal.values()) {
            a.update();
        }
        for (Animation a : resources.animations.generated.values()) {
            a.update();
        }
    }

    public static int WIDTH = 1094;
    public static int HEIGHT = 600;
    public static String NAME = "JEngine";

    public static Game launch(final Game game) {
        game.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        game.frame = new JFrame(Game.NAME);

        game.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        game.frame.setLayout(new BorderLayout());
        game.frame.add(game, BorderLayout.CENTER);
        game.frame.pack();
        game.frame.setResizable(false);
        game.frame.setVisible(true);
        game.start();

        return game;
    }

    private final MouseListener mouseListener = new MouseListener() {
        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (unstableState) return;
            PointerInfo a = MouseInfo.getPointerInfo();
            Point point = new Point(a.getLocation());
            SwingUtilities.convertPointFromScreen(point, e.getComponent());
            for (GameObject o : db.objects.values()) {
                o.onGlobalMouseClick(point.getX(), point.getY());
            }
        }
    };

    class Keyboard extends KeyAdapter {
        public void keyReleased(KeyEvent w) {
            final int kCode = w.getKeyCode();
            if (unstableState) return;
            for (GameObject gameObject : db.objects.values()) {
                gameObject.onKey(kCode);
                if (unstableState) return;
            }
        }

        public void keyPressed(KeyEvent e) {
            final int kCode = e.getKeyCode();
            if (unstableState) return;
            for (GameObject o : db.objects.values()) {
                o.onKeyPress(kCode);
                if (unstableState) return;
            }
        }
    }

}