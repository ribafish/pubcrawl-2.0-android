package com.ws1617.iosl.pubcrawl20.NewEvent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ws1617.iosl.pubcrawl20.R;

import static android.R.attr.width;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static android.support.v7.appcompat.R.attr.height;
import static java.awt.font.TextAttribute.WIDTH;

/**
 * Created by Haneen on 12/7/2016.
 */

public class ShareEventDialog extends DialogFragment {


    //Views
    ImageView mQrCodeImg;
    View rootView;
    String mBarcodeData = "test";

    public ShareEventDialog() {
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.view_invite_dialog
                , null);
        alertDialog.setView(rootView);
        initDialogView(rootView);
        return alertDialog.create();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       // initDialogView(rootView);
    }

    void initDialogView(View rootView) {
        mQrCodeImg = (ImageView) rootView.findViewById(R.id.invite_barcode);
        Bitmap bmp = null;
        try {

            bmp = encodeAsBitmap(mBarcodeData);
            mQrCodeImg.setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 1800, 900, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}

