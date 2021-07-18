package com.example.myorder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PathEffect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myorder.database.NotesDatabase;
import com.example.myorder.entities.Note;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.jar.Pack200;

public class CreateNoteActivity extends AppCompatActivity {

    ImageView imageBack, imageSave, imageNote;
    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
    private TextView textDateTime, textWebUrl;
    private LinearLayout layoutWebURL;
    private AlertDialog dialogAddURL;
    private String selectedNoteColor;
    private View viewSubtitleIndicator;
    private final static int REQUEST_CODE_STORAGE_PERMISSION=1;
    private final static int REQUEST_IMAGE_CAPTURE = 3;
    private final static int REQUEST_CODE_SELECTED_IMAGE=2;
    private String selectedImagePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        //Initialize
        imageBack = findViewById(R.id.imageBack);
        imageSave = findViewById(R.id.imageSave);
        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitleTitle);
        inputNoteText = findViewById(R.id.inputNote);
        textDateTime = findViewById(R.id.textDateTime);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        imageNote = findViewById(R.id.imageNote);
        textWebUrl = findViewById(R.id.textWebUrl);
        layoutWebURL = findViewById(R.id.layoutWebUrl);

        //Listeners
        imageBack.setOnClickListener(v->onBackPressed());
        imageSave.setOnClickListener(v->saveNote());

        //Init
        textDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));
        selectedNoteColor="#333333"; //Default note color
        selectedImagePath="";
        initMiscellaneous();
        setSubtitleIndicatorColor();
    }

    //------------------------------------------Save note-------------------------------------------------
    private void saveNote(){
        if(inputNoteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Note title can't be empty!",Toast.LENGTH_SHORT).show();
            return;
        }
        else if(inputNoteSubtitle.getText().toString().trim().isEmpty() && inputNoteText.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Note can't be empty!",Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);

        if(layoutWebURL.getVisibility() == View.VISIBLE){
            note.setWebLink(textWebUrl.getText().toString());
        }
        //Room doesn't allow database operation on the main thread. That's why we are using async task to save note.
        class SaveNoteTask extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }

        new SaveNoteTask().execute();
    }

    //------------------------------------------------------------------------------------------------------------------------------
    private void initMiscellaneous(){
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(v->{
            if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            else
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        final ImageView imageColor1= layoutMiscellaneous.findViewById(R.id.imageColor1);
        final ImageView imageColor2= layoutMiscellaneous.findViewById(R.id.imageColor2);
        final ImageView imageColor3= layoutMiscellaneous.findViewById(R.id.imageColor3);
        final ImageView imageColor4= layoutMiscellaneous.findViewById(R.id.imageColor4);
        final ImageView imageColor5= layoutMiscellaneous.findViewById(R.id.imageColor5);

        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(v-> {
            selectedNoteColor = "#333333";
            imageColor1.setImageResource(R.drawable.ic_done);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(v-> {
            selectedNoteColor = "#FDBE3B";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(R.drawable.ic_done);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(v-> {
            selectedNoteColor = "#FF4842";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(R.drawable.ic_done);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(v-> {
            selectedNoteColor = "#3A52FC";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(R.drawable.ic_done);
            imageColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(v-> {
            selectedNoteColor = "#000000";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(R.drawable.ic_done);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(v->{
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        CreateNoteActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
            }
            else
                selectImage();

        });

        layoutMiscellaneous.findViewById(R.id.layoutTakeImage).setOnClickListener(v->{
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        CreateNoteActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_IMAGE_CAPTURE);
            }
            else
                takePicture();

        });

        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(v->{
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddURLDialog();
        });
    }

    //------------------------------------------Set side line color near the subtitle-------------------------------------------------
    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable)viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    //------------------------------------------Select an Image From Gallery/Camera---------------------------------------------------------
    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_CODE_SELECTED_IMAGE);
        }
    }

    private void takePicture(){

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //------------------------------------------On Request Permissions Result---------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE_STORAGE_PERMISSION && grantResults.length>0)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                selectImage();
            else
                Toast.makeText(this,"Permission Denied!",Toast.LENGTH_SHORT).show();
        }
        else if(requestCode==REQUEST_IMAGE_CAPTURE && grantResults.length>0)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                takePicture();
            else
                Toast.makeText(this,"Permission Denied!",Toast.LENGTH_SHORT).show();
        }
    }

    //------------------------------------------On Activity Result---------------------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_SELECTED_IMAGE && resultCode==RESULT_OK){
            if(data!=null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri!=null){
                  try {
                      InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                      Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                      imageNote.setImageBitmap(bitmap);
                      imageNote.setVisibility(View.VISIBLE);
                      selectedImagePath=getPathFromUri(selectedImageUri);
                  }
                  catch (Exception exception){
                    Toast.makeText(this,exception.getMessage(),Toast.LENGTH_SHORT).show();
                  }
                }
            }
        }
        else if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageNote.setImageBitmap(bitmap);
            imageNote.setVisibility(Button.VISIBLE);
            Bitmap OutImage = Bitmap.createScaledBitmap(bitmap, 1000, 1000,true);
            String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), OutImage, "Title", null);
            Uri tempUri = Uri.parse(path);
            selectedImagePath=getPathFromUri(tempUri);
        }
    }

    //------------------------------------------Get Image Path---------------------------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor=getContentResolver().query(contentUri,null,null,null);
        if(cursor == null){
            filePath = contentUri.getPath();
        }
        else {
            cursor.moveToFirst();
            int index =cursor.getColumnIndex("_data");
            filePath=cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    //------------------------------------------URL Dialog---------------------------------------------------------
    private void showAddURLDialog(){
        if(dialogAddURL==null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup)findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);
            dialogAddURL = builder.create();
            if(dialogAddURL.getWindow()!=null){
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL= view.findViewById(R.id.inputUrl);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(v->{
                if(inputURL.getText().toString().trim().isEmpty()){
                    Toast.makeText(CreateNoteActivity.this,"Enter URL",Toast.LENGTH_SHORT).show();
                }
                else if(!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()){
                    Toast.makeText(CreateNoteActivity.this,"Ener valid URL",Toast.LENGTH_SHORT).show();
                }else{
                    textWebUrl.setText(inputURL.getText().toString());
                    layoutWebURL.setVisibility(View.VISIBLE);
                    dialogAddURL.dismiss();
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(v->dialogAddURL.dismiss());
        }
        dialogAddURL.show();
    }
}