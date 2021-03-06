package com.fungus_soft.blockgame;

import java.awt.AWTException;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.fungus_soft.blockgame.entities.Entity;
import com.fungus_soft.blockgame.entities.Player;
import com.fungus_soft.blockgame.entities.Zombie;
import com.fungus_soft.blockgame.menu.CreditsMenu;
import com.fungus_soft.blockgame.menu.MainMenu;
import com.fungus_soft.blockgame.menu.SplashScreen;
import com.fungus_soft.blockgame.world.Block;
import com.fungus_soft.blockgame.world.Blocks;
import com.fungus_soft.blockgame.world.WorldLoadingScreen;

public class BlockGame extends JFrame {

    private static final long serialVersionUID = 1L;
    public static long renderTime;

    private static JPanel render;
    public static World world;   // The only world
    public static Player player; // The only player

    public CreativeInventoryFrame inv; // The creative inventory
    public WorldLoadingScreen wload;   // The world load screen

    public static boolean inGame;
    public static boolean isRendering;
    public static boolean isChanging;
    public boolean debugInfo;

    public int fps;
    private long fps2;
    private int fps3;

    private MainMenu menu;
    private static BlockGame game;
    public static BlockGame getGame() {
        return game;
    }

    public BlockGame() {
        BlockGame.game = this;

        menu = new MainMenu();
        menu.sp.addActionListener(l -> startGame());
        menu.cr.addActionListener(l -> {
            ResourceManager.playSoundAsync("sounds/click.mp3");
            this.setContentPane(new CreditsMenu()); 
            this.validate();
        });

        this.setTitle("The Incredible Game");
        setContentPane(new SplashScreen());
        Timer timer = new Timer(3100, e -> {
            setContentPane(menu);
            validate();
        });
        timer.setRepeats(false);
        timer.start(); 

        this.setDefaultCloseOperation(3);
        this.setSize(875, 600);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void backToMainMenu() {
        this.setContentPane(menu);
        this.validate();
    }

    public void startGame() {
        Blocks.addDefaultBlocks();
        world = new World("world1");
        player = (Player) world.addEntity(new Player());
        inv = new CreativeInventoryFrame();
        wload = new WorldLoadingScreen();
        this.getGlassPane().setSize(100, 100);

        render = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paint(Graphics g) {
                if (isRendering || isChanging) {
                    fps3++;
                    return;
                }

                isRendering = true;
                long start = System.currentTimeMillis();
                if (start-fps2 >= 1000) {
                    fps2=start;
                    fps=fps3;
                    fps3=0;
                }

                world.paint(g);

                for (Entity e : world.getEntities())
                    e.paint(g);

                world.entitiesToAdd.forEach(e -> world.getEntities().add(e));
                world.entitiesToAdd.clear();

                player.getLookingAt(g); // Draw outline
                g.drawString(fps + "fps", getWidth() - 40, 15);

                if (debugInfo) {
                    g.drawString("Render time: " + renderTime + "ms", getWidth() - 100, 54);
                    g.drawString("BlockData: " + world.locationToBlock.size(), getWidth() - 100, 67);
                    g.drawString("Pos/32: {" + player.x/32 + "," + player.y/32 + "}", getWidth() - 100, 80);
                    g.drawString("Entity count: " + world.entities.size(), getWidth() - 100, 94);
                    renderTime = System.currentTimeMillis() - start;
                }

                fps3++;
                isRendering = false;

                g.dispose();
                if (!isRendering) render.repaint();
            }
        };

        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                    case KeyEvent.VK_D:
                        player.x += 16;
                        break;

                    case KeyEvent.VK_S:
                    case KeyEvent.VK_A:
                        player.x -= 16;
                        break;

                    case KeyEvent.VK_B:
                    case KeyEvent.VK_E:
                        inv = new CreativeInventoryFrame();
                        setGlassPane(inv);
                        inv.setVisible(!inv.isVisible());
                        break;

                    case KeyEvent.VK_R:
                        player.respawn();
                        break;
                    case KeyEvent.VK_SPACE:
                        player.jump();
                        break;
                    case KeyEvent.VK_F2:
                        try {
                            Robot r = new Robot();
                            Rectangle b = getBounds();
                            b.x += 9;
                            b.y += 31;
                            b.height -= 39;
                            b.width -= 20;
                            BufferedImage i = r.createScreenCapture(b);
                            File dir = new File(new File(System.getProperty("user.home")), "blockgame");
                            dir.mkdirs();
                            ImageIO.write(i, "PNG", new File(dir, System.currentTimeMillis() + ".png"));
                        } catch (AWTException | IOException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    case KeyEvent.VK_F3:
                        debugInfo = !debugInfo;
                        break;
                    case KeyEvent.VK_Z: {
                        world.addEntity(new Zombie());
                        break;
                    }
                }
                validate();
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                int mx = p.x/32;
                int my = (p.y/32)-1;

                if (e.getButton() == 3) {
                    Block b = player.getSelectedBlock();
                    b.preSetBlock(world, mx, my);
                    world.setBlockAt(mx, my, b.getId());
                }

                if (e.getButton() == 1)
                    world.setBlockAt(mx, my, 0);
            }
        });

        inGame = true;
        this.setContentPane(render);
        this.validate();
        this.requestFocus();
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        new BlockGame();
    }

}