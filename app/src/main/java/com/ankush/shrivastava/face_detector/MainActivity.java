package com.ankush.shrivastava.face_detector;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {
    ImageView iv_image;
    Uri selectedImageUri;
    Bitmap tempBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        initViews();
    }

    public void initViews(){
        iv_image=findViewById(R.id.iv_image);
    }

    public void btn_import_event(View v){
        Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null && data.getData()!=null){
            selectedImageUri=data.getData();
            Toast.makeText(getApplicationContext(),selectedImageUri.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    public void btn_add_event(View v) throws FileNotFoundException {
        Bitmap bitmap=decodeBitmapUri(this,selectedImageUri);
        Paint myRect=getRectangularBox();
        Canvas canvas=getCanvas(bitmap);
        FaceDetector faceDetector=getFaceDetector();

        // detect Face
        Frame frame=new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces=faceDetector.detect(frame);
        faceDetector.release();

        //Drawing Rect Over Face
        drawRectangle(canvas,faces,myRect,tempBitmap);


    }
    public Paint getRectangularBox(){
     Paint myRect=new Paint();
     myRect.setStrokeWidth(5);
     myRect.setColor(Color.RED);
     myRect.setStyle(Paint.Style.STROKE);
     return myRect;
    }

    public Canvas getCanvas(Bitmap myBitmap){
        tempBitmap= Bitmap.createBitmap(myBitmap.getWidth(),myBitmap.getHeight(),Bitmap.Config.RGB_565);
        Canvas canvas=new Canvas(tempBitmap);
        canvas.drawBitmap(myBitmap,0,0,null);
        return canvas;
    }

    public FaceDetector getFaceDetector(){
        FaceDetector faceDetector=new FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                .build();

        if(!faceDetector.isOperational()){
            new AlertDialog.Builder(getApplicationContext())
                    .setMessage("Could Not Set Up Detector");
            return null;
        }
        return faceDetector;
    }

    public void drawRectangle(Canvas canvas,SparseArray<Face> faces,Paint myRect,Bitmap myBitmap){


        for (int i=0;i<faces.size();i++){
            Face thisFace=faces.valueAt(i);
            float x1=thisFace.getPosition().x;
            float y1=thisFace.getPosition().y;
            float x2=x1 + thisFace.getWidth();
            float y2=y1 + thisFace.getHeight();
            canvas.drawRoundRect(new RectF(x1,y1,x2,y2),2,2,myRect);
        }
        iv_image.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));
    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inMutable = true;
        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }

}
