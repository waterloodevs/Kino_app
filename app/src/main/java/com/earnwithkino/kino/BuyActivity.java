package com.earnwithkino.kino;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class BuyActivity extends Fragment {

    private ImageView giftcardImg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.activity_buy, container, false);
        giftcardImg = fragmentView.findViewById(R.id.imageView5);
        return fragmentView;
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_buy);
//
//        giftcardImg = findViewById(R.id.imageView5);
//
//        int id = getIntent().getIntExtra("id", 0);
//        Drawable drawable = getResources().getDrawable(id);
//        giftcardImg.setImageDrawable(drawable);
//    }
//
//    public void goBack(View v) {
//        startActivity(new Intent(this, StoreActivity.class));
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//        finish();
//    }
}
