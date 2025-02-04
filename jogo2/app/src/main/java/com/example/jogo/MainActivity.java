package com.example.jogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Map;
import java.util.HashMap;
import android.util.Log;
import com.example.mathlibrary.FirebaseUtils;
import android.graphics.Paint;
import android.graphics.Rect;



public class MainActivity extends AppCompatActivity {

    public static List<Car> cars;
    private List<ImageView> carImageViews;
    private List<View> centerOfMassViews;
    private ConstraintLayout mainLayout;
    private EditText carrosInput;
    private Handler handler;
    private static ImageView sensorCanvas;
    private List<Integer> lapCounts;
    private boolean isPaused = false;
    private List<Car> carRunnables;
    private List<Veiculo> veiculos;
    public static final Semaphore regionSemaphore = new Semaphore(1);
    public static int regionLeft = 570;  // Coordenada X esquerda do retângulo
    public static int regionTop = 455;   // Coordenada Y superior do retângulo
    public static int regionRight = 525; // Coordenada X direita do retângulo
    public static int regionBottom = 495; // Coordenada Y inferior do retângulo
    private FirebaseFirestore db;
    private long tempoInicioAtividade;
    public static List<Rect> flowMeter = new ArrayList<>();
    public static List<Rect> getFlowMeter() {
        return flowMeter;
    }
    static {
        // Definindo os medidores como retângulos com posição e tamanho (especificando largura e altura)
        // Rota 1
//        flowMeter.add(new Rect(130, 150, 131, 200));  // Medidor 1
//        flowMeter.add(new Rect(280, 150, 281, 200));  // Medidor 2
//        flowMeter.add(new Rect(530, 150, 531, 200));  // Medidor 3
//        flowMeter.add(new Rect(775, 150, 776, 200));  // Medidor 4
//        flowMeter.add(new Rect(1015, 250, 1075, 251));  // Medidor 5
//        flowMeter.add(new Rect(715, 290, 716, 340));  // Medidor 6
//        flowMeter.add(new Rect(460, 320, 461, 370));  // Medidor 7
//        flowMeter.add(new Rect(270, 320, 271, 370));  // Medidor 10
//        flowMeter.add(new Rect(185, 395, 235, 396));  // Medidor 11
//        flowMeter.add(new Rect(185, 509, 235, 510));  // Medidor 17
//        flowMeter.add(new Rect(320, 550, 321, 600));  // Medidor 19
//        flowMeter.add(new Rect(850, 550, 851, 600));  // Medidor 21
        // Rota 2
        flowMeter.add(new Rect(130, 150, 131, 200));  // Medidor 1
        flowMeter.add(new Rect(280, 150, 281, 200));  // Medidor 2
        flowMeter.add(new Rect(530, 150, 531, 200));  // Medidor 3
        flowMeter.add(new Rect(775, 150, 776, 200));  // Medidor 4
        flowMeter.add(new Rect(1015, 250, 1075, 251));  // Medidor 5
        flowMeter.add(new Rect(1015, 400, 1075, 401));  // Medidor 16
        flowMeter.add(new Rect(775, 450, 776, 500));  // Medidor 15
        flowMeter.add(new Rect(525, 410, 575, 411));  // Medidor 14
        flowMeter.add(new Rect(465, 395, 515, 396));  // Medidor 13
        flowMeter.add(new Rect(465, 515, 515, 516));  // Medidor 18
        flowMeter.add(new Rect(850, 550, 851, 600));  // Medidor 21


    }
    public static long tempo_inicioAtividade;
    public static List<Rect> waitRegions = new ArrayList<>();

    static {
        // Regiões onde o carro deve esperar entre 100 ms e 1000 ms
        // Rota 1
//        waitRegions.add(new Rect(190, 330, 230, 370));  // Exemplo de região de espera 1
//        waitRegions.add(new Rect(190, 430, 230, 470));  // Exemplo de região de espera 2
//        waitRegions.add(new Rect(350, 330, 390, 370));  // Exemplo de região de espera 3
        // Rota 2
        waitRegions.add(new Rect(470, 450, 500, 490));  // Exemplo de região de espera 1
    }

