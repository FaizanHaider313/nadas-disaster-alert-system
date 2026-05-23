package com.disaster.alert.util;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleNetworkCanvas extends Canvas {

    private static final int    PARTICLE_COUNT     = 80;
    private static final double CONNECTION_DIST    = 140;
    private static final double MOUSE_ATTRACT_DIST = 180;
    private static final double SPEED              = 0.4;

    private final List<Particle> particles = new ArrayList<>();
    private double mouseX = -999, mouseY = -999;
    private final Random random = new Random();
    private boolean initialized = false;

    private static class Particle {
        double x, y, vx, vy, size;

        Particle(double w, double h, Random rnd) {
            x    = rnd.nextDouble() * w;
            y    = rnd.nextDouble() * h;
            double angle = rnd.nextDouble() * Math.PI * 2;
            double speed = (rnd.nextDouble() * 0.5 + 0.15) * SPEED;
            vx   = Math.cos(angle) * speed;
            vy   = Math.sin(angle) * speed;
            size = rnd.nextDouble() * 2.5 + 1.0;
        }

        void update(double w, double h) {
            x += vx;
            y += vy;
            if (x < 0 || x > w) vx = -vx;
            if (y < 0 || y > h) vy = -vy;
            x = Math.max(0, Math.min(w, x));
            y = Math.max(0, Math.min(h, y));
        }
    }

    public ParticleNetworkCanvas() {
        super(100, 100);
        setMouseTransparent(true);

        // Bind size to parent StackPane
        parentProperty().addListener((obs, oldP, newP) -> {
            if (newP instanceof StackPane sp) {
                widthProperty().bind(sp.widthProperty());
                heightProperty().bind(sp.heightProperty());
            }
        });

        // Mouse tracking
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(
                        javafx.scene.input.MouseEvent.MOUSE_MOVED, e -> {
                            Point2D local = sceneToLocal(e.getSceneX(), e.getSceneY());
                            mouseX = local.getX();
                            mouseY = local.getY();
                        });
            }
        });

        startAnimation();
    }

    private void initParticles(double w, double h) {
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new Particle(w, h, random));
        }
        initialized = true;
    }

    private void startAnimation() {
        GraphicsContext gc = getGraphicsContext2D();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                double w = getWidth();
                double h = getHeight();

                // Wait until canvas has real size, THEN initialize particles
                if (w < 50 || h < 50) return;

                if (!initialized) {
                    initParticles(w, h);
                }

                gc.clearRect(0, 0, w, h);

                // Update positions
                for (Particle p : particles) {
                    double dx   = mouseX - p.x;
                    double dy   = mouseY - p.y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < MOUSE_ATTRACT_DIST && dist > 0) {
                        double force = (MOUSE_ATTRACT_DIST - dist)
                                / MOUSE_ATTRACT_DIST * 0.012;
                        p.vx += dx / dist * force;
                        p.vy += dy / dist * force;
                        double spd = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
                        if (spd > SPEED * 3) {
                            p.vx = p.vx / spd * SPEED * 3;
                            p.vy = p.vy / spd * SPEED * 3;
                        }
                    }
                    p.update(w, h);
                }

                // Draw connections between particles
                for (int i = 0; i < particles.size(); i++) {
                    Particle a = particles.get(i);
                    for (int j = i + 1; j < particles.size(); j++) {
                        Particle b = particles.get(j);
                        double dx   = a.x - b.x;
                        double dy   = a.y - b.y;
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist < CONNECTION_DIST) {
                            double alpha = (1 - dist / CONNECTION_DIST) * 0.55;
                            gc.setStroke(Color.rgb(100, 200, 255, alpha));
                            gc.setLineWidth(0.8);
                            gc.strokeLine(a.x, a.y, b.x, b.y);
                        }
                    }
                }

                // Draw mouse connection lines
                if (mouseX > 0) {
                    for (Particle p : particles) {
                        double dx   = p.x - mouseX;
                        double dy   = p.y - mouseY;
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist < MOUSE_ATTRACT_DIST) {
                            double alpha = (1 - dist / MOUSE_ATTRACT_DIST) * 0.7;
                            gc.setStroke(Color.rgb(150, 230, 255, alpha));
                            gc.setLineWidth(1.0);
                            gc.strokeLine(p.x, p.y, mouseX, mouseY);
                        }
                    }
                }

                // Draw particles
                for (Particle p : particles) {
                    gc.setFill(Color.rgb(100, 200, 255, 0.15));
                    double glowSize = p.size * 3.5;
                    gc.fillOval(p.x - glowSize / 2, p.y - glowSize / 2,
                            glowSize, glowSize);
                    gc.setFill(Color.rgb(150, 220, 255, 0.9));
                    gc.fillOval(p.x - p.size / 2, p.y - p.size / 2,
                            p.size, p.size);
                }
            }
        }.start();
    }

    @Override public boolean isResizable() { return true; }

    @Override
    public void resize(double width, double height) {
        // Controlled by binding — do nothing
    }
}