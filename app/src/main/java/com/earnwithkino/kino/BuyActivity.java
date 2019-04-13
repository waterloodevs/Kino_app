package com.earnwithkino.kino;

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

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.constraint.Constraints.TAG;

public class BuyActivity extends Fragment implements View.OnClickListener {

    private OkHttpClient okHttpClient;
    private ImageView backPressed;
    private EditText emailView;
    private RadioGroup radioGroup;
    private TextView quantityView;
    private ImageView plus;
    private ImageView minus;
    private Button confirm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.activity_buy, container, false);
        ((MainActivity) getActivity()).nav.setVisibility(View.GONE);
        backPressed = fragmentView.findViewById(R.id.back);
        emailView = fragmentView.findViewById(R.id.emailView);
        radioGroup = fragmentView.findViewById(R.id.radioGroup);
        confirm = fragmentView.findViewById(R.id.confirm);
        plus = fragmentView.findViewById(R.id.plus);
        minus = fragmentView.findViewById(R.id.minus);
        quantityView = fragmentView.findViewById(R.id.quantity);
        backPressed.setOnClickListener(this);
        confirm.setOnClickListener(this);
        plus.setOnClickListener(this);
        minus.setOnClickListener(this);
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
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(getActivity(), "Amount is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check the user has sufficient balance


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

        // Get order information
        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = getView().findViewById(selectedId);
        String amount = radioButton.getText().toString();
        String email = emailView.getText().toString();
        String quantity = quantityView.getText().toString();

        // Send request to backend, returns whitelisted transaction
        RequestBody data = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("amount", amount)
                .addFormDataPart("email", email)
                .addFormDataPart("quantity", quantity)
                .build();

        Request request = new Request.Builder()
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
        } else if (view == minus) {
            subtractFromQuantity();
        }
    }
}
