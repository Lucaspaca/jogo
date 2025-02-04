// RestrictedAreaView.java
package com.example.jogo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class RestrictedAreaView extends View {
    private Paint paint;

    public RestrictedAreaView(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.argb(255, 255, 0, 0)); // Cor da zona restrita
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Desenha a zona restrita
        canvas.drawRect(MainActivity.regionLeft, MainActivity.regionBottom,
                MainActivity.regionRight, MainActivity.regionTop, paint);
    }
}
