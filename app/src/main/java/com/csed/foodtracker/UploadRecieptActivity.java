package com.csed.foodtracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
    private ListView listView;
    private AddIngredientsAdapter ingredientsAdapter;
//    ImageView view;
    private TextView tv;

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

//            startOcr(photoURI);
        }
    }

    @SuppressLint("ShowToast")
    private void startOcr(Bitmap bitmap) {
        try{
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            }
//            view.setImageBitmap(bitmap);
            String result =  extractText(bitmap);
            tv.setText(result);
//            Toast.makeText(UploadRecieptActivity.this, "Finished Loading", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this,"error startOCR",Toast.LENGTH_SHORT);
        }
    }

    private String extractText(Bitmap bitmap){
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
        String string = imageText.toString();
        String words[] = {"chicken","sausages","carrots","milk","celery","mince","ham"};
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
        return imageText.toString(); // TODO: Need to parse the data to get useful info from it. Presumably with a button after or something
    }


    private void addItem(String name) {
        Ingredient i = new Ingredient();
        i.setName(name);
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
                    startOcr(bitmap);
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
                startOcr(bitmap);
            } catch (IOException e) {
                Toast.makeText(UploadRecieptActivity.this, "Error!", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(UploadRecieptActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*        String themeVal;
        SharedPreferences themePrefs;
        themePrefs = getSharedPreferences("com.csed.foodtracker.theme", 0);
        themeVal = themePrefs.getString("theme", "1");
        if (themeVal == "1") {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeDark);
        }*/
        setContentView(R.layout.activity_upload_reciept);

        tv = findViewById(R.id.ocr_text);
        listView = (ListView) findViewById(R.id.ingredient_list);
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
            }
        });

        Button addPhotoButton = (Button) findViewById(R.id.add_photo_button);
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY);
            }
        });
        final Context context = this;
        Button confirmChangesButton = (Button) findViewById(R.id.confirm_changes_button);
        confirmChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ingredientsAdapter.getView(1,this, );
                ingredientsAdapter.storeData(context);
//                runMethod();

            }
        });
    }

    private void runMethod() {
        Toast.makeText(UploadRecieptActivity.this, ingredientList.get(0).getName(), Toast.LENGTH_SHORT).show();
    }
}
