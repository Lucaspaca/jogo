//package com.example.mathlibrary;
//
//import org.junit.Test;
//import java.awt.Point;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import static org.junit.Assert.*;
//
//public class CenterOfMassTest {
//
//    @Test
//    public void testCalculateCenterOfMass_withValidPoints() {
//        // Lista de pontos brancos simulados
//        List<Point> whitePixels = Arrays.asList(
//                new Point(2, 3),
//                new Point(4, 5),
//                new Point(6, 7)
//        );
//
//        // Calcula o centro de massa esperado
//        Point expectedCenterOfMass = new Point(4, 5);
//
//        // Executa o método e verifica se o resultado está correto
//        Point result = MainActivity.calculateCenterOfMass(whitePixels);
//        assertEquals(expectedCenterOfMass, result);
//    }
//
//    @Test
//    public void testCalculateCenterOfMass_withSinglePoint() {
//        // Lista com apenas um ponto
//        List<Point> whitePixels = Arrays.asList(new Point(10, 10));
//
//        // Espera-se que o centro de massa seja o próprio ponto
//        Point expectedCenterOfMass = new Point(10, 10);
//
//        // Executa o método e verifica o resultado
//        Point result = MainActivity.calculateCenterOfMass(whitePixels);
//        assertEquals(expectedCenterOfMass, result);
//    }
//
//    @Test
//    public void testCalculateCenterOfMass_withEmptyList() {
//        // Lista vazia de pontos brancos
//        List<Point> whitePixels = new ArrayList<>();
//
//        // O resultado esperado deve ser null
//        Point result = MainActivity.calculateCenterOfMass(whitePixels);
//        assertNull(result);
//    }
//
//    @Test
//    public void testCalculateCenterOfMass_withNullList() {
//        // Lista nula
//        List<Point> whitePixels = null;
//
//        // O resultado esperado deve ser null
//        Point result = MainActivity.calculateCenterOfMass(whitePixels);
//        assertNull(result);
//    }
//}
