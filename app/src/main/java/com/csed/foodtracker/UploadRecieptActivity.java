package com.csed.foodtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UploadRecieptActivity extends AppCompatActivity {

    ArrayList<Ingredient> ingredientList;
    String currentPhotoPath;
    File photoFile;
    private int GALLERY = 1, CAMERA = 2;
    Uri photoURI;
    private AddIngredientsAdapter ingredientsAdapter;
//    ImageView view;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(UploadRecieptActivity.this, "Sorry, there has been an error.", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.csed.foodtracker",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA);
            }
        }
    }

    private void extractText(Bitmap bitmap){
        DatabaseHelper mDBHelper = new DatabaseHelper(this);
        SQLiteDatabase mDb;
        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            mDb = mDBHelper.getWritableDatabase();

        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        Frame imageFrame = new Frame.Builder()
                .setBitmap(bitmap)                 // your image bitmap
                .build();

        StringBuilder imageText = new StringBuilder();
        Toast.makeText(UploadRecieptActivity.this, "AAA!", Toast.LENGTH_SHORT).show();

        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
            imageText.append(textBlock.getValue());                   // return string
        }
        TextView tv = (TextView) findViewById(R.id.textView);
        String string = imageText.toString();
        tv.setText(string);
        tv.setVisibility(View.GONE);
        Cursor cursor = mDb.rawQuery("SELECT name FROM Ingredients",null);
        List<String> words = new ArrayList<>();
        cursor.moveToPosition(0);
        //Keep looping until you reach the last row
        while (cursor.getPosition() < cursor.getCount()){
            String name = cursor.getString(cursor.getColumnIndex("name"));
            words.add(name.toLowerCase());
            cursor.moveToNext();
        }
        cursor.close();
        List<String> takenIngs = new ArrayList<>();
        for (String word: string.split(" ")) {
            for (String validWord: words) {
                if (word.toLowerCase().equals(validWord) && takenIngs.indexOf(word.toLowerCase()) == -1) {
                    takenIngs.add(word.toLowerCase());
                    addItem(word);
                    break;
                }
            }
        }
//                tv.setText("");
    }

    private void addItem(String name) {
        String newName = name.substring(0, 1).toUpperCase() + name.substring(1); // Should capitalise the first letter
        Ingredient i = new Ingredient();
        i.setName(newName);
        ingredientList.add(i);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    /**
     * This is an override of onActivityResult, which performs different actions depending on if
     * The camera or the gallery is used.
     * @param requestCode The code..
     * @param resultCode The code used to determine what to do next
     * @param data The intent data returned from the initial call
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    extractText(bitmap);
                    Toast.makeText(UploadRecieptActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(UploadRecieptActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == CAMERA) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                // Attempts to retrieve the photo from the previously determined URI
                extractText(bitmap);
            } catch (IOException e) {
                Toast.makeText(UploadRecieptActivity.this, "Error!", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(UploadRecieptActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String themeVal;
        SharedPreferences themePrefs;
        themePrefs = getSharedPreferences("com.csed.foodtracker.theme", 0);
        themeVal = themePrefs.getString("theme", "1");
        if (themeVal.equals("1")) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeDark);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload_reciept);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ListView listView = findViewById(R.id.ingredient_list);
        ingredientList = new ArrayList<>();
/*        Ingredient i = new Ingredient();
        i.setName("Test1");
        ingredientList.add(i);*/

        ingredientsAdapter = new AddIngredientsAdapter(this,ingredientList);
        listView.setAdapter(ingredientsAdapter);
//        listView.
/*        Ingredient ingg = new Ingredient();
        ingg.setName("Penguin Yay 1");
        ingredientList.add(ingg);*/
//        view = findViewById(R.id.imageView);


        Button useCameraButton = (Button) findViewById(R.id.use_camera_button);
        useCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
                Toast.makeText(UploadRecieptActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();
            }
        });

        Button addPhotoButton = (Button) findViewById(R.id.add_photo_button);
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY);
                Toast.makeText(UploadRecieptActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();
            }
        });
        final Context context = this;
        Button confirmChangesButton = (Button) findViewById(R.id.confirm_changes_button);
        confirmChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ingredientsAdapter.getView(1,this, );
                boolean completed = ingredientsAdapter.storeData(context);
                if (completed) {
                    Toast.makeText(UploadRecieptActivity.this, "Saved Changes", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UploadRecieptActivity.this,"Add ingredients first!",Toast.LENGTH_SHORT).show();
                }
//                runMethod();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}

