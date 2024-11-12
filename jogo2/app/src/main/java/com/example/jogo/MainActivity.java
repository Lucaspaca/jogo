// MainActivity.java

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

public class MainActivity extends AppCompatActivity {

    public static List<Car> cars;
    private List<ImageView> carImageViews;
    private List<View> centerOfMassViews;
    private ConstraintLayout mainLayout;
    private EditText carrosInput;
    private Handler handler;
    private ImageView sensorCanvas;
    private List<Integer> lapCounts;
    private boolean isPaused = false;
    private List<Car> carRunnables;
    private List<Veiculo> veiculos;
    public static final Semaphore regionSemaphore = new Semaphore(1);
    public static int regionLeft = 150;  // Coordenada X esquerda do retângulo
    public static int regionTop = 380;   // Coordenada Y superior do retângulo
    public static int regionRight = 350; // Coordenada X direita do retângulo
    public static int regionBottom = 230; // Coordenada Y inferior do retângulo
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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

        RestrictedAreaView restrictedAreaView = new RestrictedAreaView(this);
        mainLayout.addView(restrictedAreaView); // Adiciona ao layout principal


      }

    private void startRace() {
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
            createSafetyCar();

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
        int sensorRange = 20;
        double d = sensorRange + Math.hypot(carWidth / 2.0, carHeight / 2.0);

        double initialX = 315 -(index*60);
        double initialY = 650;
        lapCounts.add(0);

        ImageView carImageView = new ImageView(this);
        carImageView.setImageResource(R.drawable.carro);
        carImageView.setLayoutParams(new ConstraintLayout.LayoutParams(40, 35));
        carImageView.setX((float) initialX);
        carImageView.setY((float) initialY - 15);
        mainLayout.addView(carImageView);
        carImageViews.add(carImageView);

        View comView = new View(this);
        comView.setBackgroundColor(Color.BLACK);
        comView.setLayoutParams(new ConstraintLayout.LayoutParams(5, 5));
        mainLayout.addView(comView);
        centerOfMassViews.add(comView);

        Car car = new Car("Carro " + (index + 1), initialX , initialY, 7, sensorRange,
                findViewById(R.id.pista), d, carImageView, comView, handler, false);
        cars.add(car);

        new Thread(car).start();
    }
    private void createSafetyCar() {
        double initialX = 380;
        double initialY = 650;
        int sensorRange = 20;
        double d = sensorRange + Math.hypot(35 / 2.0, 30 / 2.0);

        ImageView carImageView = new ImageView(this);
        carImageView.setImageResource(R.drawable.safetycar); // Imagem do safety car
        carImageView.setLayoutParams(new ConstraintLayout.LayoutParams(40, 30));
        carImageView.setX((float) initialX);
        carImageView.setY((float) initialY - 15);
        mainLayout.addView(carImageView);
        carImageViews.add(carImageView);

        View comView = new View(this);
        comView.setBackgroundColor(Color.BLACK);
        comView.setLayoutParams(new ConstraintLayout.LayoutParams(5, 5));
        mainLayout.addView(comView);
        centerOfMassViews.add(comView);

        Car safetyCar = new Car("Safety Car", initialX, initialY, 7, sensorRange,
                findViewById(R.id.pista), d, carImageView, comView, handler, true);
        cars.add(safetyCar);

        new Thread(safetyCar).start();
    }

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

}