    public static List<Rect> getWaitRegions() {
        return waitRegions;
    }






//    // Definindo os medidores como retângulos com posição e tamanho (especificando largura e altura)
//        flowMeter.add(new Rect(130, 150, 131, 200));  // Medidor 1
//        flowMeter.add(new Rect(280, 150, 281, 200));  // Medidor 2
////        flowMeter.add(new Rect(530, 150, 531, 200));  // Medidor 3
//        flowMeter.add(new Rect(775, 150, 776, 200));  // Medidor 4
//        flowMeter.add(new Rect(1015, 250, 1075, 251));  // Medidor 5
////        flowMeter.add(new Rect(715, 290, 716, 340));  // Medidor 6
////        flowMeter.add(new Rect(460, 320, 461, 370));  // Medidor 7
////        flowMeter.add(new Rect(350, 270, 400, 271));  // Medidor 8
////        flowMeter.add(new Rect(185, 270, 235, 271));  // Medidor 9
////        flowMeter.add(new Rect(270, 320, 271, 370));  // Medidor 10
////        flowMeter.add(new Rect(185, 395, 235, 396));  // Medidor 11
////        flowMeter.add(new Rect(320, 420, 321, 470));  // Medidor 12
////        flowMeter.add(new Rect(465, 395, 515, 396));  // Medidor 13
////        flowMeter.add(new Rect(525, 410, 575, 411));  // Medidor 14
//        flowMeter.add(new Rect(1015, 400, 1075, 401));  // Medidor 16
//        flowMeter.add(new Rect(775, 450, 776, 500));  // Medidor 15
////        flowMeter.add(new Rect(1015, 400, 1075, 401));  // Medidor 16
////        flowMeter.add(new Rect(185, 509, 235, 510));  // Medidor 17
////        flowMeter.add(new Rect(465, 515, 515, 516));  // Medidor 18
////        flowMeter.add(new Rect(320, 550, 321, 600));  // Medidor 19
////        flowMeter.add(new Rect(525, 520, 575, 521));  // Medidor 20
////        flowMeter.add(new Rect(320, 550, 321, 600));  // Medidor 19
////        flowMeter.add(new Rect(850, 550, 851, 600));  // Medidor 21




    public static List<Long> flowMeterTimes = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tempo_inicioAtividade = System.nanoTime();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempoInicioAtividade = System.nanoTime();
        veiculos = new ArrayList<>();
        cars = new ArrayList<>();
        carImageViews = new ArrayList<>();
        centerOfMassViews = new ArrayList<>();
        lapCounts = new ArrayList<>();
        carRunnables = new ArrayList<>();
        handler = new Handler();
        db = FirebaseFirestore.getInstance();

        mainLayout = findViewById(R.id.main);
        carrosInput = findViewById(R.id.carros_input);
        sensorCanvas = findViewById(R.id.sensor_canvas);

