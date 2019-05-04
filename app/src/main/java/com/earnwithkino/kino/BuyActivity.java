package com.earnwithkino.kino;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static android.support.constraint.Constraints.TAG;

public class BuyActivity extends Fragment implements View.OnClickListener {

    private ImageView backPressed;
    private EditText emailView;
    private RadioGroup radioGroup;
    private RadioButton option1;
    private RadioButton option2;
    private RadioButton option3;
    private RadioButton selected;
    private TextView quantityView;
    private ImageView plus;
    private ImageView minus;
    private TextView totalView;
    private Button confirm;
    private int price;
    private OkHttpClient okHttpClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.activity_buy, container, false);
        ((MainActivity) getActivity()).nav.setVisibility(View.GONE);
        backPressed = fragmentView.findViewById(R.id.back);
        emailView = fragmentView.findViewById(R.id.emailView);
        radioGroup = fragmentView.findViewById(R.id.radioGroup);
        option1 = fragmentView.findViewById(R.id.radioButton1);
        option2 = fragmentView.findViewById(R.id.radioButton2);
        option3 = fragmentView.findViewById(R.id.radioButton3);
        selected = null;
        price = getContext().getSharedPreferences("_", MODE_PRIVATE).getInt("price", 0);
        //TODO: Ensure Kin price is not zero, if it is, something went wrong when trying to fetch and store it. Do it again.
        Toast.makeText(getActivity(), "Price is " + price, Toast.LENGTH_SHORT).show();
        confirm = fragmentView.findViewById(R.id.confirm);
        plus = fragmentView.findViewById(R.id.plus);
        minus = fragmentView.findViewById(R.id.minus);
        quantityView = fragmentView.findViewById(R.id.quantity);
        totalView = fragmentView.findViewById(R.id.total);
        backPressed.setOnClickListener(this);
        confirm.setOnClickListener(this);
        plus.setOnClickListener(this);
        minus.setOnClickListener(this);
        option1.setOnClickListener(this);
        option2.setOnClickListener(this);
        option3.setOnClickListener(this);

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        return fragmentView;
    }

    private boolean validateForm() {

        // check Email
        String email = emailView.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "Email is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        // check amount
        if (selected == null) {
            Toast.makeText(getActivity(), "Amount is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        // check quantity
        String quantity = quantityView.getText().toString();
        if (TextUtils.isEmpty(quantity)) {
            Toast.makeText(getActivity(), "Quantity is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check the user has sufficient balance
        int balance = Integer.parseInt(((MainActivity) getActivity()).kinBalance);
        int total = Integer.parseInt(totalView.getText().toString());
        if (total > balance){
            Toast.makeText(getActivity(), "You do not have enough Kin :(", Toast.LENGTH_SHORT).show();
            return false;
        } else if (total < 0) {
            Toast.makeText(getActivity(), "Total cannot be zero or less. Something went wrong.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;


    }

    private void addToQuantity() {
        int i = Integer.parseInt(quantityView.getText().toString());
        if (i < 10) {
            i = i + 1;
            quantityView.setText(String.valueOf(i));
        }
    }

    private void subtractFromQuantity() {
        int i = Integer.parseInt(quantityView.getText().toString());
        if (i > 1) {
            i = i - 1;
            quantityView.setText(String.valueOf(i));
        }
    }

    private void buyGiftCard(){

        String BASE_URL = MainActivity.BASE_URL;
        String route = "/buy_giftcard";
        String token = getContext().getSharedPreferences("_", MODE_PRIVATE).getString("token", null);

        // Get order information
        String amount = selected.getText().toString().replace("$", "");
        String email = emailView.getText().toString();
        String quantity = quantityView.getText().toString();
        String type = "Amazon";
        String total = totalView.getText().toString();

        // Send request to backend, returns whitelisted transaction
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("amount", amount);
            json.put("email", email);
            json.put("quantity", quantity);
            json.put("total", total);
        } catch (JSONException e){
            e.printStackTrace();
        }

        RequestBody data = RequestBody.create(JSON, json.toString());

        Request request = new Request.Builder()
                .addHeader("Authorization", "Token " + token)
                .url(BASE_URL + route)
                .post(data)
                .build();

        okHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "Buy call failed");
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        final int code = response.code();
                        response.close();
                        if (code != 200) {
                            Log.d(TAG, "Buy call failed with response code: " + code);
                        } else {
                            Log.d(TAG, "Gift card bought successfully");
                        }
                    }
                });


    }

    private void calcTotal(){
        int amount = 0;
        if (selected != null) {
            amount = Integer.parseInt(selected.getText().toString().replace("$", ""));
        }
        int quantity = Integer.parseInt(quantityView.getText().toString());
        int total = amount * quantity * price;
        totalView.setText(String.valueOf(total));
    }


    @Override
    public void onClick(View view) {
        if (view == backPressed) {
            ((MainActivity) getActivity()).nav.setVisibility(View.VISIBLE);
            getFragmentManager().popBackStackImmediate();
        } else if (view == confirm) {
            if (validateForm()) {
                Toast.makeText(getActivity(), "Valid Purchase", Toast.LENGTH_SHORT).show();
                buyGiftCard();
            }
        } else if (view == plus) {
            addToQuantity();
            calcTotal();
        } else if (view == minus) {
            subtractFromQuantity();
            calcTotal();
        } else if ((view == option1) || (view == option2) || (view == option3)){
            int selectedId = radioGroup.getCheckedRadioButtonId();
            selected = getView().findViewById(selectedId);
            calcTotal();
        }
    }
}
