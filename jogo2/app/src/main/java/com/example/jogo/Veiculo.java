package com.example.jogo;

import android.graphics.Point;

public interface Veiculo {
    void mover();
    void parar();
    Point getPosicao();
    void setPosicao(double x, double y);
    Point getCenterOfMassPosition();
    void updateRotation(Point target);
    Point getFrontPosition();
    void moveTowards(Point target);
    Point calculateCenterOfMass();
    void run();
    void checkCollision();
    void stop();
}
