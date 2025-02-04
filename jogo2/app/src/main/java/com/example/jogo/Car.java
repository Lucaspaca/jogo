package com.example.jogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Looper;
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
import android.os.SystemClock;
import java.util.Collections;


import androidx.constraintlayout.widget.ConstraintLayout;

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
    private boolean isSafetyCar;
    private boolean hasWaited = false;

    private static final int MIN_SPEED = 0;
    private static final int MAX_SPEED = 9;
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
    private long lastTimestamp;
    private List<Long> flowMeterTimes;
    private long[] times = new long[10]; // vetor para armazenar até 10 tempos
    private int timeIndex = 0; // índice para inserir no vetor
    private long lastTime = -1; // variável para armazenar o tempo do último medidor
    private ImageView imageView;
    long startTime = System.nanoTime();


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
    public void setSpeed(double speed) {
       this.speed = speed;
    }
    public double getD() {
        return d;
    }

    public long getTempoInicioAtividade() {
        return tempo_inicioAtividade;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }
    private boolean waiting = false; // Flag para evitar múltiplas pausas

    private void checkWaitRegion() {
        for (Rect region : MainActivity.getWaitRegions()) {
            if (region.contains((int) x, (int) y)) {
                if (!hasWaited) {
                    long waitTime = 100 + random.nextInt(901); // Tempo entre 100 e 1000 ms
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    hasWaited = true;
                }
                return;
            }
        }
        hasWaited = false; // Reseta quando sai da região
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
        carIsAtMeter();
        tempo_inicioAtividade = System.nanoTime();
        boolean inRestrictedRegion = false;

        if (isSafetyCar) {
            setPriority(Thread.MAX_PRIORITY);
        }

        while (isRunning) {
            checkWaitRegion();
            long currentTime = System.nanoTime();
            double elapsedTime = (currentTime - startTime) / 1_000_000_000.0; // Converte para segundos
            Point target = new Point(850, 575);
            double totalTime =22; // Tempo total desejado

            double requiredSpeed = calculateRequiredSpeed(target, totalTime, elapsedTime);
            setSpeed(requiredSpeed);

            if (!isPaused) {  // Só continua se não estiver pausado
                try {

                    carIsAtMeter();
                    MainActivity.drawFlowMeters();

                    executorService.submit(new ScanForCarPixelsTask(this));
                    executorService.submit(new CalculateCenterOfMassTask(this));
                    executorService.submit(new MoveTowardsTask(this, getCenterOfMassPosition()));

                    if (isInRestrictedRegion() && !inRestrictedRegion) {
                        MainActivity.regionSemaphore.acquire();
                        inRestrictedRegion = true;
                    }

                    if (!isInRestrictedRegion() && inRestrictedRegion) {
                        MainActivity.regionSemaphore.release();
                        inRestrictedRegion = false;
                    }

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
                Thread.sleep(43); // Controle da taxa de atualização
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


    // Método para verificar se o carro está no medidor (substitua com a lógica do seu jogo)
    // Mover a lista meterPassed para fora do método, para garantir que ela seja persistente entre as chamadas
    private List<Boolean> meterPassed = new ArrayList<>();

    // Mover a lista meterPassed para fora do método, para garantir que ela seja persistente entre as chamadas


    private void carIsAtMeter() {
        List<Rect> flowMeter = MainActivity.getFlowMeter();

        // Inicializa a lista meterPassed, caso ela ainda não tenha sido inicializada
        if (meterPassed.size() != flowMeter.size()) {
            meterPassed = new ArrayList<>(Collections.nCopies(flowMeter.size(), false));
        }

        // Raio de tolerância
        final int toleranceRadius = 1;

        // Posições do carro
        double carLeft = getX();
        double carRight = carLeft + 40;  // Largura do carro
        double carTop = getY();
        double carBottom = carTop + 35;  // Altura do carro

        // Variáveis para armazenar o sensor mais próximo
        double minDistance = Double.MAX_VALUE;
        int closestMeterIndex = -1;

        for (int i = 0; i < flowMeter.size(); i++) {
            Rect meter = flowMeter.get(i);

            // Verifica se alguma parte do carro está dentro do medidor, considerando a tolerância
            boolean carIsInMeterX = (carRight >= meter.left - toleranceRadius) && (carLeft <= meter.right + toleranceRadius);
            boolean carIsInMeterY = (carBottom >= meter.top - toleranceRadius) && (carTop <= meter.bottom + toleranceRadius);

            // Se o carro estiver dentro dos limites do medidor e ainda não passou por ele
            if (carIsInMeterX && carIsInMeterY && !meterPassed.get(i)) {
                long currentTime = System.nanoTime();
                MainActivity.addFlowMeterTime(currentTime);

                // Marca esse medidor como já "passado"
                meterPassed.set(i, true);

                // Log para saber que o carro passou pelo medidor
                Log.d("CarFlowMeter", "Passou pelo medidor " + (i + 1));
            }

            // Calcular a distância do carro para o medidor
            double meterCenterX = (meter.left + meter.right) / 2.0;
            double meterCenterY = (meter.top + meter.bottom) / 2.0;
            double distance = Math.sqrt(Math.pow(carLeft - meterCenterX, 2) + Math.pow(carTop - meterCenterY, 2));

            // Verifica se esse medidor é o mais próximo
            if (distance < minDistance) {
                minDistance = distance;
                closestMeterIndex = i;
            }
        }

        // Se foi encontrado um sensor mais próximo, podemos usar a variável closestMeterIndex
        if (closestMeterIndex != -1) {
            Rect closestMeter = flowMeter.get(closestMeterIndex);
            Log.d("ClosestMeter", "Sensor mais próximo: " + (closestMeterIndex + 1) + " na posição (" + closestMeter.left + ", " + closestMeter.top + ")");
        }
    }


    public double calculateRequiredSpeed(Point target, double totalTime, double elapsedTime) {
        // Verifica se o tempo total e o tempo decorrido são válidos
        if (totalTime <= 0 || elapsedTime < 0) {
            throw new IllegalArgumentException("O tempo total deve ser maior que zero e o tempo decorrido não pode ser negativo.");
        }

        // Calcula a distância entre a posição atual do carro e o ponto alvo
        double distance = Math.hypot(target.x - this.x, target.y - this.y);

        // Calcula o tempo restante para alcançar o ponto alvo
        double remainingTime = totalTime - elapsedTime;

        // Se o tempo restante for menor ou igual a zero, retorna a velocidade máxima possível
        if (remainingTime <= 0) {
            Log.d("SpeedCalculation", "Velocidade máxima: " + MAX_SPEED);
            return MAX_SPEED; // Retorna a velocidade máxima definida
        }

        // Calcula a velocidade necessária para alcançar o ponto alvo no tempo restante
        double requiredSpeed = distance / remainingTime;

        // Isso pode ser ajustado conforme o comportamento esperado
        if (requiredSpeed > MAX_SPEED) {
            requiredSpeed = MAX_SPEED;  // Limita a velocidade máxima
        }


        return requiredSpeed;
    }









}

//qual





//    private void measureTimeAtMeter(Point meter) {
//        // Verifica se o carro passou pelo medidor
//        if (carIsAtMeter(meter)) {
//            long currentTime = System.currentTimeMillis();  // Captura o tempo atual
//
//            // Se o tempo do último medidor foi registrado, calcula o tempo
//            if (lastTime != -1) {
//                long timeDifference = currentTime - lastTime;
//                if (timeIndex < times.length) {
//                    times[timeIndex] = timeDifference; // Armazena o tempo no vetor
//                    timeIndex++; // Incrementa o índice para o próximo tempo
//
//                    // Imprime o tempo registrado no log
//                    Log.d("CarTime", "Tempo registrado no medidor " + timeIndex + ": " + timeDifference + " ms");
//                }
//            }
//
//            // Atualiza o tempo do último medidor
//            lastTime = currentTime;
//        }
//    }






