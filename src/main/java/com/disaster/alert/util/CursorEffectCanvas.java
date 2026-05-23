package com.disaster.alert.util;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CursorEffectCanvas extends Canvas {

    private static final int SQUARE_SIZE = 40;

    private static class Cell {
        double x, y;
        double alpha = 0;
        boolean fading = false;
        long lastTouched = 0;

        Cell(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private Cell[] grid;

    public CursorEffectCanvas() {
        super(1200, 700);

        setMouseTransparent(true);

        initGrid();
        startAnimation();

        widthProperty().addListener(e -> initGrid());
        heightProperty().addListener(e -> initGrid());

        // ✅ Convert scene coordinates to canvas local coordinates
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnMouseMoved(e -> {
                    Point2D localPoint = sceneToLocal(e.getSceneX(), e.getSceneY());
                    highlightCell(localPoint.getX(), localPoint.getY());
                });
            }
        });
    }

    private void initGrid() {
        int cols = (int)(getWidth()  / SQUARE_SIZE) + 2;
        int rows = (int)(getHeight() / SQUARE_SIZE) + 2;
        grid = new Cell[cols * rows];
        int idx = 0;
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                grid[idx++] = new Cell(x * SQUARE_SIZE, y * SQUARE_SIZE);
            }
        }
    }

    private void highlightCell(double mx, double my) {
        if (grid == null) return;
        for (Cell cell : grid) {
            if (mx >= cell.x && mx < cell.x + SQUARE_SIZE &&
                    my >= cell.y && my < cell.y + SQUARE_SIZE) {
                cell.alpha = 1.0;
                cell.lastTouched = System.currentTimeMillis();
                cell.fading = false;
                break;
            }
        }
    }

    private void startAnimation() {
        GraphicsContext gc = getGraphicsContext2D();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, getWidth(), getHeight());
                long time = System.currentTimeMillis();

                if (grid == null) return;

                for (Cell cell : grid) {
                    if (cell.alpha > 0 && !cell.fading
                            && time - cell.lastTouched > 500) {
                        cell.fading = true;
                    }

                    if (cell.fading) {
                        cell.alpha -= 0.018;
                        if (cell.alpha <= 0) {
                            cell.alpha = 0;
                            cell.fading = false;
                        }
                    }

                    if (cell.alpha > 0) {
                        // Outer glow
                        gc.setStroke(Color.rgb(56, 182, 255, cell.alpha * 0.3));
                        gc.setLineWidth(3);
                        gc.strokeRect(cell.x, cell.y, SQUARE_SIZE, SQUARE_SIZE);

                        // Inner sharp border
                        gc.setStroke(Color.rgb(56, 182, 255, cell.alpha * 0.8));
                        gc.setLineWidth(1.2);
                        gc.strokeRect(cell.x + 0.5, cell.y + 0.5,
                                SQUARE_SIZE - 1, SQUARE_SIZE - 1);
                    }
                }
            }
        }.start();
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(double width, double height) {
        super.setWidth(width);
        super.setHeight(height);
        initGrid();
    }
}