package com.example.mathlibrary;
import org.junit.Test;
import static org.junit.Assert.*;

import android.graphics.Point;

public class MathUtilsTest {

    @Test
    public void testGetNewTargetPosition() {
        // Teste com um deslocamento de 0 graus (movendo-se para a direita)
        Point currentPosition = new Point(0, 0);
        double angle = 0; // 0 graus
        double distance = 10;
        Point expectedPosition = new Point(10, 0); // Espera-se que a nova posição seja (10, 0)
        Point result = MathUtils.getNewTargetPosition(currentPosition, angle, distance);

        // Comparação manual
        assertTrue(result.x == expectedPosition.x && result.y == expectedPosition.y);

        // Teste com um deslocamento de 90 graus (movendo-se para cima)
        angle = 90; // 90 graus
        expectedPosition = new Point(0, 10); // Espera-se que a nova posição seja (0, 10)
        result = MathUtils.getNewTargetPosition(currentPosition, angle, distance);
        assertTrue(result.x == expectedPosition.x && result.y == expectedPosition.y);
    }
}
