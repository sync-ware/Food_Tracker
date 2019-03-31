package com.csed.foodtracker;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadRecieptActivity extends AppCompatActivity {

    String currentPhotoPath;
    File photoFile;
    private int GALLERY = 1, CAMERA = 2;
    Uri photoURI;
    ImageView view;
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
            view.setImageBitmap(bitmap);
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
        System.out.println(imageText);
        return imageText.toString();
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
//                    String path = saveImage(bitmap);
                    Toast.makeText(UploadRecieptActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
//                    view.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(UploadRecieptActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
//            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
//            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                startOcr(bitmap);
            } catch (IOException e) {
                Toast.makeText(UploadRecieptActivity.this, "Error!", Toast.LENGTH_SHORT).show();
            }
//            view.setImageBitmap(thumbnail);
//            saveImage(thumbnail);
            Toast.makeText(UploadRecieptActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_reciept);
        tv = findViewById(R.id.ocr_text);

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
                Toast.makeText(UploadRecieptActivity.this, "Upload photo!", Toast.LENGTH_SHORT).show();
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(galleryIntent, GALLERY);
//                showPictureDialog();
            }
        });

        Button readTextButton = (Button) findViewById(R.id.read_text_button);
        readTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(UploadRecieptActivity.this, "Read Text!", Toast.LENGTH_SHORT).show();
                startOcr(null);
            }
        });

        view = (ImageView) findViewById(R.id.imageView);


    }

}
