package com.kingsbyte.subrefill;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ContactUsDialog extends Dialog {
    String facebookUrl = "subrefill";
    String facebookId = "107289690752119";
    String emailAddress = "hello@subrefill.com";
    String phoneNumber = "08127586926";
    public ContactUsDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_contact_us);
        RelativeLayout closeBtn = findViewById(R.id.close_container);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

            }
        });

        TextView facebookBtn = findViewById(R.id.facebook_link);
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

        TextView emailBtn = findViewById(R.id.email_link);
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

        TextView callBtn = findViewById(R.id.phone_link);
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+phoneNumber));
                getContext().startActivity(intent);
            }
        });


    }
}
