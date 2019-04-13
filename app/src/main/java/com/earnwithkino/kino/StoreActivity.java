package com.earnwithkino.kino;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;

public class StoreActivity extends Fragment implements View.OnClickListener {

    private ImageView amazonGiftCard;
    private ImageView info;
    private PopupWindow popupWindow;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.activity_store, container, false);
        amazonGiftCard = fragmentView.findViewById(R.id.imageView3);
        info = fragmentView.findViewById(R.id.info);
        info.setOnClickListener(this);
//        ImageView appleGiftCard = fragmentView.findViewById(R.id.imageButton4);
//        ImageView walmartGiftCard = fragmentView.findViewById(R.id.imageButton5);
        amazonGiftCard.setOnClickListener(this);
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
        if (view == info) {
            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
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
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        } else if (view == amazonGiftCard) {
            //        String imageName = view.getTag().toString();
//        int id = getResources().getIdentifier(imageName,"drawable", getActivity().getPackageName());
//        Bundle args = new Bundle();
//        args.putInt("id", id);
            Fragment fragment = new BuyActivity();
//        fragment.setArguments(args);
            View container = getActivity().findViewById(R.id.fragment_container);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(container.getId(), fragment, "findThisFragment")
                    .addToBackStack(null)
                    .commit();
        }
    }

}
