package com.kingsbyte.subrefill;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.net.URLEncoder;


public class Support extends Fragment {
    String facebookUrl = "subrefill";
    String facebookId = "107289690752119";
    String emailAddress = "hello@subrefill.com";
    String phoneNumber = "08127586926";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_support, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Contact us");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        CardView callBtn = view.findViewById(R.id.call);
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+phoneNumber));
                getContext().startActivity(intent);
            }
        });

        CardView whatSappBtn = view.findViewById(R.id.whatsapp);
        whatSappBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessageToWhatsAppContact("+2348127586926",getActivity());
            }
        });

        CardView emailBtn = view.findViewById(R.id.email);
        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto",emailAddress, null));
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                intent.putExtra(Intent.EXTRA_TEXT, "");
                getContext().startActivity(Intent.createChooser(intent, "Send Us a mail"));
            }
        });

        CardView facebookBtn = view.findViewById(R.id.facebook);
        facebookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    getContext().getPackageManager().getPackageInfo("com.facebook.katana", 0);

                    String facebookScheme = "fb://page/" + facebookId;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookScheme));
                    getContext().startActivity(intent);

                }
                catch(Exception e) {
                    e.printStackTrace();
                    // Cache and Open a url in browser
                    String facebookProfileUri = "https://www.facebook.com/" + facebookUrl;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookProfileUri));
                    getContext().startActivity(intent);

                }

            }
        });
        return view;
    }


    private void sendMessageToWhatsAppContact(String number, Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent i = new Intent(Intent.ACTION_VIEW);
        try {
            String url = "https://api.whatsapp.com/send?phone=" + number + "&text=" + URLEncoder.encode("Hello", "UTF-8");
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(packageManager) != null) {
                context.startActivity(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}