package com.ws1617.iosl.pubcrawl20.Search;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.DataModels.Person;
import com.ws1617.iosl.pubcrawl20.Database.PersonDbHelper;
import com.ws1617.iosl.pubcrawl20.Details.PersonDetailsActivity;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Haneen on 09/02/2017.
 */

public class PersonListAdapter extends RecyclerView.Adapter<PersonListAdapter.PersonItem> {

    static final String TAG = PersonListAdapter.class.getName();

    List<PersonMini> personsList = new ArrayList<>();
    List<PersonMini> personsListCopy;

    Context context;

    public PersonListAdapter(Context context) {
        getAllPersons();
        this.context = context;
        personsListCopy = personsList;
    }

    private void getAllPersons() {
        PersonDbHelper personDbHelper = new PersonDbHelper();
        List<Person> el = personDbHelper.getAllPersons();
        for (Person e : el) {
            PersonMini personMini = new PersonMini(e);
            personsList.add(personMini);
        }
        notifyDataSetChanged();
    }

    @Override
    public PersonItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_search_list_row, parent, false);
        PersonItem personView = new PersonItem(view);
        return personView;
    }

    @Override
    public void onBindViewHolder(PersonItem holder, int position) {
        final PersonMini personMini = personsList.get(position);
        holder.name.setText(personMini.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,PersonDetailsActivity.class);
                intent.putExtra("id",personMini.getId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return personsList.size();
    }

    public void filter(String text, SearchActivity.SearchResultView personSearchListener) {

        personsList = new ArrayList<>();
        if (text.isEmpty()) {
            personsList.addAll(personsListCopy);
        } else {
            text = text.toLowerCase();
            for (PersonMini item : personsListCopy) {
                if (item.getName().toLowerCase().contains(text)) {
                    personsList.add(item);
                }
            }
        }
        personSearchListener.setSearchResultSize(personsList.size());
        notifyDataSetChanged();
    }

    public class PersonItem extends RecyclerView.ViewHolder  {

        TextView name;

        public PersonItem(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.person_name);

        }

    }
}
