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

public class CalculateCenterOfMassTask extends Thread {
    private Car car;

    public CalculateCenterOfMassTask(Car car) {
        this.car = car;
    }

    @Override
    public void run() {

        long tempo_inicioCalculate = System.nanoTime();

        // Usa o novo método para obter pixels brancos e dos carros
        List<Point> relevantPixels = car.scanForCarPixels();
        car.setCenterOfMass(MathUtils.calculateCenterOfMass(relevantPixels));

        long tempo_fimCalculate = System.nanoTime();
        long tempoExecucao = tempo_fimCalculate - tempo_inicioCalculate;

        Log.i("Task", "Tempo de execução  calculo centro de massa" + ": " + (tempoExecucao/ 1_000_000_000.0) + " segundos.");
        Log.i("CarTask", "Finalizando calculateCenterOfMass" + ": " +
                (tempo_fimCalculate - car.getTempoInicioAtividade()) / 1_000_000_000.0);
    }

}
