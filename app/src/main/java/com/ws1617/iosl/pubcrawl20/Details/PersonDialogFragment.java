package com.ws1617.iosl.pubcrawl20.Details;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;


/**
 * Created by Gasper Kojek on 12. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class PersonDialogFragment extends DialogFragment {
    private static final String TAG = "PersonDialogFragment";

    private PersonAdapter personAdapter;
    private ArrayList<PersonMini> participants = new ArrayList<>();
    private ArrayList<PersonMini> participantsDiplayed = new ArrayList<>();
    private Context context;
    private View view;

    public PersonDialogFragment() {
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        view = inflater.inflate(R.layout.popup_persons, container, false);

        Bundle args = getArguments();
        String title = "";
        try {
            title = args.getString("title");
        }catch (Exception e) {
            Log.e(TAG, "title argument empty");
        }


        ListView participantsListView = (ListView) view.findViewById(R.id.popup_person_listview);

        this.context = getContext();

        this.personAdapter = new PersonAdapter(context, this.participantsDiplayed);
        participantsListView.setAdapter(this.personAdapter);
        participantsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, PersonDetailsActivity.class);
                intent.putExtra("name", participantsDiplayed.get(i).getName());
                intent.putExtra("id", participantsDiplayed.get(i).getId());
                startActivity(intent);
            }
        });

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.popup_person_toolbar);
        this.setHasOptionsMenu(true);
        toolbar.inflateMenu(R.menu.popup_listview_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.popup_listview_menu_close:
                        dismiss();
                        break;
                    case R.id.popup_listview_menu_search:
                        SearchView searchView = (SearchView) item.getActionView();
                        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                Log.d(TAG, "search onQueryTextSubmit");
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String newText) {
                                Log.d(TAG, "search onQueryTextChange");

                                participantsDiplayed.clear();
                                for (PersonMini p : participants) {
                                    if (p.getName().toLowerCase().contains(newText.toLowerCase()) || String.valueOf(p.getId()).contains(newText)) {
                                        participantsDiplayed.add(p);
                                    }
                                }

                                personAdapter.notifyDataSetChanged();

                                return false;
                            }
                        });
                }

                return true;
            }
        });
        toolbar.setTitle(title);

        return view;
    }

    public PersonDialogFragment setParticipants(ArrayList<PersonMini> participants) {
        Log.d(TAG, "setParticipants");
        this.participants.clear();
        this.participants.addAll(participants);
        this.participantsDiplayed.clear();
        this.participantsDiplayed.addAll(participants);
        return this;
    }

    public PersonDialogFragment notifyDataSetChanged() {
        Log.d(TAG, "notifyDataSetChanged");

        this.participantsDiplayed.clear();
        this.participantsDiplayed.addAll(participants);
        this.personAdapter.notifyDataSetChanged();
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(DialogFragment.STYLE_NO_FRAME, getTheme());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog");

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int padding = (int) (2 * 26 * getResources().getDisplayMetrics().density + 0.5f);

        Window window = getDialog().getWindow();
        window.setLayout(displayMetrics.widthPixels - padding/2, displayMetrics.heightPixels - padding);

        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        window.setGravity(Gravity.CENTER);
        if(Build.VERSION.SDK_INT >= 22){
            window.setElevation(10.0f);
        }
    }
}
