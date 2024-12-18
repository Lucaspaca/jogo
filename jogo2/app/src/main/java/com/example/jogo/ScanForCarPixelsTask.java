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

public class ScanForCarPixelsTask extends Thread
{
    private Car car;

    public ScanForCarPixelsTask(Car car) {
        this.car = car;
    }

    @Override
    public void run() {
        long tempo_inicioScan = System.nanoTime();

        // Código do scanForCarPixels
        Point frontPosition = car.getFrontPosition();
        List<Point> detectedPixels = MathUtils.scanForWhitePixels(
                car.getBitmap(),
                frontPosition,
                car.getAngle(),
                car.getSensorRange(),
                car.getD()
        );

        // Verifica a posição dos outros carros na área de varredura e adiciona seus pixels
        for (Car otherCar : MainActivity.cars) {
            if (otherCar != car) { // Ignora o próprio carro
                Point otherCarPos = otherCar.getPosicao();

                // Verifica se o outro carro está dentro do alcance do sensor
                if (Math.hypot(otherCarPos.x - car.getX(), otherCarPos.y - car.getY()) <= car.getSensorRange()) {
                    detectedPixels.add(otherCarPos);
                }
            }
        }

        long tempo_fimScan = System.nanoTime();
        long tempoExecucao = tempo_fimScan - tempo_inicioScan;

        Log.i("Task", "Tempo de execução scan" + ": " + (tempoExecucao/ 1_000_000_000.0) + " segundos." + "teste ");
        Log.i("CarTask", "Finalizando scanForCarPixels" + ": " +
                (tempo_fimScan - car.getTempoInicioAtividade()) / 1_000_000_000.0);
    }
}
