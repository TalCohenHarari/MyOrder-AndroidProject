package com.example.myorder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.myorder.adapters.NotesAdapter;
import com.example.myorder.database.NotesDatabase;
import com.example.myorder.entities.Note;
import com.example.myorder.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

//https://youtu.be/hlkekoPqsis
public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;

    ImageView imageAddNoteMain;
    EditText inputSearch;
    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private int noteClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize
        imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        inputSearch = findViewById(R.id.inputSearch);

        //RecyclerView
        notesRecyclerView = findViewById(R.id.notesRycyclerview);
        notesRecyclerView.setHasFixedSize(true);
        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList,this);
        notesRecyclerView.setAdapter(notesAdapter);

        //init
        getNotes(REQUEST_CODE_SHOW_NOTES,false);

        //Listeners
        imageAddNoteMain.setOnClickListener(v->startActivityForResult(new Intent(getApplicationContext(),CreateNoteActivity.class),REQUEST_CODE_ADD_NOTE));
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(noteList.size()!=0){
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition=position;
        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note",note);
        startActivityForResult(intent,REQUEST_CODE_UPDATE_NOTE);
    }

    private void getNotes(final int requestCode,final boolean isNoteDeleted){

        class getNotesTask extends AsyncTask<Void, Void, List<Note>>{

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if(requestCode==REQUEST_CODE_SHOW_NOTES)
                {
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }
                else if(requestCode==REQUEST_CODE_ADD_NOTE)
                {
                    noteList.add(0,notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                }
                else if(requestCode==REQUEST_CODE_UPDATE_NOTE)
                {
                    noteList.remove(noteClickedPosition);

                    if(isNoteDeleted){
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    }
                    else{
                        noteList.add(noteClickedPosition,notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }
            }
        }

        new getNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_ADD_NOTE && resultCode==RESULT_OK)
        {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        }
        else if(requestCode==REQUEST_CODE_UPDATE_NOTE && resultCode==RESULT_OK)
        {
             if(data!=null)
             {
                 getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted",false));
             }
        }
    }
}