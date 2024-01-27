package com.example.bleposprinter;

import android.graphics.Bitmap;

public class BitmapUtils {
    public static Bitmap[] splitBitmap(Bitmap originalBitmap) {
        int maxHeight = 400;
        int originalHeight = originalBitmap.getHeight();
        int numberOfSubBitmaps = (int) Math.ceil((float) originalHeight / maxHeight);

        Bitmap[] subBitmaps = new Bitmap[numberOfSubBitmaps];

        int yPosition = 0;
        for (int i = 0; i < numberOfSubBitmaps; i++) {
            int remainingHeight = originalHeight - yPosition;
            int subBitmapHeight = Math.min(remainingHeight, maxHeight);

            subBitmaps[i] = Bitmap.createBitmap(originalBitmap, 0, yPosition, originalBitmap.getWidth(), subBitmapHeight);
            yPosition += subBitmapHeight;
        }

        return subBitmaps;
    }
}
