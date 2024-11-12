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
import java.util.concurrent.Semaphore;
import android.os.Handler;
import java.util.Map;
import java.util.HashMap;
import com.example.mathlibrary.MathUtils;

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

    // Getters e Setters
    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }
    public int getPenalty() {
        return this.penalty;
    }

    public double getSpeed() {
        return this.speed;
    }

    public int getSensorRange() {
        return this.sensorRange;
    }

    public double getAngle() {
        return this.angle;
    }

    public Point getCenterOfMass() {
        return this.centerOfMass;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }


    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public void updateBitmap() {
        this.bitmap = getBitmapFromView(trackView);
    }
    private void setRandomSpeed() {
        this.speed = MIN_SPEED + random.nextInt(MAX_SPEED - MIN_SPEED + 1);
    }

    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
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
        boolean inRestrictedRegion = false; // Controle para saber se o carro está na região
        if (isSafetyCar) {
            setPriority(Thread.MAX_PRIORITY); // Atribuir prioridade máxima
        }
        while (isRunning) {
            if (!isPaused) {
                try {
                    // Ajusta a velocidade do carro aleatoriamente entre 5 e 25
                    setRandomSpeed();

                    // Verifica se o carro está para entrar na região restrita
                    if (isInRestrictedRegion() && !inRestrictedRegion) {
                        MainActivity.regionSemaphore.acquire(); // Tenta adquirir o permit
                        inRestrictedRegion = true; // Marca que o carro está na região
                    }

                    // Se o carro saiu da região restrita, libera o permit
                    if (!isInRestrictedRegion() && inRestrictedRegion) {
                        MainActivity.regionSemaphore.release();
                        inRestrictedRegion = false;
                    }

                    moveTowards(getCenterOfMassPosition());
                    checkCollision();

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

        // Se o carro estiver na região restrita ao parar, libera o semaphore
        if (inRestrictedRegion) {
            MainActivity.regionSemaphore.release();
        }
    }


        public void updateRotation(Point target) {
        if (target != null) {
            double angleToTarget = MathUtils.calculateAngle(getPosicao(), target);
            float newRotation = MathUtils.updateRotation(carImageView.getRotation(), angleToTarget);
            carImageView.setRotation(newRotation);
        }
    }

    public List<Point> scanForWhitePixels() {
        Point frontPosition = getFrontPosition();
        return MathUtils.scanForWhitePixels(bitmap, frontPosition, angle, sensorRange, d);
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public double getD() {
        return this.d; // Retorna o valor de d
    }

    public Point calculateCenterOfMass() {
        // Usa o novo método para obter pixels brancos e dos carros
        List<Point> relevantPixels = scanForCarPixels();
        centerOfMass = MathUtils.calculateCenterOfMass(relevantPixels);
        return centerOfMass;
    }

    @Override
    public void moveTowards(Point target) {
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
    public void mover() {
        if (!isPaused) {
            moveTowards(getCenterOfMassPosition());
        }
    }

    @Override
    public void parar() {
        isRunning = false;
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
    public boolean isInRestrictedRegion() {
        return x >= MainActivity.regionLeft && x <= MainActivity.regionRight &&
                y >= MainActivity.regionBottom && y <= MainActivity.regionTop;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> carData = new HashMap<>();
        carData.put("name", name);
        carData.put("x", x);
        carData.put("y", y);
        carData.put("speed", speed);
        carData.put("sensorRange", sensorRange);
        carData.put("isSafetyCar", isSafetyCar);
        carData.put("penalty", penalty);
        // Adicione outros atributos necessários
        return carData;
    }

    public List<Point> scanForCarPixels() {
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

        return detectedPixels;
    }

}

