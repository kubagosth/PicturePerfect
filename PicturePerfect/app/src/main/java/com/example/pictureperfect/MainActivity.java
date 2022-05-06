package com.example.pictureperfect;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements MainActivityPresenter.View,View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentPhotoPath;
    private Bitmap bitmap;
    private MainActivityPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.ButtonPhoto);
        button.setOnClickListener(v -> dispatchTakePictureIntent());

        Button buttonTest = findViewById(R.id.buttonTESTTOP);
        buttonTest.setOnClickListener(view -> presenter.set());
    }

    /**
     *  Take Picture Intent
     *  When result code is ok then can set picture and create new presenter
     *  Used part of the code from Selfie app project
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic();
            presenter = new MainActivityPresenter(MainActivity.this,findViewById(R.id.imageView));
        }
    }

    /**
     * Method called when Take a picture button is pressed
     * Used code from Selfie app project
     */
    private void dispatchTakePictureIntent()
    {
        Intent takePicturesIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Check if camera handler is active
        if (takePicturesIntent.resolveActivity(getPackageManager()) != null)
        {
            //Create Image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch (IOException ex)
            {
                Log.d("ErrorLog",ex.toString());
            }
            //
            if (photoFile != null)
            {
                Uri photoUri = FileProvider.getUriForFile(this,"com.example.pictureperfect.fileprovider",photoFile);
                takePicturesIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                //Take Picture Intent
                startActivityForResult(takePicturesIntent,REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    /**
     * Set Picture to the Image view in main activity
     * Used part of the code from Selfie app project
     * Instead of full image size scale to 1280 x 720
     * */
    private void setPic() {
        // Get the imageView
        ImageView imageView = findViewById(R.id.imageView);

        // Get the dimensions of the View
        //int targetW = imageView.getWidth();
        // int targetH = imageView.getHeight();
        int targetW = 1280;
        int targetH = 720;
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * Save the picture under application picture folder temporary
     * Used code from Selfie app project
     * */
    private File createImageFile() throws IOException
    {
        // Create new timeStamp with current date
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName,".jpg",storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * On application close or change of activity
     * Used code from Selfie app project
     * */
    @Override
    protected void onDestroy()
    {
        deleteRecursive(getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        super.onDestroy();
    }
    /**
     * Only Delete Photo Temp files from application picture folder
     * Used code from Selfie app project
     * */
    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    /**
     * Assign images to top 5 colors
     * @param rgbArrayList - All grouped colors sorted
     */
    @Override
    public void topRgb(ArrayList<RGB> rgbArrayList)
    {
        try {
            TextView textView = findViewById(R.id.textViewTOP5);
            String text = "";

            for (int i = 0; i < 5; i++) {

                text += "(" + rgbArrayList.get(i).getRed() + "," + rgbArrayList.get(i).getGreen() + "," + rgbArrayList.get(i).getBlue() + ") Pixel Count : " + rgbArrayList.get(i).getCount() + "\n";
            }
            ImageView image = findViewById(R.id.top1);
            image.setBackgroundColor(Color.rgb(rgbArrayList.get(0).getRed(),rgbArrayList.get(0).getGreen(),rgbArrayList.get(0).getBlue()));
            image = findViewById(R.id.top2);
            image.setBackgroundColor(Color.rgb(rgbArrayList.get(1).getRed(),rgbArrayList.get(1).getGreen(),rgbArrayList.get(1).getBlue()));
            image = findViewById(R.id.top3);
            image.setBackgroundColor(Color.rgb(rgbArrayList.get(2).getRed(),rgbArrayList.get(2).getGreen(),rgbArrayList.get(2).getBlue()));
            image = findViewById(R.id.top4);
            image.setBackgroundColor(Color.rgb(rgbArrayList.get(3).getRed(),rgbArrayList.get(3).getGreen(),rgbArrayList.get(3).getBlue()));
            image = findViewById(R.id.top5);
            image.setBackgroundColor(Color.rgb(rgbArrayList.get(4).getRed(),rgbArrayList.get(4).getGreen(),rgbArrayList.get(4).getBlue()));

            textView.setText(text);
        }
        catch (IndexOutOfBoundsException e)
        {
            Log.d("Log-E","rgbArrayList is empty");
            Toast.makeText(this, "NOT READY YET", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onClick(View v) {

    }
}