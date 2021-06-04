package com.signmaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {

    private ArrayList<ExampleItem> mExampleList;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button buttonAdd;
    private EditText editTextSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        createExampleList();
        buildRecyclerView();

        buttonAdd = findViewById(R.id.btn_addFav);
        editTextSearch = findViewById(R.id.fav_search);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String place = editTextSearch.getText().toString();
                insertItem(place);
            }
        });

    }

    public void insertItem(String place){
        mExampleList.add(0,new ExampleItem(R.drawable.ic_favorite, place));
        mAdapter.notifyItemInserted(0);
    }

    public void removeItem(){
        mExampleList.remove(0);
        mAdapter.notifyItemInserted(0);
    }

    public void createExampleList(){
        mExampleList = new ArrayList<>();
    }

    public void buildRecyclerView(){
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ExampleAdapter(mExampleList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }
}