        ImageButton startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> startRace());

        ImageButton pauseButton = findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(v -> togglePause());

        ImageButton finishButton = findViewById(R.id.finish_button);
        finishButton.setOnClickListener(v -> finishRace());
        sensorCanvas.getViewTreeObserver().addOnGlobalLayoutListener(() -> drawFlowMeters());


    }

    public static void drawFlowMeters() {
        int width = sensorCanvas.getWidth();
        int height = sensorCanvas.getHeight();

        if (width == 0 || height == 0) {
            Log.e("FlowMeter", "Dimensões inválidas para desenhar os medidores.");
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);  // Cor dos medidores
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        Paint waitRegionPaint = new Paint();
        waitRegionPaint.setColor(Color.RED);  // Cor das áreas de espera
        waitRegionPaint.setStyle(Paint.Style.FILL);

        // Desenhando os medidores de fluxo
        for (Rect meter : flowMeter) {
            canvas.drawRect(meter.left, meter.top, meter.right, meter.bottom, paint);
        }

        // Configuração do texto dos tempos
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLUE);
        textPaint.setTextSize(30); // Tamanho da fonte para os tempos
        textPaint.setTextAlign(Paint.Align.CENTER);


        // Desenhando as áreas de espera
        for (Rect waitRegion : waitRegions) {
            canvas.drawRect(waitRegion, waitRegionPaint);
        }

        // Percorrer os medidores e exibir os tempos na posição do medidor correspondente
        // Percorrer os medidores e calcular o intervalo de tempo
        for (int i = 0; i < flowMeter.size(); i++) {
            // Verifica se já há tempo registrado para o medidor atual
            if (i < flowMeterTimes.size()) {
                long tempoAtual = flowMeterTimes.get(i);
                String tempoTexto = String.format("%.2f s", (tempoAtual - tempo_inicioAtividade) / 1_000_000_000.0);

                // Se i > 0, calcular o intervalo de tempo entre o medidor atual e o anterior
                if (i > 0) {
                    long tempoAnterior = flowMeterTimes.get(i - 1);
                    long intervaloTempo = tempoAtual - tempoAnterior;
                    tempoTexto = String.format("%.2f s", (intervaloTempo) / 1_000_000_000.0);
                }

                // Pegamos a posição do medidor para desenhar o tempo diretamente nele
                Rect meter = flowMeter.get(i);

                // Usar os valores diretamente para calcular o centro
                float x = (meter.left + meter.right) / 2f;  // Centraliza no eixo X
                float y = meter.bottom - 15;  // Coloca um pouco abaixo do medidor para melhor visibilidade

                // Desenha o tempo ou intervalo de tempo na posição do medidor
                canvas.drawText(tempoTexto, x, y, textPaint);
            }
        }

        sensorCanvas.setImageBitmap(bitmap);
    }



    public static void addFlowMeterTime(long time) {
        flowMeterTimes.add(time);
    }



    private void startRace() {
        tempo_inicioAtividade = System.nanoTime();
        cars.clear();
        carImageViews.forEach(mainLayout::removeView);
        carImageViews.clear();
        centerOfMassViews.forEach(mainLayout::removeView);
        centerOfMassViews.clear();
        carRunnables.forEach(Car::parar); // Usa o método parar para interromper as threads
        carRunnables.clear();

        retrieveSavedCars(); // Recupera o estado dos carros salvos no Firestore antes de iniciar a corrida

        String inputText = carrosInput.getText().toString();
        int numCars = inputText.isEmpty() ? 0 : Integer.parseInt(inputText);

        if (numCars > 0) {
            for (int i = 0; i < numCars; i++) {
                createCar(i);
            }
            isPaused = false;
//            createSafetyCar();

            for (Veiculo veiculo : veiculos) {
                new Thread((Runnable) veiculo).start();
            }
        }
    }




    private void togglePause() {
        isPaused = !isPaused;

        for (Car car : cars) {
            car.setPaused(isPaused);

        }


        if (isPaused) {
            saveCarStates(); // Salva o estado dos carros ao pausar
        }
    }

    private void finishRace() {
        isPaused = true;
        flowMeterTimes.clear();
        // Para todos os carros na corrida
        for (Car car : cars) {
            car.parar();
        }

        saveCarStates(); // Salva o estado dos carros ao finalizar

        handler.postDelayed(() -> {
            for (ImageView carImageView : carImageViews) {
                mainLayout.removeView(carImageView);
            }
            carImageViews.clear();
            cars.clear();

            // Limpa o banco de dados após a finalização da corrida
            FirebaseUtils.clearDatabase(new FirebaseUtils.DatabaseClearCallback() {
                @Override
                public void onSuccess() {
                    Log.d("MainActivity", "Banco de dados apagado com sucesso após a finalização da corrida!");
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("MainActivity", "Erro ao apagar banco de dados: " + e.getMessage());
                }
            });
        }, 100);
    }




    private void createCar(int index) {
        double carWidth = 35;
        double carHeight = 30;
        int sensorRange = 33;
        double d = sensorRange + Math.hypot(carWidth / 2.0, carHeight / 2.0);

        double initialX = 100; // Posição X comum
        double initialY = 180; // Posição Y comum

        lapCounts.add(0);

        // Criação do ImageView para o carro
        ImageView carImageView = new ImageView(this);
        carImageView.setImageResource(R.drawable.carro);
        carImageView.setLayoutParams(new ConstraintLayout.LayoutParams(40, 35));
        carImageView.setX((float) initialX);
        carImageView.setY((float) initialY - 15);
        mainLayout.addView(carImageView);
        carImageViews.add(carImageView);

        // Criação do View para o centro de massa do carro
        View comView = new View(this);
        comView.setBackgroundColor(Color.BLACK);
        comView.setLayoutParams(new ConstraintLayout.LayoutParams(5, 5));
        mainLayout.addView(comView);
        centerOfMassViews.add(comView);

        // Criação do carro
        Car car = new Car("Carro " + (index + 1), initialX , initialY,7 , sensorRange,
                findViewById(R.id.pista), d, carImageView, comView, handler, false);
        cars.add(car);

        // Adicionando o delay entre a criação dos carros
        long delay = index * 750; // Define o tempo de delay (exemplo: 1 segundo entre cada carro)

        // Usando Handler para executar a criação do carro com delay
        handler.postDelayed(() -> {
            new Thread(car).start();
        }, delay);
    }

