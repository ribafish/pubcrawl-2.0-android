package com.ws1617.iosl.pubcrawl20.NewEvent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.ws1617.iosl.pubcrawl20.Database.resetDbTask;
import com.ws1617.iosl.pubcrawl20.MainActivity;
import com.ws1617.iosl.pubcrawl20.R;

import java.lang.reflect.Array;

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
    public static String mBarcodeData = "/event/";

    LinearLayout inProcessView;
    LinearLayout qrCodeView;

    boolean isDialog = false;

    public static ShareEventDialog newInstance() {
        Bundle args = new Bundle();
        ShareEventDialog fragment = new ShareEventDialog();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        isDialog = true;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.view_invite_dialog
                , null);
        closeBtn = (Button) rootView.findViewById(R.id.invite_dialog_pub_done);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // new resetDbTask(getContext(), resetDbTask.EVENTS_DB).execute();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });
        alertDialog.setView(rootView);

        inProcessView = (LinearLayout) rootView.findViewById(R.id.spinner_placeholder);
        qrCodeView = (LinearLayout) rootView.findViewById(R.id.qrcode_placeholder);

        initInProcessView();
        return alertDialog.create();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (isDialog) {
            isDialog = false;
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.view_invite_dialog
                , null);
        closeBtn = (Button) rootView.findViewById(R.id.invite_dialog_pub_done);
       closeBtn.setVisibility(View.GONE);

        inProcessView = (LinearLayout) rootView.findViewById(R.id.spinner_placeholder);
        qrCodeView = (LinearLayout) rootView.findViewById(R.id.qrcode_placeholder);
        return rootView;

    }

    boolean showCodeWhenReady = false;
    String eventName;

    public void showCodeWhenReady(String eventName) {
        showCodeWhenReady = true;
        this.eventName = eventName;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (showCodeWhenReady) {
            initQRCodeView(eventName);
            showCodeWhenReady = false;
        }

    }

    public void initQRCodeView(String event_name) {

        inProcessView.setVisibility(View.GONE);
        qrCodeView.setVisibility(View.VISIBLE);

        mQrCodeImg = (ImageView) rootView.findViewById(R.id.invite_barcode);
        Bitmap bmp = null;
        try {

            bmp = encodeAsBitmap(mBarcodeData + event_name);
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

