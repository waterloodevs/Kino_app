package com.earnwithkino.kino;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class StoreActivity extends Fragment implements View.OnClickListener {

    private MainActivity parentActivity;
    private ImageView info;
    private PopupWindow popupWindow;
    private ImageView amazonGiftCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.activity_store, container, false);
        parentActivity = (MainActivity) getActivity();
        info = fragmentView.findViewById(R.id.info);
        info.setOnClickListener(this);
        amazonGiftCard = fragmentView.findViewById(R.id.imageView3);
        amazonGiftCard.setOnClickListener(this);
        return fragmentView;
    }

    private void showPopup() {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) parentActivity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.spend_popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup to dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        // Get a reference for the custom view close button
        ImageButton closeButton = popupView.findViewById(R.id.ib_close);

        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                popupWindow.dismiss();
            }
        });

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(info, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onClick(View view) {
        if (view == info) {
            showPopup();
        } else if (view == amazonGiftCard) {
            Fragment fragment = new BuyActivity();
            View container = parentActivity.findViewById(R.id.fragment_container);
            parentActivity.getSupportFragmentManager().beginTransaction()
                    .replace(container.getId(), fragment, "findThisFragment")
                    .commit();
        }
    }

}
