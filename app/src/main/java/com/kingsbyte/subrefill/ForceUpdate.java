package com.kingsbyte.subrefill;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;

public class ForceUpdate extends AsyncTask<String,String, JSONObject> {
    private String latestVersion;
    private String currentVersion;
    private Context context;

    public ForceUpdate(String currentVersion, Context context) {
        this.currentVersion = currentVersion;
        this.context = context;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {

        try {
            latestVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + context.getPackageName()+ "&hl=en")
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select("div.hAyfc:nth-child(4) > span:nth-child(2) > div:nth-child(1) > span:nth-child(1)")
                    .first()
                    .ownText();
        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new JSONObject();
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        try {
            double cv = Double.parseDouble(currentVersion);
            double lv = Double.parseDouble(latestVersion);
            if (latestVersion != null) {
                if (lv > cv) {
                    if (!((Activity) context).isFinishing()) {
                        showForceUpdateDialog();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onPostExecute(jsonObject);
    }


    private void showForceUpdateDialog() {
        Activity activity = (Activity) context;
        CustomUpdateDialog cud = new CustomUpdateDialog(activity);
        cud.setCancelable(false);
        cud.setCanceledOnTouchOutside(false);
        cud.show();
    }
}
