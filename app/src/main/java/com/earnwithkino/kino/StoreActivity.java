package com.earnwithkino.kino;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

public class StoreActivity extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.activity_store, container, false);
//        ImageView amazonGiftCard = fragmentView.findViewById(R.id.imageView2);
//        ImageView appleGiftCard = fragmentView.findViewById(R.id.imageView3);
//        ImageView walmartGiftCard = fragmentView.findViewById(R.id.imageView4);
//        amazonGiftCard.setOnClickListener(this);
//        appleGiftCard.setOnClickListener(this);
//        walmartGiftCard.setOnClickListener(this);
        return fragmentView;
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_store);
//    }

//    public void giftcard (View v) {
//        String imageName = v.getTag().toString();
//        int id = getResources().getIdentifier(imageName,"drawable", getActivity().getPackageName());
//        Intent i = new Intent(getActivity(), BuyActivity.class);
//        i.putExtra("drawableId", id);
//        startActivity(i);
//        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//        getActivity().finish();
//    }

    @Override
    public void onClick(View view) {
        Fragment fragment = new BuyActivity();
        View container = getActivity().findViewById(R.id.fragment_container);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(container.getId(), fragment, "findThisFragment")
                .addToBackStack(null)
                .commit();
    }
}
