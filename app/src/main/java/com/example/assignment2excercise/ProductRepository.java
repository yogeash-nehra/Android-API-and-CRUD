package com.example.assignment2excercise;

import android.content.Context;
import android.widget.Toast;

import com.example.assignment2excercise.api.ApiService;
import com.example.assignment2excercise.api.RetrofitClient;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {
    private final FirebaseFirestore db;
    private final CollectionReference productsRef;
    private final Context context;
    private ApiService apiService;

    // Callback interface for retrieving products
    public interface OnProductsRetrievedListener {
        void onProductsRetrieved(List<Product> products);
    }

    // Constructor
    public ProductRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.productsRef = db.collection("products");
        this.context = context;
        this.apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    // Method to add a product to Firestore
    public void addProduct(Product product) {
        productsRef.document(product.getId()) // Assumes ID is manually provided
                .set(product)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Product added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error adding product: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Method to update a product in Firestore
    public void updateProduct(Product product) {
        if (product.getId() == null) {
            Toast.makeText(context, "Product ID is required for update", Toast.LENGTH_SHORT).show();
            return;
        }

        productsRef.document(product.getId())
                .set(product)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Product updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error updating product: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Method to delete a product from Firestore
    public void deleteProduct(String productId) {
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(context, "Product ID is required for deletion", Toast.LENGTH_SHORT).show();
            return;
        }

        productsRef.document(productId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Product deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error deleting product: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Method to search for products by name and return results via callback
    public void searchProduct(String productName, OnProductsRetrievedListener listener) {
        if (productName == null || productName.isEmpty()) {
            Toast.makeText(context, "Product name is required for search", Toast.LENGTH_SHORT).show();
            return;
        }

        productsRef.whereEqualTo("name", productName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        List<Product> products = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            products.add(product);
                        }
                        listener.onProductsRetrieved(products); // Return the result via callback
                    } else {
                        Toast.makeText(context, "No product found with the name: " + productName, Toast.LENGTH_SHORT).show();
                        listener.onProductsRetrieved(new ArrayList<>()); // Return empty list if no product found
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error searching for product: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Method to fetch products from API and handle the response
    public void fetchProducts(OnProductsRetrievedListener listener) {
        Call<List<Product>> call = apiService.getProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onProductsRetrieved(response.body()); // Pass the products to the listener
                } else {
                    Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
