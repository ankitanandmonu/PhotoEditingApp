package org.drupalchamp.photoeditingapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView viewImage;
    private Button original, bw, sepia, crop, gallery, camera, save;

    private static final int PICK_FROM_CAMERA = 1;

    private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";

    private Uri fileUri; // file url to store image/video
    public static final int MEDIA_TYPE_IMAGE = 1;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    BitmapDrawable drawable;
    Bitmap bitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        original = (Button) findViewById(R.id.original);
        bw = (Button) findViewById(R.id.bw);
        sepia = (Button) findViewById(R.id.sepia);
        viewImage = (ImageView) findViewById(R.id.image);
        crop = (Button) findViewById(R.id.crop);
        gallery = (Button) findViewById(R.id.gallery);
        camera = (Button) findViewById(R.id.camera);
        save = (Button) findViewById(R.id.save);

        gallery.setOnClickListener(this);
        camera.setOnClickListener(this);
        crop.setOnClickListener(this);
        original.setOnClickListener(this);
        bw.setOnClickListener(this);
        sepia.setOnClickListener(this);
        save.setOnClickListener(this);

        drawable = (BitmapDrawable) viewImage.getDrawable();
        bitmap = drawable.getBitmap();

        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            finish();
        }


    }

    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        final Drawable drawable = viewImage.getDrawable();

        switch (id){
            case R.id.original:
                setNoColorFilter(drawable);
                break;
            case R.id.sepia:
                setSepiaColorFilter(drawable);
                break;
            case R.id.bw:
                setBlackAndWhiteColorFilter(drawable);
                break;
            case R.id.gallery:
                gallerypic();
                break;
            case R.id.camera:
                camerapic();
                break;
            case R.id.crop:
                //startActivity(new Intent(getApplicationContext(),SecondActivity.class));
                viewImage.setRotation(90);

                break;
            case R.id.save:
                saveImage();
            default:
                break;
        }

    }

    private void saveImage() {
        File filename;
        try {
            String path = Environment.getExternalStorageDirectory().toString();

            new File(path + "/folder/subfolder").mkdirs();
            filename = new File(path + "/folder/subfolder/image.jpg");

            FileOutputStream out = new FileOutputStream(filename);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            MediaStore.Images.Media.insertImage(getContentResolver(), filename.getAbsolutePath(), filename.getName(), filename.getName());

            Toast.makeText(getApplicationContext(), "File is Saved in  " + filename, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gallerypic() {
        viewImage.setImageDrawable(null);
        Crop.pickImage(this);
    }

    private void camerapic() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }


    private void setBlackAndWhiteColorFilter(Drawable drawable) {
        if (drawable == null)
            return;

        final ColorMatrix matrixA = new ColorMatrix();
        matrixA.setSaturation(0);

        final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrixA);
        drawable.setColorFilter(filter);

    }

    private void setSepiaColorFilter(Drawable drawable) {
        if (drawable == null)
            return;

        final ColorMatrix matrixA = new ColorMatrix();
        matrixA.setSaturation(0);

        final ColorMatrix matrixB = new ColorMatrix();
        matrixB.setScale(1f, .95f, .82f, 1.0f);
        matrixA.setConcat(matrixB, matrixA);

        final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrixA);
        drawable.setColorFilter(filter);

    }

    private void setNoColorFilter(Drawable drawable) {
        if (drawable == null)
            return;
        drawable.setColorFilter(null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void previewCapturedImage() {
        try {
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            viewImage.setImageBitmap(bitmap);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    /**
     * ------------ Helper Methods ----------------------
     * */

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}
