package com.example.assignment2excercise;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.assignment2excercise.api.ApiService;
import com.example.assignment2excercise.api.RetrofitClient;
import com.google.firebase.FirebaseApp;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText editTextProductId, editTextProductName, editTextProductDescription, editTextProductPrice;
    private Button buttonAddProduct, buttonUpdateProduct, buttonDeleteProduct, buttonSearchProduct, buttonFetchJSON;
    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private ProductRepository productRepository;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        editTextProductId = findViewById(R.id.editTextProductId);
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductDescription = findViewById(R.id.editTextProductDescription);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        buttonAddProduct = findViewById(R.id.buttonAddProduct);
        buttonUpdateProduct = findViewById(R.id.buttonUpdateProduct);
        buttonDeleteProduct = findViewById(R.id.buttonDeleteProduct);
        buttonSearchProduct = findViewById(R.id.buttonSearchProduct);
        buttonFetchJSON = findViewById(R.id.buttonFetchJSON);

        // Initialize ProductRepository
        productRepository = new ProductRepository(this);

        // Initialize RecyclerView and ProductAdapter
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProducts.setAdapter(productAdapter);

        // Set up button listeners
        buttonAddProduct.setOnClickListener(v -> addProduct());
        buttonUpdateProduct.setOnClickListener(v -> updateProduct());
        buttonDeleteProduct.setOnClickListener(v -> deleteProduct());
        buttonSearchProduct.setOnClickListener(v -> searchProduct());
        buttonFetchJSON.setOnClickListener(v -> fetchJSONData());
    }

    private void addProduct() {
        String id = editTextProductId.getText().toString().trim();  // Get manually entered Product ID
        String name = editTextProductName.getText().toString().trim();
        String description = editTextProductDescription.getText().toString().trim();
        String priceStr = editTextProductPrice.getText().toString().trim();

        if (id.isEmpty() || name.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields, including Product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number for the price", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product(id, name, description, price);
        productRepository.addProduct(product);
        Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();

        clearFields();
    }

    private void updateProduct() {
        String id = editTextProductId.getText().toString().trim();
        String name = editTextProductName.getText().toString().trim();
        String description = editTextProductDescription.getText().toString().trim();
        String priceStr = editTextProductPrice.getText().toString().trim();

        if (id.isEmpty() || name.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields, including Product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number for the price", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product(id, name, description, price);
        productRepository.updateProduct(product);
        Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
        clearFields();
    }

    private void deleteProduct() {
        String id = editTextProductId.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(this, "Please enter Product ID to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        productRepository.deleteProduct(id);
        Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
        clearFields();
    }

    private void searchProduct() {
        String name = editTextProductName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter Product Name to search", Toast.LENGTH_SHORT).show();
            return;
        }

        productRepository.searchProduct(name, products -> {
            if (products.isEmpty()) {
                Toast.makeText(MainActivity.this, "No products found", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Products retrieved successfully", Toast.LENGTH_SHORT).show();
            }
            productAdapter.updateProductList(products);
        });
    }

    private void fetchJSONData() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Product>> call = apiService.getProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    productAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Data fetched successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        editTextProductId.setText("");
        editTextProductName.setText("");
        editTextProductDescription.setText("");
        editTextProductPrice.setText("");
    }
}
