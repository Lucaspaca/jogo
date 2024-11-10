package com.example.jogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.view.View;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
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

    public void checkCollision() {
        // Verifica colisão com bordas da pista
        if (x < 0 || x > trackView.getWidth() || y < 0 || y > trackView.getHeight()) {
            penalty++;
        }

        // Verifica colisão com outros carros
        for (Car otherCar : MainActivity.cars) {  // Supondo que cars é acessível como lista pública ou estática.
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
                    // Verifica se o carro está para entrar na região restrita
                    if (MathUtils.isInRestrictedRegion((int) x, (int) y, MainActivity.regionLeft, MainActivity.regionRight, MainActivity.regionBottom, MainActivity.regionTop) && !inRestrictedRegion) {
                        MainActivity.regionSemaphore.acquire(); // Tenta adquirir o permissão
                        inRestrictedRegion = true; // Marca que o carro está na região
                    }

                    // Se o carro saiu da região restrita, libera o permissão
                    if (!MathUtils.isInRestrictedRegion((int) x, (int) y, MainActivity.regionLeft, MainActivity.regionRight, MainActivity.regionBottom, MainActivity.regionTop) && inRestrictedRegion) {
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
                Thread.sleep(80);
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
            double deltaX = target.x - x;
            double deltaY = target.y - y;
            double angleToTarget = Math.toDegrees(Math.atan2(deltaY, deltaX));
            float currentRotation = carImageView.getRotation();
            float rotationDifference = (float) (angleToTarget - currentRotation);
            rotationDifference = (rotationDifference + 180) % 360 - 180;

            if (Math.abs(rotationDifference) > 5) {
                rotationDifference = (rotationDifference > 0 ? 5 : -5);
            }
            carImageView.setRotation(currentRotation + rotationDifference);
        }
    }
    public List<Point> scanForWhitePixels() {
        List<Point> whitePixels = new ArrayList<>();

        if (bitmap != null) {
            Point frontPosition = getFrontPosition();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            double angleRad = Math.toRadians(angle);
            double halfConeRad = Math.PI / 2; // 45 graus para cada lado

            int sensorRangeSquared = sensorRange * sensorRange;

            for (int i = -sensorRange; i <= sensorRange; i++) {
                for (int j = -sensorRange; j <= sensorRange; j++) {
                    if (i * i + j * j > sensorRangeSquared) {
                        continue; // fora do alcance circular do sensor
                    }

                    // Verifica se a distância ao centro do carro está dentro do raio d
                    double distanceToCar = Math.sqrt(i * i + j * j);
                    if (distanceToCar > d) {
                        continue; // fora do alcance de leitura
                    }

                    // Cálculo do ângulo do pixel em relação à direção do sensor
                    double pixelAngle = Math.atan2(j, i);
                    double angleDiff = pixelAngle - angleRad;

                    // Ajusta a diferença de ângulo para o intervalo -PI a PI
                    if (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
                    if (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

                    if (angleDiff >= -halfConeRad && angleDiff <= halfConeRad) {
                        int pixelX = frontPosition.x + i;
                        int pixelY = frontPosition.y + j;

                        // Verifica se o pixel está dentro do bitmap
                        if (pixelX >= 0 && pixelX < width && pixelY >= 0 && pixelY < height) {
                            int pixelColor = bitmap.getPixel(pixelX, pixelY);
                            if (Color.red(pixelColor) == 255 && Color.green(pixelColor) == 255 && Color.blue(pixelColor) == 255) {
                                whitePixels.add(new Point(pixelX, pixelY));
                            }
                        }
                    }
                }
            }
        }
        return whitePixels;
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
        List<Point> whitePixels = scanForWhitePixels();

        int sumX = 0;
        int sumY = 0;
        for (Point p : whitePixels) {
            sumX += p.x;
            sumY += p.y;
        }

        int centerX = sumX / whitePixels.size();
        int centerY = sumY / whitePixels.size();

        centerOfMass = new Point(centerX, centerY);
        return centerOfMass;
    }

    public void moveTowards(Point target) {
        if (target != null) {
            double deltaX = target.x - x;
            double deltaY = target.y - y;
            double angleToTarget = Math.atan2(deltaY, deltaX);

            // Movimentação do carro em direção ao alvo
            x += Math.cos(angleToTarget) * speed;
            y += Math.sin(angleToTarget) * speed;

            // Atualiza o ângulo de direção
            angle = Math.toDegrees(angleToTarget);

        }
    }

    public Point getFrontPosition() {
        double angleRad = Math.toRadians(angle);
        int frontX = (int) (x + Math.cos(angleRad) * 30);
        int frontY = (int) (y + Math.sin(angleRad) * 40);
        return new Point(frontX, frontY);
    }

    public Point getCenterOfMassPosition() {
        Point centerMass = calculateCenterOfMass();
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
//    private boolean isInRestrictedRegion() {
//        return x >= MainActivity.regionLeft && x <= MainActivity.regionRight &&
//                y >= MainActivity.regionBottom && y <= MainActivity.regionTop;
//    }

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

}