//    private void createSafetyCar() {
//        double initialX = 380;
//        double initialY = 650;
//        int sensorRange = 20;
//        double d = sensorRange + Math.hypot(35 / 2.0, 30 / 2.0);
//
//        ImageView carImageView = new ImageView(this);
//        carImageView.setImageResource(R.drawable.safetycar); // Imagem do safety car
//        carImageView.setLayoutParams(new ConstraintLayout.LayoutParams(40, 30));
//        carImageView.setX((float) initialX);
//        carImageView.setY((float) initialY - 15);
//        mainLayout.addView(carImageView);
//        carImageViews.add(carImageView);
//
//        View comView = new View(this);
//        comView.setBackgroundColor(Color.BLACK);
//        comView.setLayoutParams(new ConstraintLayout.LayoutParams(5, 5));
//        mainLayout.addView(comView);
//        centerOfMassViews.add(comView);
//
//        Car safetyCar = new Car("Safety Car", initialX, initialY, 7, sensorRange,
//                findViewById(R.id.pista), d, carImageView, comView, handler, true);
//        cars.add(safetyCar);
//
//        new Thread(safetyCar).start();
//    }


    private void restoreCar(Map<String, Object> carData) {
        double x = (double) carData.get("x");
        double y = (double) carData.get("y");
        double rotation = (double) carData.get("rotation"); // Recuperando a rotação
        double speed = (double) carData.get("speed");
        int sensorRange = ((Long) carData.get("sensorRange")).intValue();
        boolean isSafetyCar = (boolean) carData.get("isSafetyCar");

        double carWidth = 35;
        double carHeight = 30;
        double d = sensorRange + Math.hypot(carWidth / 2.0, carHeight / 2.0);

        // Recupera a posição frontal e o ângulo do carro
        int frontX = ((Long) carData.get("frontX")).intValue();
        int frontY = ((Long) carData.get("frontY")).intValue();
        double angle = (double) carData.get("angle");

        ImageView carImageView = new ImageView(this);
        carImageView.setImageResource(isSafetyCar ? R.drawable.safetycar : R.drawable.carro);
        carImageView.setLayoutParams(new ConstraintLayout.LayoutParams(40, 35));
        carImageView.setX((float) x);
        carImageView.setY((float) y - 15);
        mainLayout.addView(carImageView);
        carImageViews.add(carImageView);

        View comView = new View(this);
        comView.setBackgroundColor(Color.BLACK);
        comView.setLayoutParams(new ConstraintLayout.LayoutParams(5, 5));
        mainLayout.addView(comView);
        centerOfMassViews.add(comView);

        // Cria o carro com a posição e ângulo restaurados
        Car car = new Car((String) carData.get("name"), x, y, (int) speed, sensorRange,
                findViewById(R.id.pista), d, carImageView, comView, handler, isSafetyCar);

        car.setRotation((float) rotation); // Define a rotação inicial para manter a direção
        car.setAngle(angle); // Define o ângulo restaurado
        car.setFrontPosition(new Point(frontX, frontY)); // Define a posição frontal restaurada

        cars.add(car);
        new Thread(car).start();
    }



    private void retrieveSavedCars() {
        FirebaseUtils.retrieveSavedCars(new FirebaseUtils.CarRetrieveCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> carsData) {
                for (Map<String, Object> carData : carsData) {
                    restoreCar(carData); // Restaura cada carro a partir dos dados recuperados
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("MainActivity", "Erro ao recuperar dados dos carros: " + e.getMessage());
            }
        });
    }
    private void saveCarStates() {
        for (Car car : cars) {
            FirebaseUtils.saveCarState(car.getName(), car.toMap(), new FirebaseUtils.CarSaveCallback() {
                @Override
                public void onSuccess() {
                    // Carro salvo com sucesso
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("MainActivity", "Erro ao salvar estado do carro: " + e.getMessage());
                }
            });
        }
    }
    public static List<Rect> getFlowMeterPoints() {
        return flowMeter;
    }

}