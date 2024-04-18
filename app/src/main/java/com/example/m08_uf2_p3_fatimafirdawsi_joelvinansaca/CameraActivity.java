package com.example.m08_uf2_p3_fatimafirdawsi_joelvinansaca;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
public class CameraActivity extends AppCompatActivity {
    ImageButton capture, toggleFlash, flipCamera;
    ImageView thumbnailView;
    private PreviewView previewView;
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private String fileName = "";
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseFirestore mDataBase = FirebaseFirestore.getInstance();
    StorageReference storageRef;
    private Location location;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                startCamera(cameraFacing);
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        location = intent.getParcelableExtra("location");
        previewView = findViewById(R.id.cameraPreview);
        capture = findViewById(R.id.capture);
        toggleFlash = findViewById(R.id.toggleFlash);
        flipCamera = findViewById(R.id.flipCamera);
        //navigateToVideo = findViewById(R.id.navigateToVideo);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera(cameraFacing);
        }

        /*navigateToVideo.setOnClickListener((view -> {
            startActivity(new Intent(MainActivity.this, VideoActivity.class));
        }));

         */

        flipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
                    cameraFacing = CameraSelector.LENS_FACING_FRONT;
                } else {
                    cameraFacing = CameraSelector.LENS_FACING_BACK;
                }
                startCamera(cameraFacing);
            }
        });

        thumbnailView = findViewById(R.id.thumbnail);
        loadThumbnail();
    }

    public void startCamera(int cameraFacing) {
        int aspectRatio = aspectRatio(previewView.getWidth(), previewView.getHeight());
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);

        listenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) listenableFuture.get();

                Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

                ImageCapture imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                capture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }
                        takePicture(imageCapture);
                    }
                });

                toggleFlash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setFlashIcon(camera);
                    }
                });

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    public void takePicture(ImageCapture imageCapture) {

        if (location != null) {
            DecimalFormat df = new DecimalFormat("#.#####");
            String lat = df.format(location.getLatitude());
            String lon = df.format(location.getLongitude());

            storage.getReference();

            fileName = lat + "_" + lon + ".jpg";

        } else {
            Log.d("TAGGG", "fileName: " + fileName);
        }

        File file = new File(getExternalFilesDir(null), fileName);

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(file).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference imageRef = storageRef.child("images/" + fileName);

                        try {
                            InputStream stream = new FileInputStream(file);

                            UploadTask uploadTask = imageRef.putStream(stream);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(CameraActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                                    Log.e("Firebase", "Upload failed", exception);
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    CameraActivity.this.capture.setActivated(false);
                                    // PUEDE TARDAR UN POCO EN SALIR EL TOAST PERO SI SALE.
                                    Toast.makeText(CameraActivity.this, "Uploaded succeeded", Toast.LENGTH_SHORT).show();
                                    Log.i("Firebase", "Upload succeeded");

                                    double lat =  location.getLatitude();
                                    double lon = location.getLongitude();

                                    // Crea un nuevo objeto con los datos de la foto
                                    Map<String, Object> photo = new HashMap<>();

                                    photo.put("lat", lat);
                                    photo.put("lon", lon);
                                    photo.put("fileName", fileName);

                                    // Añade el nuevo objeto a la colección de fotos en Firestore
                                    mDataBase.collection("photos").add(photo)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error adding document", e);
                                                }
                                            });

                                    startActivity(new Intent(CameraActivity.this, MapsActivity.class));
                                }
                            });
                        } catch (Exception e) {
                            Toast.makeText(CameraActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                            Log.e("CameraX", "Error uploading image", e);
                        }



                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Image capture failed", exception);
                    }
                });
    }


    private Bitmap createThumbnail(File imgFile, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

        int scaleFactor = Math.min(options.outWidth / width, options.outHeight / height);

        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPurgeable = true;

        return BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
    }

    private void setFlashIcon(Camera camera) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            if (camera.getCameraInfo().getTorchState().getValue() == 0) {
                camera.getCameraControl().enableTorch(true);
                toggleFlash.setImageResource(R.drawable.flash_off_icon);
            } else {
                camera.getCameraControl().enableTorch(false);
                toggleFlash.setImageResource(R.drawable.flash_on_icon);
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CameraActivity.this, "Flash is not available currently", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private int aspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    private void loadThumbnail(){
        SharedPreferences sharedPreferences = getSharedPreferences("App", MODE_PRIVATE);
        String lastImagePath = sharedPreferences.getString("lastImagePath", null);

        if (lastImagePath != null) {
            File imgFile = new File(lastImagePath);
            if(imgFile.exists()){
                // Crear una miniatura
                Bitmap thumbnail = createThumbnail(imgFile, 100, 100);

                // Establecer la miniatura en el ImageView
                ImageView thumbnailView = findViewById(R.id.thumbnail);
                thumbnailView.setImageBitmap(thumbnail);
            }
        }

        ImageView thumbnailView = findViewById(R.id.thumbnail);

        thumbnailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraActivity.this, FullImageActivity.class);
                intent.putExtra("imagePath", lastImagePath);
                startActivity(intent);
            }
        });
    }
}