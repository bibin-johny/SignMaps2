/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.signmaps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.MenuItem;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.media.Image.Plane;
import com.google.android.material.navigation.NavigationView;
import java.io.IOException;
import java.util.List;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.util.Size;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import com.google.android.material.navigation.NavigationView;
import com.signmaps.env.ImageUtils;
import com.signmaps.env.Logger;

public abstract class CameraActivity extends AppCompatActivity
    implements OnImageAvailableListener,PopUp.Listener, NavigationView.OnNavigationItemSelectedListener,
        Camera.PreviewCallback,
        CompoundButton.OnCheckedChangeListener{
  private static final Logger LOGGER = new Logger();

  private static final int PERMISSIONS_REQUEST = 1;

  private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
  protected int previewWidth = 0;
  protected int previewHeight = 0;
  private boolean debug = false;
  private Handler handler;
  private HandlerThread handlerThread;
  private boolean useCamera2API;
  private boolean isProcessingFrame = false;
  private byte[][] yuvBytes = new byte[3][];
  private int[] rgbBytes = null;
  private int yRowStride;
  private Runnable postInferenceCallback;
  private Runnable imageConverter;

  protected TextView inferenceTimeTextView;
  private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
  private static final String[] RUNTIME_PERMISSIONS = {
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.INTERNET,
          Manifest.permission.ACCESS_WIFI_STATE,
          Manifest.permission.ACCESS_NETWORK_STATE
  };

  private MapFragmentView m_mapFragmentView;
  private DrawerLayout drawer;
  private Button search;
  private String loc1;
  private String loc2;
  public static double lat;
  public static double longi;
  private FusedLocationProviderClient fusedLocationProviderClient;
  private Geocoder geocoder;
  public static double lat1;
  public static double lon1;
  public static double lat2;
  public static double lon2;

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    LOGGER.d("onCreate " + this);
    super.onCreate(null);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    setContentView(R.layout.activity_camera);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    if (hasPermission()) {
      setFragment();
    } else {
      requestPermission();
    }
    drawer = findViewById(R.id.drawer_layout);

    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();
    search = (Button) findViewById(R.id.btn);
    search.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ShowPopup();
      }
    });
    geocoder = new Geocoder(this);
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    if (hasPermissions(this, RUNTIME_PERMISSIONS)) {
      getCurrentLocation();
      setupMapFragmentView();
    } else {
      ActivityCompat
              .requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE_ASK_PERMISSIONS);
    }


    inferenceTimeTextView = findViewById(R.id.inference_info);
  }
  public void Camerapop(String title){
    final AlertDialog.Builder dialog = new AlertDialog.Builder(this,R.style.MyDialogTheme).setTitle("           Detected Traffic symbol").setMessage(title);
    final AlertDialog alert = dialog.create();

    ImageView imageView = new ImageView(this);

    switch (title) {
      case "STOP":
        imageView.setImageResource(R.drawable.stop);
        break;
      case "KEEPRIGHT":
        imageView.setImageResource(R.drawable.keepright);
        break;
      case "LIMIT50":
        imageView.setImageResource(R.drawable.limit50);
        break;
      case "LIMIT80":
        imageView.setImageResource(R.drawable.limit80);
        break;
      case "GIVEWAY":
        imageView.setImageResource(R.drawable.giveway);
        break;
      case "NOENTRY":
        imageView.setImageResource(R.drawable.noentry);
        break;
      case "NOOVERTAKING":
        imageView.setImageResource(R.drawable.noovertaking);
        break;
      case "PRIORITYROAD":
        imageView.setImageResource(R.drawable.priorityroad);
        break;
      case "UNKNOWN":
        imageView.setImageResource(R.drawable.unknown);
        break;
      case "SLIPPERYROAD":
        imageView.setImageResource(R.drawable.slip);
        break;
      case "CAUTIONARYSIGN":
        imageView.setImageResource(R.drawable.caution);
        break;
    }

    alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
    WindowManager.LayoutParams wmlp = alert.getWindow().getAttributes();

    wmlp.gravity = Gravity.TOP | Gravity.CENTER;

    alert.setView(imageView);
    alert.show();

// Hide after some seconds
    final Handler handler  = new Handler();
    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        if (alert.isShowing()) {
          alert.dismiss();
        }
      }
    };

    alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialog) {
        handler.removeCallbacks(runnable);
      }
    });

    handler.postDelayed(runnable, 2000);
  }

  public void ShowPopup() {
    PopUp popupdialog = new PopUp();
    popupdialog.show(getSupportFragmentManager(), "popup");
  }
  public void getCurrentLocation(){
    fusedLocationProviderClient.getLastLocation()
            .addOnSuccessListener(new OnSuccessListener<Location>() {
              @Override
              public void onSuccess(Location location) {
                if( location != null){
                  lat= location.getLatitude();
                  longi = location.getLongitude();
                }

              }
            });
  }
  /**
   * Only when the app's target SDK is 23 or higher, it requests each dangerous permissions it
   * needs when the app is running.
   */
  private static boolean hasPermissions(Context context, String... permissions) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
      for (String permission : permissions) {
        if (ActivityCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public void onBackPressed() {
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }


  private void setupMapFragmentView() {
    // All permission requests are being handled. Create map fragment view. Please note
    // the HERE Mobile SDK requires all permissions defined above to operate properly.
    m_mapFragmentView = new MapFragmentView(this);
  }

  @Override
  public void onDestroy() {
    m_mapFragmentView.onDestroy();
    super.onDestroy();
  }

  @Override
  public void applyTexts(String starting, String ending) throws IOException {
    loc1 = starting;
    loc2 = ending;
    List<Address> addresses = geocoder.getFromLocationName(loc1, 1);
    Address address1 = addresses.get(0);
    List<Address> addresses2 = geocoder.getFromLocationName(loc2, 1);
    Address address2 = addresses2.get(0);
    lat1 = address1.getLatitude();
    lon1 = address1.getLongitude();
    lat2 = address2.getLatitude();
    lon2 = address2.getLongitude();

  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
    switch (menuItem.getItemId()) {
      case R.id.nav_favorites:
        Toast.makeText(this, "clicked Favorites", Toast.LENGTH_SHORT).show();
        Intent favIntent = new Intent(this, FavoriteActivity.class);
        startActivity(favIntent);
        break;
      case R.id.voiceCtrlButton:
        Toast.makeText(this, "clicked Voice Settings", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CameraActivity.this, VoiceSkinsActivity.class);
        startActivity(intent);
        break;
      case R.id.nav_settings:
        Toast.makeText(this, "clicked Settings", Toast.LENGTH_SHORT).show();
        break;
      case R.id.nav_about:
        Toast.makeText(this, "clicked About Us", Toast.LENGTH_SHORT).show();
        break;
    }
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }
  protected int[] getRgbBytes() {
    imageConverter.run();
    return rgbBytes;
  }

  protected int getLuminanceStride() {
    return yRowStride;
  }

  protected byte[] getLuminance() {
    return yuvBytes[0];
  }

  /** Callback for android.hardware.Camera API */
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void onPreviewFrame(final byte[] bytes, final Camera camera) {
    if (isProcessingFrame) {
      LOGGER.w("Dropping frame!");
      return;
    }

    try {
      // Initialize the storage bitmaps once when the resolution is known.
      if (rgbBytes == null) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        previewHeight = previewSize.height;
        previewWidth = previewSize.width;
        rgbBytes = new int[previewWidth * previewHeight];
        onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
      }
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      return;
    }

    isProcessingFrame = true;
    yuvBytes[0] = bytes;
    yRowStride = previewWidth;

    imageConverter =
        new Runnable() {
          @Override
          public void run() {
            ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
          }
        };

    postInferenceCallback =
        new Runnable() {
          @Override
          public void run() {
            camera.addCallbackBuffer(bytes);
            isProcessingFrame = false;
          }
        };
    processImage();
  }

  /** Callback for Camera2 API */
  @Override
  public void onImageAvailable(final ImageReader reader) {
    // We need wait until we have some size from onPreviewSizeChosen
    if (previewWidth == 0 || previewHeight == 0) {
      return;
    }
    if (rgbBytes == null) {
      rgbBytes = new int[previewWidth * previewHeight];
    }
    try {
      final Image image = reader.acquireLatestImage();

      if (image == null) {
        return;
      }

      if (isProcessingFrame) {
        image.close();
        return;
      }
      isProcessingFrame = true;
      Trace.beginSection("imageAvailable");
      final Plane[] planes = image.getPlanes();
      fillBytes(planes, yuvBytes);
      yRowStride = planes[0].getRowStride();
      final int uvRowStride = planes[1].getRowStride();
      final int uvPixelStride = planes[1].getPixelStride();

      imageConverter =
          new Runnable() {
            @Override
            public void run() {
              ImageUtils.convertYUV420ToARGB8888(
                  yuvBytes[0],
                  yuvBytes[1],
                  yuvBytes[2],
                  previewWidth,
                  previewHeight,
                  yRowStride,
                  uvRowStride,
                  uvPixelStride,
                  rgbBytes);
            }
          };

      postInferenceCallback =
          new Runnable() {
            @Override
            public void run() {
              image.close();
              isProcessingFrame = false;
            }
          };

      processImage();
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      Trace.endSection();
      return;
    }
    Trace.endSection();
  }

  @Override
  public synchronized void onStart() {
    LOGGER.d("onStart " + this);
    super.onStart();
  }

  @Override
  public synchronized void onResume() {
    LOGGER.d("onResume " + this);
    super.onResume();

    handlerThread = new HandlerThread("inference");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
  }

  @Override
  public synchronized void onPause() {
    LOGGER.d("onPause " + this);

    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
    } catch (final InterruptedException e) {
      LOGGER.e(e, "Exception!");
    }

    super.onPause();
  }

  @Override
  public synchronized void onStop() {
    LOGGER.d("onStop " + this);
    super.onStop();
  }



  protected synchronized void runInBackground(final Runnable r) {
    if (handler != null) {
      handler.post(r);
    }
  }


  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    if (requestCode == PERMISSIONS_REQUEST) {
      if (allPermissionsGranted(grantResults)) {
        setFragment();
      } else {
        requestPermission();
      }
    }
    switch (requestCode) {
      case REQUEST_CODE_ASK_PERMISSIONS: {
        for (int index = 0; index < permissions.length; index++) {
          if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {

            /*
             * If the user turned down the permission request in the past and chose the
             * Don't ask again option in the permission request system dialog.
             */
            if (!ActivityCompat
                    .shouldShowRequestPermissionRationale(this, permissions[index])) {
              Toast.makeText(this, "Required permission " + permissions[index]
                              + " not granted. "
                              + "Please go to settings and turn on for sample app",
                      Toast.LENGTH_LONG).show();
            } else {
              Toast.makeText(this, "Required permission " + permissions[index]
                      + " not granted", Toast.LENGTH_LONG).show();
            }
          }
        }

        setupMapFragmentView();
        break;
      }
      default:
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }


  private static boolean allPermissionsGranted(final int[] grantResults) {
    for (int result : grantResults) {
      if (result != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  private boolean hasPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
    } else {
      return true;
    }
  }

  private void requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
        Toast.makeText(
                CameraActivity.this,
                "Camera permission is required for this demo",
                Toast.LENGTH_LONG)
            .show();
      }
      requestPermissions(new String[] {PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
    }
  }

  // Returns true if the device supports the required hardware level, or better.
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private boolean isHardwareLevelSupported(
      CameraCharacteristics characteristics, int requiredLevel) {
    int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
      return requiredLevel == deviceLevel;
    }
    // deviceLevel is not LEGACY, can use numerical sort
    return requiredLevel <= deviceLevel;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private String chooseCamera() {
    final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    try {
      for (final String cameraId : manager.getCameraIdList()) {
        final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        // We don't use a front facing camera in this sample.
        final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }

        final StreamConfigurationMap map =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        if (map == null) {
          continue;
        }

        // Fallback to camera1 API for internal cameras that don't have full support.
        // This should help with legacy situations where using the camera2 API causes
        // distorted or otherwise broken previews.
        useCamera2API =
            (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                || isHardwareLevelSupported(
                    characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
        LOGGER.i("Camera API lv2?: %s", useCamera2API);
        return cameraId;
      }
    } catch (CameraAccessException e) {
      LOGGER.e(e, "Not allowed to access camera");
    }

    return null;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  protected void setFragment() {
    String cameraId = chooseCamera();

    Fragment fragment;
    if (useCamera2API) {
      CameraConnectionFragment camera2Fragment =
          CameraConnectionFragment.newInstance(
              new CameraConnectionFragment.ConnectionCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onPreviewSizeChosen(final Size size, final int rotation) {
                  previewHeight = size.getHeight();
                  previewWidth = size.getWidth();
                  CameraActivity.this.onPreviewSizeChosen(size, rotation);
                }
              },
              this,
              getLayoutId(),
              getDesiredPreviewFrameSize());

      camera2Fragment.setCamera(cameraId);
      fragment = camera2Fragment;
    } else {
      fragment =
          new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
    }

    getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
  }

  protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (int i = 0; i < planes.length; ++i) {
      final ByteBuffer buffer = planes[i].getBuffer();
      if (yuvBytes[i] == null) {
        LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
        yuvBytes[i] = new byte[buffer.capacity()];
      }
      buffer.get(yuvBytes[i]);
    }
  }

  public boolean isDebug() {
    return debug;
  }

  protected void readyForNextImage() {
    if (postInferenceCallback != null) {
      postInferenceCallback.run();
    }
  }

  protected int getScreenOrientation() {
    switch (getWindowManager().getDefaultDisplay().getRotation()) {
      case Surface.ROTATION_270:
        return 270;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_90:
        return 90;
      default:
        return 0;
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

  }

  protected void showInference(String inferenceTime) {
    inferenceTimeTextView.setText(inferenceTime);
  }

  protected abstract void processImage();

  protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

  protected abstract int getLayoutId();

  protected abstract Size getDesiredPreviewFrameSize();

}
