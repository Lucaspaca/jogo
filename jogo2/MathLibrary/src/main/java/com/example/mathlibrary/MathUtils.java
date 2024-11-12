package com.example.mathlibrary;

import android.graphics.Point;
import android.graphics.Bitmap;
import android.graphics.Color;
import java.util.List;
import java.util.ArrayList;

public class MathUtils {

    public static Point calculateCenterOfMass(List<Point> whitePixels) {
        if (whitePixels == null || whitePixels.isEmpty()) return null;

        int sumX = 0;
        int sumY = 0;
        for (Point p : whitePixels) {
            sumX += p.x;
            sumY += p.y;
        }

        int centerX = sumX / whitePixels.size();
        int centerY = sumY / whitePixels.size();

        return new Point(centerX, centerY);
    }

    public static Point moveTowards(Point current, Point target, double speed) {
        if (target == null) return current;

        double deltaX = target.x - current.x;
        double deltaY = target.y - current.y;
        double angleToTarget = Math.atan2(deltaY, deltaX);

        int newX = (int) (current.x + Math.cos(angleToTarget) * speed);
        int newY = (int) (current.y + Math.sin(angleToTarget) * speed);

        return new Point(newX, newY);
    }

    public static double calculateAngle(Point current, Point target) {
        if (target == null) return 0;

        double deltaX = target.x - current.x;
        double deltaY = target.y - current.y;
        return Math.toDegrees(Math.atan2(deltaY, deltaX));
    }

    public static double getD(int sensorRange, int carWidth, int carHeight) {
        return sensorRange + Math.hypot(carWidth / 2.0, carHeight / 2.0);
    }

    public static List<Point> scanForWhitePixels(Bitmap bitmap, Point frontPosition, double angle, int sensorRange, double d) {
        List<Point> whitePixels = new ArrayList<>();

        if (bitmap == null || frontPosition == null) return whitePixels;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        double angleRad = Math.toRadians(angle);
        double halfConeRad = Math.PI / 2; // 45 graus para cada lado
        int sensorRangeSquared = sensorRange * sensorRange;

        for (int i = -sensorRange; i <= sensorRange; i++) {
            for (int j = -sensorRange; j <= sensorRange; j++) {
                if (i * i + j * j > sensorRangeSquared) {
                    continue;
                }

                double distanceToCar = Math.sqrt(i * i + j * j);
                if (distanceToCar > d) {
                    continue;
                }

                double pixelAngle = Math.atan2(j, i);
                double angleDiff = pixelAngle - angleRad;

                if (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
                if (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

                if (angleDiff >= -halfConeRad && angleDiff <= halfConeRad) {
                    int pixelX = frontPosition.x + i;
                    int pixelY = frontPosition.y + j;

                    if (pixelX >= 0 && pixelX < width && pixelY >= 0 && pixelY < height) {
                        int pixelColor = bitmap.getPixel(pixelX, pixelY);
                        if (Color.red(pixelColor) == 255 && Color.green(pixelColor) == 255 && Color.blue(pixelColor) == 255) {
                            whitePixels.add(new Point(pixelX, pixelY));
                        }
                    }
                }
            }
        }
        return whitePixels;
    }

    public static Point getFrontPosition(Point current, double angle) {
        double angleRad = Math.toRadians(angle);
        int frontX = (int) (current.x + Math.cos(angleRad) * 30);
        int frontY = (int) (current.y + Math.sin(angleRad) * 40);
        return new Point(frontX, frontY);
    }

    public static float updateRotation(float currentRotation, double angleToTarget) {
        float rotationDifference = (float) (angleToTarget - currentRotation);
        rotationDifference = (rotationDifference + 180) % 360 - 180;

        if (Math.abs(rotationDifference) > 5) {
            rotationDifference = (rotationDifference > 0 ? 5 : -5);
        }

        return currentRotation + rotationDifference;
    }
    public static Point getNewTargetPosition(Point currentPosition, double angle, double distance) {
        int newX = (int) (currentPosition.x + Math.cos(Math.toRadians(angle)) * distance);
        int newY = (int) (currentPosition.y + Math.sin(Math.toRadians(angle)) * distance);
        return new Point(newX, newY);
    }
}
