// FirebaseUtils.java

package com.example.mathlibrary;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import android.util.Log;

public class FirebaseUtils {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface CarSaveCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
    public interface DatabaseClearCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface CarRetrieveCallback {
        void onSuccess(List<Map<String, Object>> carsData);
        void onFailure(Exception e);
    }

    public static void saveCarState(String carName, Map<String, Object> carData, CarSaveCallback callback) {
        db.collection("carros").document(carName)
                .set(carData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public static void retrieveSavedCars(CarRetrieveCallback callback) {
        db.collection("carros").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> carsData = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            carsData.add(document.getData());
                        }
                        callback.onSuccess(carsData);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }
    public static void clearDatabase(DatabaseClearCallback callback) {
        db.collection("carros").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        WriteBatch batch = db.batch();
                        QuerySnapshot querySnapshot = task.getResult();

                        for (DocumentSnapshot document : querySnapshot) {
                            batch.delete(document.getReference());
                        }

                        batch.commit().addOnSuccessListener(aVoid -> {
                            callback.onSuccess();
                        }).addOnFailureListener(e -> {
                            callback.onFailure(e);
                            Log.e("FirebaseUtils", "Erro ao apagar documentos: " + e.getMessage());
                        });
                    } else {
                        callback.onFailure(task.getException());
                        Log.e("FirebaseUtils", "Erro ao buscar documentos: " + task.getException().getMessage());
                    }
                });
    }

}