package com.example.lab5_starter;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {


    private Button addCityButton;
    private Button deleteCityButton;
    private ListView cityListView;


    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;
    private int selectedPosition = -1;


    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");


        addCityButton = findViewById(R.id.buttonAddCity);
        deleteCityButton = findViewById(R.id.buttonDeleteCity);
        cityListView = findViewById(R.id.listviewCities);


        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);


        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
                return;
            }
            if (value != null) {
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {

                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");
                    cityArrayList.add(new City(name, province));
                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });

        // 5. Add Button Listener
        addCityButton.setOnClickListener(view -> {
            new CityDialogFragment().show(getSupportFragmentManager(), "Add City");
        });


        deleteCityButton.setOnClickListener(view -> {
            if (selectedPosition != -1 && selectedPosition < cityArrayList.size()) {
                City cityToDelete = cityArrayList.get(selectedPosition);


                deleteCity(cityToDelete);


                selectedPosition = -1;
                clearListSelection();
            } else {
                Toast.makeText(this, "Please select a city to delete", Toast.LENGTH_SHORT).show();
            }
        });


        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedPosition = i;


            clearListSelection();
            view.setBackgroundColor(Color.LTGRAY);
        });
    }


    private void clearListSelection() {
        for (int i = 0; i < cityListView.getChildCount(); i++) {
            View v = cityListView.getChildAt(i);
            if (v != null) v.setBackgroundColor(Color.TRANSPARENT);
        }
    }



    @Override
    public void addCity(City city) {

        citiesRef.document(city.getName()).set(city)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "City added!"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding city", e));
    }

    @Override
    public void deleteCity(City city) {

        citiesRef.document(city.getName()).delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "City deleted!"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting city", e));
    }

    @Override
    public void updateCity(City city, String newName, String newProvince) {

        String oldName = city.getName();
        if (oldName.equals(newName)) {
            citiesRef.document(oldName).update("province", newProvince);
        } else {
            citiesRef.document(oldName).delete();
            citiesRef.document(newName).set(new City(newName, newProvince));
        }
    }
}