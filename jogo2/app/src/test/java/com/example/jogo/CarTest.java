package com.example.jogo;

import org.junit.Test;
import java.util.Random;

import static org.junit.Assert.*;

public class CarTest {

    private static final int MIN_SPEED = 10; // exemplo de limite mínimo
    private static final int MAX_SPEED = 100; // exemplo de limite máximo

    private class Car {
        private int speed;
        private Random random = new Random();

        private void setRandomSpeed() {
            this.speed = MIN_SPEED + random.nextInt(MAX_SPEED - MIN_SPEED + 1);
        }

        public int getSpeed() {
            return speed;
        }
    }

    @Test
    public void testSetRandomSpeed_withinBounds() {
        Car car = new Car();

        // Executa o método que define a velocidade aleatória
        car.setRandomSpeed();

        // Verifica se a velocidade está dentro dos limites esperados
        int speed = car.getSpeed();
        assertTrue("A velocidade deve estar entre MIN_SPEED e MAX_SPEED",
                speed >= MIN_SPEED && speed <= MAX_SPEED);
    }
}
