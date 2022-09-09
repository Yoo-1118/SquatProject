package or.kr.ajouhosp.stroke.posedetector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.Nullable;

import or.kr.ajouhosp.stroke.GraphicOverlay;
import or.kr.ajouhosp.stroke.GraphicOverlay.Graphic;
import or.kr.ajouhosp.stroke.R;
import or.kr.ajouhosp.stroke.activity.LivePreviewActivity;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;
import java.util.Locale;

/** Draw the detected pose in preview. */
public class PoseGraphic extends Graphic {

    private static final float DOT_RADIUS = 8.0f;
    private static final float IN_FRAME_LIKELIHOOD_TEXT_SIZE = 30.0f;
    private final Pose pose;
    private final boolean showInFrameLikelihood;
    private final Paint leftPaint;
    private final Paint rightPaint;
    private final Paint whitePaint;
    public static int checkpoint = 0;
    public static int count = 0;
    private static float req = 0;
    private static int reqCheck=0;
    public PoseGraphic(GraphicOverlay overlay, Pose pose, boolean showInFrameLikelihood) {
        super(overlay);
        this.pose = pose;
        this.showInFrameLikelihood = showInFrameLikelihood;

        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        whitePaint.setTextSize(IN_FRAME_LIKELIHOOD_TEXT_SIZE);
        leftPaint = new Paint();
        leftPaint.setColor(Color.WHITE);
        leftPaint.setTextSize(100);
        rightPaint = new Paint();
        rightPaint.setColor(Color.YELLOW);
        rightPaint.setTextSize(50);
        LivePreviewActivity.setCountTxt(String.valueOf(count));
    }

    public static String lh="init",rh="",lf="",rf="",cp="",ct="";
    @Override
    public void draw(Canvas canvas) {
        drawText(canvas, lh,rh,lf,rf,cp,count,rightPaint,leftPaint);

        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();

        if (landmarks.isEmpty()) {
            return;
        }

        // Draw all the points
        for (PoseLandmark landmark : landmarks) {
            drawPoint(canvas, landmark.getPosition(), whitePaint);
            if (showInFrameLikelihood) {
                canvas.drawText(
                        String.format(Locale.US, "%.2f", landmark.getInFrameLikelihood()),
                        translateX(landmark.getPosition().x),
                        translateY(landmark.getPosition().y),
                        whitePaint);
            }
        }

        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        PoseLandmark leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY);
        PoseLandmark rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY);
        PoseLandmark leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX);
        PoseLandmark rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX);
        PoseLandmark leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB);
        PoseLandmark rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB);
        PoseLandmark leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL);
        PoseLandmark rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL);
        PoseLandmark leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX);
        PoseLandmark rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX);

        // Left body
        drawLine(canvas, leftShoulder.getPosition(), leftElbow.getPosition(), leftPaint);
        drawLine(canvas, leftElbow.getPosition(), leftWrist.getPosition(), leftPaint);
        drawLine(canvas, leftShoulder.getPosition(), leftHip.getPosition(), leftPaint);
        drawLine(canvas, leftHip.getPosition(), leftKnee.getPosition(), leftPaint);
        drawLine(canvas, leftKnee.getPosition(), leftAnkle.getPosition(), leftPaint);
        drawLine(canvas, leftWrist.getPosition(), leftThumb.getPosition(), leftPaint);
        drawLine(canvas, leftWrist.getPosition(), leftPinky.getPosition(), leftPaint);
        drawLine(canvas, leftWrist.getPosition(), leftIndex.getPosition(), leftPaint);
        drawLine(canvas, leftAnkle.getPosition(), leftHeel.getPosition(), leftPaint);
        drawLine(canvas, leftHeel.getPosition(), leftFootIndex.getPosition(), leftPaint);

        // Right body
        drawLine(canvas, rightShoulder.getPosition(), rightElbow.getPosition(), rightPaint);
        drawLine(canvas, rightElbow.getPosition(), rightWrist.getPosition(), rightPaint);
        drawLine(canvas, rightShoulder.getPosition(), rightHip.getPosition(), rightPaint);
        drawLine(canvas, rightHip.getPosition(), rightKnee.getPosition(), rightPaint);
        drawLine(canvas, rightKnee.getPosition(), rightAnkle.getPosition(), rightPaint);
        drawLine(canvas, rightWrist.getPosition(), rightThumb.getPosition(), rightPaint);
        drawLine(canvas, rightWrist.getPosition(), rightPinky.getPosition(), rightPaint);
        drawLine(canvas, rightWrist.getPosition(), rightIndex.getPosition(), rightPaint);
        drawLine(canvas, rightAnkle.getPosition(), rightHeel.getPosition(), rightPaint);
        drawLine(canvas, rightHeel.getPosition(), rightFootIndex.getPosition(), rightPaint);

        drawLine(canvas, leftShoulder.getPosition(), rightShoulder.getPosition(), whitePaint);
        drawLine(canvas, leftHip.getPosition(), rightHip.getPosition(), whitePaint);

        //각도 변수
        double dx,dy,rad;
        double leftHandDegree;
        double rightHandDegree;
        double leftLegDegree=0;
        double rightLegDegree=0;
        
        //상체
        dx = leftElbow.getPosition().x-leftShoulder.getPosition().x;
        dy = leftElbow.getPosition().y-leftShoulder.getPosition().y;
        rad= Math.atan2(dx, dy);

        if(leftShoulder.getPosition().x<leftHip.getPosition().x) {
            leftHandDegree =(rad * 180) / Math.PI;
        }else{
            leftHandDegree = Math.abs((rad * 180) / Math.PI);
        }

        dx = rightElbow.getPosition().x-rightShoulder.getPosition().x;
        dy = rightElbow.getPosition().y-rightShoulder.getPosition().y;
        rad= Math.atan2(dx, dy);

        if(rightShoulder.getPosition().x>rightHip.getPosition().x) {
            rightHandDegree =Math.abs((rad * 180) / Math.PI);
        }else{
            rightHandDegree = Math.abs((rad * 180) / Math.PI);
        }

        if(Math.round(leftHandDegree)<0){
            lh = "왼팔(0)";
        }else{lh = "왼팔"+"("+Math.round(leftHandDegree)+")";}

        if(Math.round(rightHandDegree)<0){
            rh = "오른팔(0)";
        }else{rh = "오른팔"+"("+Math.round(rightHandDegree)+")";}
        
        //기존 하체 측정
        /*dx = leftKnee.getPosition().x-leftHip.getPosition().x;
        dy = leftKnee.getPosition().y-leftHip.getPosition().y;
        rad= Math.atan2(dx, dy);

        if(leftShoulder.getPosition().x<leftHip.getPosition().x){
            leftLegDegree =(rad*180)/Math.PI;
        }else{
            leftLegDegree = (Math.abs(rad*180)/Math.PI);
        }

        dx = rightKnee.getPosition().x-rightHip.getPosition().x;
        dy = rightKnee.getPosition().y-rightHip.getPosition().y;
        rad= Math.atan2(dx, dy);

        if(rightShoulder.getPosition().x>rightHip.getPosition().x){
            rightLegDegree = (Math.abs((rad*180)/Math.PI));
        }else{
            rightLegDegree = (rad*180)/Math.PI;
        }

        if(Math.round(leftLegDegree)<0){
            lf="왼발(0)";
        }else{lf = "왼발"+"("+Math.round(leftLegDegree)+")";}

        if(Math.round(rightLegDegree)<0){
            rf = "오른발(0)";
        }else{rf = "오른발"+"("+Math.round(rightLegDegree)+")";}*/

        //스쿼트 하체
        dx = leftKnee.getPosition().x-leftHip.getPosition().x;
        dy = leftKnee.getPosition().y-leftHip.getPosition().y;
        rad= Math.atan2(dx, dy);
        leftLegDegree =Math.abs((rad*180)/Math.PI);

        dx = rightKnee.getPosition().x-rightHip.getPosition().x;
        dy = rightKnee.getPosition().y-rightHip.getPosition().y;
        rad= Math.atan2(dx, dy);
        rightLegDegree = Math.abs((rad*180)/Math.PI);

        lf = "왼다리"+"("+Math.round(leftLegDegree)+")";
        rf = "오른다리"+"("+Math.round(rightLegDegree)+")";

        if(leftLegDegree<5 && reqCheck==0){
            req = leftShoulder.getPosition().y;
            reqCheck = 1;
        }

        if(leftLegDegree>=50 && rightLegDegree>=50 || leftShoulder.getPosition().y>req+60){
            if(reqCheck==1){
                //조건 1 : 양무릎 굽혔을 때
                //조건 2 : 양팔 올리고 서있을 때보다 어깨 위치 낮아졌을 때
                checkpoint = 1;
            }
        }
        Log.e("checking","checkpoint : "+checkpoint);
        if(leftLegDegree<=30 && rightLegDegree<=30){
            if(checkpoint==1){
                count += 1;
                checkpoint = 0;
                //play();
                reqCheck = 0;
            }
        }

        cp = "check("+checkpoint+")";
        ct = "갯수("+count+")";
    }

