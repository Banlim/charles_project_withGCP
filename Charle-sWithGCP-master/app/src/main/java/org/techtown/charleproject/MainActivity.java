package org.techtown.charleproject;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ArrayList<File> resultFile; // 내부 저장소 이미지 파일 배열
    private ArrayList<File> updateImageFileArr; // 업데이트 이미지 파일 배열
    private int ImageCount; // 내부 저장소 이미지 파일 개수
    private int updateImageCount = 0; //업데이트 후 전체 이미지 파일 개수
    private String TAG = "vision";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultFile = new ArrayList<>();
        updateImageFileArr = new ArrayList<>();
        //checkPermission();
        init();

        /*
        Button button = (Button) findViewById(R.id.return_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        */
        Handler handler = new Handler(){
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                startActivity(new Intent(MainActivity.this, WebviewActivity.class));
                finish();
            }
        };
        handler.sendEmptyMessageDelayed(0, 10000);

    }
    public void hello(FirebaseVisionImage image, String date, String encode, String name){

        final String imageDate = date;
        final String imageEncoding = encode;
        final String imageName = name;
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.2f)
                        .build();
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
        // FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getCloudImageLabeler(labelerOptions);

        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setRotation(FirebaseVisionImageMetadata.ROTATION_0)
                .build();


        Bitmap bt1 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.hala); // 인물 image
        FirebaseVisionImage image1 = FirebaseVisionImage.fromBitmap(bt1);
        final FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image1).addOnSuccessListener(this,
                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                                //  editText.setText("Success");
                                for (FirebaseVisionFace face : firebaseVisionFaces) {
                                    FirebaseVisionFace firebaseVisionFace;

                                    if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                        //editText.setText("Success");
                                        //faceList.add(tempFile);
                                        //imageView.setImageBitmap(bt2);
                                        Log.d("호이이잇", "성공");
                                        Log.d(TAG, "****************************");
                                        Log.d(TAG, "face [" + face + "]");
                                        Log.d(TAG, "Smiling Prob [" + face.getSmilingProbability() + "]");
                                        Log.d(TAG, "Left eye open [" + face.getLeftEyeOpenProbability() + "]");
                                        Log.d(TAG, "Right eye open [" + face.getRightEyeOpenProbability() + "]");
                                        InsertData task = new InsertData();
                                        task.execute("http://charlesgirls.ml/andInsert.php", imageDate, imageEncoding, imageName);


                                    } else {
                                        // 사실상 else문은 필요 없는 듯
                                        // imageView.setImageBitmap(bt3);
                                        Log.d("오잉용용", "notSucess");
                                        //editText.setText("notSuccess");
                                    }
                                }

                            }
                        })

                        .addOnFailureListener(this,
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("오잉용잉", "failure");
                                    }
                                });
    }

    public void checkPermission(){
        // 마시멜로 버전 이후부터 무조건 사용자에게 권한 요청을 해야한다고 함.

        // 사용자에게 외부저장소 읽기 권한을 요청하는 함수
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }

        // 사용자에게 외부저장소 쓰기 권한을 요청하는 함수
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.INTERNET)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET},
                        1);
            }
        }
    }
    private void cacheWrite(int ImageFileCount){
        String cacheFileName = "cacheFile.txt";
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String ImageFileCountString = String.valueOf(ImageFileCount);
        Log.d("TimeStamp", timeStamp);
        Log.d("cacheFilePath", this.getFilesDir().getAbsolutePath());

        try{
            FileOutputStream os = openFileOutput(cacheFileName, Context.MODE_PRIVATE);
            os.write(timeStamp.getBytes());
            os.write("\n".getBytes());
            os.write(ImageFileCountString.getBytes());
            os.flush();
            os.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    private String[] cacheRead(String cacheFileName){
        // cacheReadFile[0] : timeStamp
        // cacheReadFile[1] : FileCount
        String[] cacheReadFile = new String[2];

        try{
            FileInputStream is = openFileInput(cacheFileName);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
            cacheReadFile[0] = buffer.readLine();
            cacheReadFile[1] = buffer.readLine();
            buffer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        Log.d("cacheTimeStamp", cacheReadFile[0]);
        Log.d("cacheFileCount", cacheReadFile[1]);

        return cacheReadFile;
    }
    public void init(){
        //  checkPermission();
        String externalPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        String tempPath = externalPath+"/abc";
        String cacheFileName = "cacheFile.txt";
        String internalPath = this.getFilesDir().getAbsolutePath()+"/"+cacheFileName;
        File file = new File(internalPath);

        if(cacheFileName.equals(file.getName())){
            // 앱 처음 실행 시
            ImageCount = firstImagePathArrayCount(tempPath);
            personSeparate(resultFile);
            cacheWrite(updateImageCount);
        }
        else{
            // 이미지 업데이트 할 시
            String[] cacheFile = cacheRead(cacheFileName);
            Date date;
            if(updateCheckImagePathArray(tempPath, Integer.parseInt(cacheFile[0]))){
                try {
                    date = stringToDate(cacheFile[1]);
                    updateImageFileArr = updateImagePathArray(tempPath, date);
                    personSeparate(updateImageFileArr);
                    cacheWrite(ImageCount);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }


        // 권한이 허락 되면, 내부저장소에 미리 저장된 캐시 파일을 확인한다.
        // 그 캐시 파일을 열어서 이전까지 저장된 이미지 파일 개수와 날짜를 불러온다.
        // 만약 캐시 파일이 없다면, firstImagePathArrayCount 함수를 호출하고, 캐시파일을 생성한다.
        // first~~ 함수의 결과값을 통해 personSeparate 함수를 호출하여 인물로 분류된 이미지를 모델로 전송한다.
        // 캐시 파일이 있다면, 캐시 파일에 저장된 이미지 파일 개수를 비교하는 updateCheckImagePathArray 함수를 호출한다.
        // update 해야한다고 리턴값이 나오면, updateImagePathArray 함수를 호출한다.
        // update~~함수의 결과값을 통해 personSeparate 함수를 호출하여 업데이트 해야할 이미지 파일들만 모델로 전송한다.

        // 요런 내용을 곧 코드로 짤 계획이다.
        // 다 작성하면 주석 지워야징
    }
    public static boolean checkExternalAvailable(){

        // 외부저장소 사용할 수 있는지 확인하는 함수
        // 근데 쓸지 안 쓸지는 잘 모르겠음

        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        return false;
    }
    public static boolean checkExternalWriteable(){

        // 외부저장소에 write 할 수 있는지 확인하는 함수
        // 근데 쓸지 안 쓸지는 잘 모르겠음.
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
                return false;
            }
            else{
                return true;
            }
        }
        return false;
    }
    public int firstImagePathArrayCount(String path){

        // 앱 첫 실행 시 모든 이미지 파일을 업로드 하기 위한 함수

        File imageFile = new File(path);
        File [] imageFiles = imageFile.listFiles();
        File innerFile;
        String innerPath;
        int temp = 0;
        int imageCount = 0;

        for(int i = 0; i < imageFiles.length; i++){
            if(imageFiles[i].isDirectory()){
                Log.d("FirstDirectory", imageFiles[i].getName());
                innerPath = imageFiles[i].getAbsolutePath();
                temp = firstImagePathArrayCount(innerPath);
                imageCount += temp;
                Log.d("innerPath", innerPath);
            }
            else{
                if(imageFiles[i].getName().endsWith("jpg") || imageFiles[i].getName().endsWith("png")){
                    Log.d("FirstFiles", imageFiles[i].getName());
                    resultFile.add(imageFiles[i]);
                }
                imageCount++;
            }
        }
        Log.d("imageCount", String.valueOf(imageCount));
        return imageCount;
    }
    public boolean updateCheckImagePathArray(String path, int prevCount){

        // 현재 이미지 파일을 업데이트 해야하는지 말아야하는지 판단하는 함수
        // 이미지 개수로 비교

        String imageFilePath = path;
        File imageFile = new File(imageFilePath);
        File [] imageFiles = imageFile.listFiles();
        String innerPath;
        File innerFile;
        boolean TF = false;

        for(int i = 0; i < imageFiles.length; i++){
            if(imageFiles[i].isDirectory()){
                innerPath = imageFiles[i].getAbsolutePath();
                innerFile = new File(innerPath);
                if(!innerFile.isDirectory()){
                    Log.d("updateCheckCount", String.valueOf(innerFile.listFiles().length));
                    updateImageCount += innerFile.listFiles().length;
                }
            }
        }
        if(updateImageCount > prevCount){
            TF = true;
        }

        return TF;
    }
    public ArrayList<File> updateImagePathArray(String path, Date prevDate){

        // 최근 이미지 파일들만 업데이트 하는 함수

        String imagePath = path;
        File imageFIle = new File(imagePath);
        File[] imageFiles = imageFIle.listFiles();
        String innerPath;
        File innerFile;

        try{
            // ExifInterface -> 이미지가 가지고 있는 메타데이터 정보를 가져올 수 잇음.
            ExifInterface exif;
            for(int i = 0; i < imageFiles.length; i++){
                if(imageFiles[i].isDirectory()){
                    innerPath = imageFiles[i].getAbsolutePath();
                    innerFile = new File(innerPath);
                    updateImagePathArray(innerPath, prevDate);
                }
                else{
                    exif = new ExifInterface(imageFiles[i].getAbsolutePath());
                    String date;
                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    if(exif.getAttribute(ExifInterface.TAG_DATETIME) == null){
                        exif.setAttribute(ExifInterface.TAG_DATETIME, timeStamp);
                    }
                    date = exif.getAttribute(ExifInterface.TAG_DATETIME);
                    Date newDate = stringToDate(date);
                    Log.d("update Date : ", date);

                    // 최신 사진만 업데이트 하기 위해 날짜 비교
                    if(newDate.after(prevDate) && (imageFiles[i].getName().endsWith("jpg") || imageFiles[i].getName().endsWith("png"))){
                        updateImageFileArr.add(imageFiles[i]);
                        Log.d("updateFile", imageFiles[i].getName());
                    }
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return updateImageFileArr;
    }
    private Date stringToDate(String date) throws ParseException {
        if(date == null)
            return null;
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date to = transFormat.parse(date);

        return to;
    }
    public void personSeparate(ArrayList<File> imageFileArray){
        // 인물인지 아닌지 분류하는 함수
        // Firebase -> ML Kit face Detction 이용
        Log.d("person", "호잇");
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector();
        Bitmap bitmap;
       // Bitmap bitmap1 = BitmapFactory.decodeFile(imageFileArray.get(4).getAbsolutePath());
        //ImageView imageView = (ImageView)findViewById(R.id.imageView);
       // String btEncode = image2base64(bitmap1);
       // Bitmap btDecode = decodeBase64(btEncode);
        //imageView.setImageBitmap(btDecode);

        ExifInterface exif;
        for(int i = 0; i < imageFileArray.size(); i++) {
            try {
                Log.d("firefire", "들어왔는가");
                exif = new ExifInterface(imageFileArray.get(i).getAbsolutePath());
                bitmap = BitmapFactory.decodeFile(imageFileArray.get(i).getAbsolutePath());
                String date;
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                if(exif.getAttribute(ExifInterface.TAG_DATETIME) == null){
                    exif.setAttribute(ExifInterface.TAG_DATETIME, timeStamp);
                }
                date = exif.getAttribute(ExifInterface.TAG_DATETIME);
                Log.d("Date : ", date);
                String encode = image2base64(bitmap);
                String name = imageFileArray.get(i).getName();
                Log.d("imageName : ", name);
                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
                hello(image, date, encode, name);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public static String image2base64(Bitmap bitmap){
        String imageCode = null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Log.d("width : ", Integer.toString(width));
        Log.d("height : ", Integer.toString(height));
        Bitmap resizeImage = Bitmap.createScaledBitmap(bitmap, 264, 191, true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] ba = bos.toByteArray();
        imageCode = Base64.encodeToString(ba, Base64.DEFAULT);
        Log.d("encodeSize : ", Integer.toString(imageCode.length()));

        String hexStr = byteArrayToHex(ba);
        Log.d("hex : ", hexStr);

        Log.d("encode : ", imageCode);
        return imageCode;
    }
    public static Bitmap decodeBase64(String input){
        byte[] bytes = Base64.decode(input, 0);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }
    static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for(final byte b: a)
            sb.append(String.format("%02x ", b&0xff));
        return sb.toString();
    }

    class InsertData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("preExecute", "wait");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("postExecute", "Post response - " + result);
        }

        @Override
        protected String doInBackground(String... params) {
            // php 파일을 실행시킬 수 있는 주소와 전송 데이터를 준비한다.
            // POST 방식으로 데이터를 전달할 때에는 데이터가 주소에 직접 입력되지 않는다.
            String serverURL = (String)params[0];
            String imageDate = (String)params[1];
            String imageEncode = (String)params[2]; // base64 인코딩
            String imageName = (String)params[3];

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";


            String tmpimage = imageEncode;
            try {
                tmpimage = URLEncoder.encode(imageEncode, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            // HTTP 메시지 본문에 포함되어 전송되기 때문에, 따로 데이터를 준비해야한다.
            // 전송할 데이터는 "이름=값" 형식이고, 여러 개를 보내야 할 경우에는 항목 사이에 &를 추가한다.
            // 여기에 적어준 이름은 나중에 php에서 사용하여 값을 얻게 된다.
            String postParameters = "imageDate=" + imageDate + "&imageEncode=" + tmpimage + "&imageName=" + imageName;

            try{
                // HttpURLConnection 클래스를 사용하여 POST 방식으로 데이터를 전송한다.
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(20000); // 5초 안에 응답이 없으면 Exception
                httpURLConnection.setConnectTimeout(20000); // 5초 안에 연결이 되지 않으면 Exception
                httpURLConnection.setRequestMethod("POST"); // POST 방식으로 요청
                //httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                //httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                //DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
                //dos.writeBytes(twoHyphens+boundary+lineEnd);
               // dos.writeBytes(Content-Disposition:form-data;imageDate=\"imageDate\";imageEncode);

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("responseStatusCode", "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == httpURLConnection.HTTP_OK){
                    // 정상적인 응답 데이터
                    Log.d("responseSuccess", "Success");
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    // 에러 발생 시
                    Log.d("responseFail", "Failure");
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder stringBuilder = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    stringBuilder.append(line);
                }
                bufferedReader.close();

                // 저장된 데이터를 Stirng으로 변환하여 리턴
                return stringBuilder.toString().trim();


            }
            catch (Exception e){
                Log.d("connectError", "InsertData Error ", e);
                return new String("Error : " + e.getMessage());
            }
        }
    }
}