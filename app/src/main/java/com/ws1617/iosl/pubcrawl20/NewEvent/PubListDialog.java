package com.ws1617.iosl.pubcrawl20.NewEvent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;
import com.ws1617.iosl.pubcrawl20.Models.Pub;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.List;

/**
 * Created by Haneen on 11/29/2016.
 */

public class PubListDialog extends DialogFragment {

    static final String TAG = "PubListDialog";

    OnSelectPubDialogDismissed onSelectPubDialogDismissed;

    Button mDoneBtn;


    public PubListDialog() {

    }

    public void setPubListListener(OnSelectPubDialogDismissed onSelectPubDialogDismissed) {
        this.onSelectPubDialogDismissed = onSelectPubDialogDismissed;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.view_pup_list,null);
        alertBuilder.setView(view);
        initView(view);
        return alertBuilder.create();
    }


    private  void initView(View view){


        mDoneBtn = (Button) view.findViewById(R.id.pub_done);
        mDoneBtn.setOnClickListener(mDoneBtnClickedListener);


    }

    View.OnClickListener mDoneBtnClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Pub pub = new Pub("no",new LatLng(1,1),1);
            onSelectPubDialogDismissed.addPubToList(pub);
            dismiss();
        }
    };

    interface OnSelectPubDialogDismissed {
        void addPubToList(Pub newPub);
    }
}
