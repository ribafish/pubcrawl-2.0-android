package com.ws1617.iosl.pubcrawl20.NewEvent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.ws1617.iosl.pubcrawl20.MainActivity;
import com.ws1617.iosl.pubcrawl20.R;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * Created by Haneen on 12/7/2016.
 */

public class ShareEventDialog extends DialogFragment {


    //Views
    ImageView mQrCodeImg;
    View rootView;
    Button closeBtn;
    String mBarcodeData = "test";

    LinearLayout inProcessView;
    LinearLayout qrCodeView;

    public ShareEventDialog() {
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.view_invite_dialog
                , null);
        closeBtn = (Button) rootView.findViewById(R.id.invite_dialog_pub_done);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                getActivity().startActivity(intent);
            }
        });
        alertDialog.setView(rootView);

        inProcessView = (LinearLayout) rootView.findViewById(R.id.spinner_placeholder);
        qrCodeView = (LinearLayout) rootView.findViewById(R.id.qrcode_placeholder);

        initInProcessView();
        return alertDialog.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // end current activity and switch to MainActivity
        Intent intent = new Intent(getActivity(), MainActivity.class);
        getActivity().startActivity(intent);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void initQRCodeView() {

        inProcessView.setVisibility(View.GONE);
        qrCodeView.setVisibility(View.VISIBLE);

        mQrCodeImg = (ImageView) rootView.findViewById(R.id.invite_barcode);
        Bitmap bmp = null;
        try {

            bmp = encodeAsBitmap(mBarcodeData);
            mQrCodeImg.setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public void initInProcessView() {
        inProcessView.setVisibility(View.VISIBLE);
        qrCodeView.setVisibility(View.GONE);
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