/**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    void drawPoint(Canvas canvas, @Nullable PointF point, Paint paint) {
        if (point == null) {
            return;
        }
        canvas.drawCircle(translateX(point.x), translateY(point.y), DOT_RADIUS, paint);
    }

    void drawLine(Canvas canvas, @Nullable PointF start, @Nullable PointF end, Paint paint) {
        if (start == null || end == null) {
            return;
        }
        canvas.drawLine(
                translateX(start.x), translateY(start.y), translateX(end.x), translateY(end.y), paint);
    }

    //측정값 표시
    void drawText(Canvas canvas,String dir,String dir2,String dir3,String dir4,String dir5,int dir6,Paint paint,Paint paint2){
        if(dir == "init"){
            //canvas.drawText("개수("+dir6+")",0,canvas.getHeight()/3+100,paint);
            //canvas.drawText("대기 중입니다.",0,canvas.getHeight()/3,paint);
        }else{
            /*canvas.drawText(dir,0,canvas.getHeight()/3,paint);
            canvas.drawText(dir2,250,canvas.getHeight()/3,paint);
            canvas.drawText(dir3,0,canvas.getHeight()/3+150,paint);
            canvas.drawText(dir4,250,canvas.getHeight()/3+150,paint);
            canvas.drawText("개수("+dir6+")",0,canvas.getHeight()/3+300,paint);*/
            Log.e("Check count","poseGraphic : "+dir6);
            LivePreviewActivity.setCountTxt(String.valueOf(dir6));
        }
    }
    
    //효과음
    public void play(){
        Context context = getApplicationContext();
        MediaPlayer player = MediaPlayer.create(context, R.raw.ringtone);
        player.start();
    }
}