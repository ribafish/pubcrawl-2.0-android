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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ws1617.iosl.pubcrawl20.R;

/**
 * Created by Haneen on 12/7/2016.
 */

public class ShareEventDialog extends DialogFragment {


    //Views
    ImageView mQrCodeImg;
    View rootView;

    public ShareEventDialog() {
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {€    void initDialogView(View rootView) {

        mQrCodeImg = (ImageView) rootView.findViewById(R.id.invite_barcode);
        QRCodeWriter writer = new QRCodeWriter();
        String content = "test";
        // this is a small sample use of the QRCodeEncoder class from zxing
        try {
            // generate a 150x150 QR code== €Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {<*
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Colo≉
                mQrCodeImg.setImageBitmap(bmp);
            }
        } catch (WriterException e) { //eek }
        }
    }ckedListener);


        mPubsListView = (Spinner) view.findViewById(R.id.pub_dialog_pubs_list);
        mPubsListView.setOnItemSelectedListener(pubListOnItemSelectedListener);
        //TODO should be fetched from the DB
}