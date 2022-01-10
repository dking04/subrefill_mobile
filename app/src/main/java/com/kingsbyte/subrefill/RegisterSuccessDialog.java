package com.kingsbyte.subrefill;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class RegisterSuccessDialog extends Dialog {
    Activity context;
    String message;

    public RegisterSuccessDialog(@NonNull Activity context,String message) {
        super(context);
        this.context = context;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_success_dialog);
        Button okBtn = findViewById(R.id.ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                context.finish();
            }
        });

        TextView messageTxt = findViewById(R.id.message);
        messageTxt.setText(message);
    }
}
