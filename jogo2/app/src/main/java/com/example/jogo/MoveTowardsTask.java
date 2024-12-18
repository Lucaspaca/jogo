package com.example.jogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.view.View;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import android.os.Handler;
import java.util.Map;
import java.util.HashMap;
import com.example.mathlibrary.MathUtils;
import android.util.Log;
import android.os.Process;

public class MoveTowardsTask extends Thread{
    private Car car;
    private Point target;

    public MoveTowardsTask(Car car, Point target) {
        this.car = car;
        this.target = target;
    }

    @Override
    public void run() {
        long tempo_inicioMove = System.nanoTime();

        if (target != null) {
            Point newPosition = MathUtils.moveTowards(car.getPosicao(), target, car.getSpeed());

            // Lógica para evitar colisão com outros carros
            for (Car otherCar : MainActivity.cars) {
                if (otherCar != car) {
                    double distanceToOtherCar = Math.hypot(otherCar.getX() - newPosition.x, otherCar.getY() - newPosition.y);

                    // Ajuste para evitar colisão se muito próximo
                    if (distanceToOtherCar < car.getCarWidth()) {
                        newPosition.x += (car.getX() - otherCar.getX()) * 2;
                        newPosition.y += (car.getY() - otherCar.getY()) * 2;
                    }
                }
            }

            // Atualiza a posição e o ângulo do carro
            car.setX(newPosition.x);
            car.setY(newPosition.y);
            car.setAngle(MathUtils.calculateAngle(car.getPosicao(), target));
        }

        long tempo_fimMove = System.nanoTime();
        long tempoExecucao = tempo_fimMove - tempo_inicioMove;

        Log.i("Task", "Tempo de execução mover carro" + ": " + (tempoExecucao/ 1_000_000_000.0) + " segundos.");
        Log.i("CarTask", "Finalizando moveTowards" + ": " +
                (tempo_fimMove - car.getTempoInicioAtividade()) / 1_000_000_000.0);
    }

}
