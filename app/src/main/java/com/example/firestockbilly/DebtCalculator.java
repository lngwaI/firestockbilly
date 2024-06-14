package com.example.firestockbilly;

import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;

public class DebtCalculator {

    private static final String TAG = "DebtCalculator";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface DebtCalculationListener {
        void onDebtsCalculated(Map<String, Double> debts);
        void onError(String errorMessage);
    }

    public void calculateDebts(String accountId, DebtCalculationListener listener) {
        db.collection("accounts").document(accountId).collection("transactions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Double> debts = new HashMap<>();

                            for (DocumentSnapshot document : task.getResult()) {
                                String payer = document.getString("payer");
                                double amount = document.getDouble("amount");
                                Map<String, Double> recipients = (Map<String, Double>) document.get("recipients");

                                // Add amount spent by payer
                                if (!debts.containsKey(payer)) {
                                    debts.put(payer, 0.0);
                                }
                                debts.put(payer, debts.get(payer) - amount); // Negative amount spent by payer

                                // Add amounts received by recipients
                                for (Map.Entry<String, Double> entry : recipients.entrySet()) {
                                    String recipient = entry.getKey();
                                    double receivedAmount = entry.getValue();

                                    if (!debts.containsKey(recipient)) {
                                        debts.put(recipient, 0.0);
                                    }
                                    debts.put(recipient, debts.get(recipient) + receivedAmount); // Positive amount received by recipient
                                }

                                // Log transaction details (optional, for debugging)
                                Log.d(TAG, "Transaktion: " + document.getId());
                                Log.d(TAG, "Betrag: " + amount);
                                Log.d(TAG, "Payer: " + payer);
                                Log.d(TAG, "Recipients: " + recipients);
                                Log.d(TAG, "-----------------------");
                            }

                            // Callback to handle debts Map
                            listener.onDebtsCalculated(debts);

                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                            listener.onError("Error getting documents: " + task.getException().getMessage());
                        }
                    }
                });
    }
}
