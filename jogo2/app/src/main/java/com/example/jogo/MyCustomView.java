package com.example.jogo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.View; // Importando a classe View

import java.util.ArrayList;
import java.util.List;


public class MyCustomView extends View {
    private List<Point> flowMeters; // Lista de medidores de fluxo
    private Paint paint; // Para configurar a aparência dos medidores

    public MyCustomView(Context context) {
        super(context);
        // Inicializa a lista de medidores de fluxo
        flowMeters = new ArrayList<>();
        // Adiciona alguns medidores de fluxo de exemplo (você pode modificar isso)
        flowMeters.add(new Point(300, 180)); // Exemplo de posição
        flowMeters.add(new Point(300, 400)); // Exemplo de posição

        // Inicializa o objeto Paint para personalizar a aparência
        paint = new Paint();
        paint.setColor(Color.RED); // Cor vermelha para os medidores
        paint.setStyle(Paint.Style.FILL); // Preenchimento sólido
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); // Chama o método da superclasse

        // Desenha os medidores de fluxo
        for (Point meter : flowMeters) {
            // Desenha um círculo para cada medidor de fluxo
            canvas.drawCircle(meter.x, meter.y, 10, paint); // O raio é 10 (você pode ajustar)
        }
    }
}
