/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package or.kr.ajouhosp.stroke.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;

import or.kr.ajouhosp.stroke.CameraSource;
import or.kr.ajouhosp.stroke.CameraSourcePreview;
import or.kr.ajouhosp.stroke.GraphicOverlay;
import or.kr.ajouhosp.stroke.R;
import or.kr.ajouhosp.stroke.posedetector.PoseDetectorProcessor;
import or.kr.ajouhosp.stroke.posedetector.PoseGraphic;
import or.kr.ajouhosp.stroke.preference.PreferenceUtils;
import or.kr.ajouhosp.stroke.preference.SettingsActivity;
import or.kr.ajouhosp.stroke.preference.SettingsActivity.LaunchSource;
import or.kr.ajouhosp.stroke.ChartActivity;

import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC;

/** Live preview demo for ML Kit APIs. */
/** Live preview demo for ML Kit APIs. */
@KeepName
public final class LivePreviewActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback, SurfaceHolder.Callback, CompoundButton.OnCheckedChangeListener{

  private static final String TAG = "LivePreviewActivity";
  private CameraSource cameraSource = null;

  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay;

  private GraphicOverlay video_graphicOverlay;
  public static TextView countTxt;
  private VideoView screenVideoView;
  private View 	decorView;
  private int	uiOption;
  public InputImage image;
  public Uri uri;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //Log.d("lifecycddddddddle : ", "check");
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.activity_vision_live_preview);
    decorView = getWindow().getDecorView();
    uiOption = getWindow().getDecorView().getSystemUiVisibility();
    if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH )
      uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
      uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
    if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )
      uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    countTxt = findViewById(R.id.countTxt); //갯수 표시
    decorView.setSystemUiVisibility( uiOption );

    //프리뷰 설정
    preview = findViewById(R.id.preview_view);
    if (preview == null) {
      Log.d(TAG, "Preview is null");
    }
    graphicOverlay = findViewById(R.id.graphic_overlay);
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }

    video_graphicOverlay = findViewById(R.id.video_graphic_overlay);

    //비디오 재생
    screenVideoView = findViewById(R.id.screenVideoView);
    uri = Uri.parse("android.resource://"+getPackageName()+"/raw/sample");
    screenVideoView.setVideoURI(uri);
    screenVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        // 준비 완료되면 비디오 재생
        mp.start();
        Log.v("VideoPoseDetection",":"+ mp.getVideoWidth());
      }
    });

    //카메라 방향 전환
    ToggleButton facingSwitch = findViewById(R.id.facing_switch);
    facingSwitch.setOnCheckedChangeListener(this);

    //갯수 초기화 버튼
    ToggleButton setInit = findViewById(R.id.setInit);
    setInit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        initCount();
      }
    });

    //그래프 표시
    ToggleButton open_graph = findViewById(R.id.open_graph);
    open_graph.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(),ChartActivity.class);
        startActivity(intent);
      }
    });
    
    //갯수 저장
    Button saveCnt = findViewById(R.id.saveCnt);
    saveCnt.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(LivePreviewActivity.this);
        dlg.setMessage("저장하시겠습니까?"); // 메시지
        dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
          public void onClick(DialogInterface dialog, int which) {
            sendInfo();
          }
        });
        dlg.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) { }
        });
        dlg.show();
      }
    });
  }

  public void sendInfo(){
    //현재 날짜 가져오기
    Date mDate = new Date(System.currentTimeMillis());
    int mHour = mDate.getHours();
    int mMinute = mDate.getMinutes();
    int mMonth = mDate.getMonth();
    int mDay = mDate.getDate();
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MONTH,mMonth);
    Toast.makeText(LivePreviewActivity.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();

    writeFile((mMonth+1)+"/"+mDay+"("+mHour+":"+mMinute+")", String.valueOf(countTxt.getText()));
  }

  public void writeFile(String date, String cnt) {
    try{
      BufferedWriter bwd = new BufferedWriter(new FileWriter(getFilesDir() + "SquatDate.txt", true));
      bwd.write(date+"\r\n");
      //bwd.write("test"+"\r\n");
      bwd.close();
      BufferedWriter bwc = new BufferedWriter(new FileWriter(getFilesDir() + "SquatCnt.txt", true));
      bwc.write(cnt+"\r\n");
      //bwc.write(130+"\r\n");
      bwc.close();
      Toast.makeText(this,"저장완료", Toast.LENGTH_SHORT).show();
    }catch (Exception e){
      e.printStackTrace();
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  //갯수 표시
  public static void setCountTxt(String countTxt) {
    LivePreviewActivity.countTxt.setText(countTxt);
    Log.e("Check count","Preview : "+countTxt);
  }

  //초기화 메소드
  public void initCount(){
    PoseGraphic.checkpoint=0;
    PoseGraphic.count=0;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.live_preview_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.settings) {
      Intent intent = new Intent(this, SettingsActivity.class);
      intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, LaunchSource.LIVE_PREVIEW);
      startActivity(intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onResume() { //활동 재개
    super.onResume();
    Log.d("lifecycle : ", "onResume");
    createCameraSource();
    startCameraSource();
  }
  /** Stops the camera. */
  @Override
  protected void onPause() {
    Log.d("lifecycle : ", "onPause");
    super.onPause();
    preview.stop();
  }
  @Override
  public void onDestroy() {
    Log.d("lifecycle : ", "onDestroy");
    super.onDestroy();
    if (cameraSource != null) {
      cameraSource.release();
    }
  }

  public void createCameraSource() {
    // If there's no existing cameraSource, create one.
    if (cameraSource == null) {
      cameraSource = new CameraSource(this, graphicOverlay);
    }

    try{
      PoseDetectorOptionsBase poseDetectorOptions =
              PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);
      boolean shouldShowInFrameLikelihood =
              PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this);
      Log.i(TAG, "Using Pose Detector with options " + poseDetectorOptions);

      cameraSource.setMachineLearningFrameProcessor(
              new PoseDetectorProcessor(this, poseDetectorOptions, shouldShowInFrameLikelihood));
    }catch (Exception e){
      Log.e(TAG, "Can not create image processor: POSE_DETECTION", e);
      Toast.makeText(
              getApplicationContext(),
              "Can not create image processor: " + e.getMessage(),
              Toast.LENGTH_LONG)
              .show();
    }
  }

  public void startCameraSource() {
    if (cameraSource != null) {
      try {
        if (preview == null) {
          Log.d(TAG, "resume: Preview is null");
        }
        if (graphicOverlay == null) {
          Log.d(TAG, "resume: graphOverlay is null");
        }
        preview.start(cameraSource, graphicOverlay);
      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        cameraSource.release();
        cameraSource = null;
      }
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {

  }

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

  }
  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    Log.d(TAG, "Set facing");
    if (cameraSource != null) {
      if (isChecked) {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
      } else {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
      }
    }
    preview.stop();
    startCameraSource();
  }
}