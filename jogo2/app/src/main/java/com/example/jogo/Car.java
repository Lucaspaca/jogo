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

public class Car extends Thread implements Veiculo {
    private String name;
    private double x;
    private double y;
    private double speed;
    private int sensorRange;
    private View trackView;
    private double angle;
    private Point centerOfMass;
    private double d;
    private int carWidth;
    private int carHeight;
    private ImageView carImageView;
    private View comView;
    private boolean isRunning;
    private boolean isPaused;
    private Handler handler;
    private Bitmap bitmap;
    private int penalty;
    private boolean isSafetyCar;;
    private static final int MIN_SPEED = 5;
    private static final int MAX_SPEED = 12;
    private Random random = new Random();
    private float rotation;
    private Point frontPosition;
    private long tempo_inicio;
    private long tempo_fim;
    List<Long> executionTimesForCenterOfMass = new ArrayList<>();
    private long tempo_inicioAtividade;
    private long tempo_fimCalculate;
    private long tempo_fimScan;
    private long tempo_fimMove;
    private long tempo_inicioCalculate;
    private long tempo_inicioScan;
    private long tempo_fimEScan;
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);


    public Car(String name, double x, double y, double speed, int sensorRange,
               View trackView, double d,
               ImageView carImageView, View comView, Handler handler, boolean isSafetyCar) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.sensorRange = sensorRange;
        this.trackView = trackView;
        this.angle = 0;
        this.centerOfMass = new Point((int) x, (int) y);
        this.d = sensorRange + Math.hypot(carWidth / 2.0, carHeight / 2.0);
        this.carImageView = carImageView;
        this.comView = comView;
        this.isRunning = true;
        this.isPaused = false;
        this.handler = handler;
        this.bitmap = getBitmapFromView(trackView);
        this.penalty = 0;
        this.isSafetyCar = isSafetyCar;
    }
    public Bitmap getBitmap() {
        return bitmap;
    }
    public void setCenterOfMass(Point centerOfMass) {
        this.centerOfMass = centerOfMass;
    }
    public double getAngle() {
        return angle;
    }

    public int getSensorRange() {
        return sensorRange;
    }

    public double getD() {
        return d;
    }
    public long getTempoInicioAtividade() {
        return tempo_inicioAtividade;
    }
    // Getters e Setters
    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    private void setRandomSpeed() {
        this.speed = MIN_SPEED + random.nextInt(MAX_SPEED - MIN_SPEED + 1);
    }
    public double getCarWidth() {
        return carWidth;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        carImageView.setRotation(rotation);  // Aplica a rotação ao ImageView do carro
    }
    public double getSpeed() {
        return speed;
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public Point getFrontPosition() {
        return MathUtils.getFrontPosition(getPosicao(), angle);
    }

    public Point getCenterOfMassPosition() {
        // Usa o novo método de cálculo do centro de massa que inclui os carros
        Point centerMass = calculateCenterOfMass
                ();
        return (centerMass != null) ? centerMass : getFrontPosition();
    }

    @Override
    public Point getPosicao() {
        return new Point((int) x, (int) y);
    }

    @Override
    public void setPosicao(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setFrontPosition(Point frontPosition) {
        this.frontPosition = frontPosition;
    }

    public void checkCollision() {
        // Verifica colisão com bordas da pista
        if (x < 0 || x > trackView.getWidth() || y < 0 || y > trackView.getHeight()) {
            penalty++;
        }

        // Verifica colisão com outros carros
        for (Car otherCar : MainActivity.cars) {
            if (otherCar != this && Math.hypot(otherCar.getX() - this.x, otherCar.getY() - this.y) < carWidth) {
                penalty++;
            }
        }
    }

    @Override
    public void run() {
//        tempo_inicioAtividade = System.nanoTime();
//        Log.i("CarTask", "Início da atividade " + name + ": " +
//                (tempo_inicioAtividade / 1_000_000_000.0) + " segundos.");

        boolean inRestrictedRegion = false; // Controle para saber se o carro está na região
        if (isSafetyCar) {
            setPriority(Thread.MAX_PRIORITY); // Atribuir prioridade máxima
        }
        while (isRunning) {
            if (!isPaused) {
                try {
                    setRandomSpeed();

                    int numThreads = Runtime.getRuntime().availableProcessors(); // qtd
                    //processadores disponíveis
                    ExecutorService executor = Executors.newFixedThreadPool(1);

                    executor.submit(() ->{
                        tempo_inicioAtividade = System.nanoTime();
                        Log.i("CarTask", "Início da atividade " + name + ": " +
                                (tempo_inicioAtividade / 1_000_000_000.0) + " segundos.");
                        executorService.submit(new ScanForCarPixelsTask(this));
                        executorService.submit(new CalculateCenterOfMassTask(this));
                        executorService.submit(new MoveTowardsTask(this, getCenterOfMassPosition()));

});

                    // Verifica se o carro está para entrar na região restrita
                    if (isInRestrictedRegion() && !inRestrictedRegion) {
                        MainActivity.regionSemaphore.acquire();
                        inRestrictedRegion = true;
                    }

                    // Se o carro saiu da região restrita, libera o permit
                    if (!isInRestrictedRegion() && inRestrictedRegion) {
                        MainActivity.regionSemaphore.release();
                        inRestrictedRegion = false;
                    }

//                    moveTowards(getCenterOfMassPosition());

                    handler.post(() -> {
                        carImageView.setX((float) x);
                        carImageView.setY((float) y - 15);

                        Point centerOfMass = getCenterOfMassPosition();
                        if (centerOfMass != null) {
                            comView.setX(centerOfMass.x - comView.getWidth() / 2);
                            comView.setY(centerOfMass.y - comView.getHeight() / 2);
                            comView.setVisibility(View.VISIBLE);
                        } else {
                            comView.setVisibility(View.INVISIBLE);
                        }

                        if (centerOfMass != null) {
                            double deltaX = centerOfMass.x - x;
                            double deltaY = centerOfMass.y - y;
                            double angleToTarget = Math.toDegrees(Math.atan2(deltaY, deltaX));

                            float currentRotation = carImageView.getRotation();
                            float rotationDifference = (float) (angleToTarget - currentRotation);
                            rotationDifference = (rotationDifference + 180) % 360 - 180;

                            if (Math.abs(rotationDifference) > 5) {
                                rotationDifference = (rotationDifference > 0 ? 5 : -5);
                            }

                            carImageView.setRotation(currentRotation + rotationDifference);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(43);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (inRestrictedRegion) {
            MainActivity.regionSemaphore.release();
        }

        tempo_fim = System.nanoTime();

    }



        public void updateRotation(Point target) {
        if (target != null) {
            double angleToTarget = MathUtils.calculateAngle(getPosicao(), target);
            float newRotation = MathUtils.updateRotation(carImageView.getRotation(), angleToTarget);
            carImageView.setRotation(newRotation);
        }
    }

    public Point calculateCenterOfMass() {
        int coreNumber = 0; // Escolha o núcleo que você deseja usar
        int mask = 1 << coreNumber; // Cria uma máscara para o núcleo escolhido
        Process.setThreadPriority(Process.myTid(), mask); // Aplica a máscara ao
        tempo_inicioCalculate = System.nanoTime();

        // Usa o novo método para obter pixels brancos e dos carros
        List<Point> relevantPixels = scanForCarPixels();
        centerOfMass = MathUtils.calculateCenterOfMass(relevantPixels);

        tempo_fimCalculate = System.nanoTime();

        // Calcula o tempo de execução da função
        long tempoExecucao = tempo_fimCalculate - tempo_inicioCalculate;
        executionTimesForCenterOfMass.add(tempoExecucao); // Armazena o tempo de execução

//        Log.e("CenterOfMassTiming", "Tempo de execução do cálculo do centro de massa " + name + ": " +
//                (tempoExecucao / 1_000_000_000.0) + " segundos.");
//        Log.i("CarTask", "Finalizando calculateCenterOfMass para o carro " + name + ": " + (tempo_fimCalculate  - tempo_inicioAtividade)/ 1_000_000_000.0);

        return centerOfMass;
    }

    // Lista para armazenar os tempos de execução
    List<Long> executionTimesForMoveTowards = new ArrayList<>();

    @Override
    public void moveTowards(Point target) {
        int coreNumber = 0; // Escolha o núcleo que você deseja usar
        int mask = 1 << coreNumber; // Cria uma máscara para o núcleo escolhido
        Process.setThreadPriority(Process.myTid(), mask); // Aplica a máscara ao
        tempo_inicio = System.nanoTime();
        tempo_fimMove = System.nanoTime();


        if (target != null) {
            Point newPosition = MathUtils.moveTowards(getPosicao(), target, speed);

            // Adiciona lógica para evitar colisão
            for (Car otherCar : MainActivity.cars) {
                if (otherCar != this) {
                    double distanceToOtherCar = Math.hypot(otherCar.getX() - newPosition.x, otherCar.getY() - newPosition.y);

                    // Ajusta posição para evitar colisão se muito próximo
                    if (distanceToOtherCar < carWidth) {
                        newPosition.x += (this.x - otherCar.getX()) * 2; // Ajuste pequeno
                        newPosition.y += (this.y - otherCar.getY()) * 2;
                    }
                }
            }

            // Atualiza a posição e o ângulo do carro
            this.x = newPosition.x;
            this.y = newPosition.y;
            this.angle = MathUtils.calculateAngle(getPosicao(), target);
        }

        tempo_fim = System.nanoTime();

        // Calcula o tempo de execução da função
        long tempoExecucao = tempo_fim - tempo_inicio;
        executionTimesForMoveTowards.add(tempoExecucao); // Armazena o tempo de execução

//        Log.e("MoveTowardsTiming", "Tempo de execução do movimento do carro " + name + ": " +
//                (tempoExecucao / 1_000_000_000.0) + " segundos.");
//        Log.i("CarTask", "Finalizando moveTowards para o carro " + name + ": " + (tempo_fimMove  - tempo_inicioAtividade)/ 1_000_000_000.0);

    }

    @Override
    public void mover() {
        if (!isPaused) {
            moveTowards(getCenterOfMassPosition());
        }
    }

    @Override
    public void parar() {
        isRunning = false;
    }

    public boolean isInRestrictedRegion() {
        return x >= MainActivity.regionLeft && x <= MainActivity.regionRight &&
                y >= MainActivity.regionBottom && y <= MainActivity.regionTop;
    }

    public Map<String, Object> toMap() {
        tempo_inicio = System.nanoTime();

        Map<String, Object> carData = new HashMap<>();
        carData.put("name", name);
        carData.put("x", x);
        carData.put("y", y);
        carData.put("speed", speed);
        carData.put("rotation", (double) carImageView.getRotation());
        carData.put("sensorRange", sensorRange);
        carData.put("isSafetyCar", isSafetyCar);
        carData.put("penalty", penalty);

        // Adiciona a posição frontal e o ângulo
        Point frontPosition = getFrontPosition();
        carData.put("frontX", frontPosition.x);
        carData.put("frontY", frontPosition.y);
        carData.put("angle", angle); // Ângulo atual do carro

        return carData;
    }

    // List to store the times for multiple runs
    List<Long> executionTimes = new ArrayList<>();

    public List<Point> scanForCarPixels() {

        tempo_inicioScan = System.nanoTime();
        tempo_fimScan = System.nanoTime();

        // Código da função original
        Point frontPosition = getFrontPosition();
        List<Point> detectedPixels = MathUtils.scanForWhitePixels(bitmap, frontPosition, angle, sensorRange, d);

        // Verifica a posição dos outros carros na área de varredura e adiciona seus pixels
        for (Car otherCar : MainActivity.cars) {
            if (otherCar != this) { // Ignora o próprio carro
                Point otherCarPos = otherCar.getPosicao();

                // Verifica se o outro carro está dentro do alcance do sensor
                if (Math.hypot(otherCarPos.x - this.x, otherCarPos.y - this.y) <= sensorRange) {
                    detectedPixels.add(otherCarPos);
                }
            }
        }

        tempo_fimEScan = System.nanoTime();

        // Calcula o tempo de execução da função
        long tempoExecucao = tempo_fimScan - tempo_inicioScan;
        executionTimes.add(tempoExecucao); // Armazena o tempo de execução

//        Log.e("SensorTiming", "nao podes " + name + ": " +
//                (tempoExecucao / 1_000_000_000.0) + " segundos.");
//
//        Log.i("CarTask", "nao " + name + ": " + (tempo_fimScan  - tempo_inicioAtividade)/ 1_000_000_000.0) ;

        return detectedPixels;
    }
}

