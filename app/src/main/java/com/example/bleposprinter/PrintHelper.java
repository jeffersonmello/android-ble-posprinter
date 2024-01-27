package com.example.bleposprinter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;

public class PrintHelper {
    private Activity activity;

    public PrintHelper(Activity activity) {
        this.activity = activity;
    }

    public Bitmap printReciboTest() {
        View report = this.activity.getLayoutInflater().inflate(R.layout.recibo_teste, null);

        PrintViewHelper printViewHelper = new PrintViewHelper();
        Bitmap viewImage = printViewHelper.generateBitmapFromView(report);

        return viewImage;
    }
}
