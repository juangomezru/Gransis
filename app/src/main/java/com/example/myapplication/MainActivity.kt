package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.ml.PlantDiseaseModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MainActivity : AppCompatActivity() {

    lateinit var selectBtn: ImageButton
    lateinit var predBtn: Button
    lateinit var resView: TextView
    lateinit var resView2: TextView
    lateinit var imageView: ImageView
    lateinit var bitmap: Bitmap
    lateinit var cameraBtn: ImageButton
    private val mModelPath = "plant_disease_model.tflite"
    private val mLabelPath = "labels.txt"
    private val mInputSize = 224
    private lateinit var mClassifier: Classifier



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectBtn = findViewById(R.id.selectBtn)
        predBtn = findViewById(R.id.predBtn)
        resView = findViewById(R.id.resView)
        resView2 = findViewById(R.id.resView2)
        imageView = findViewById(R.id.imageView)
        cameraBtn = findViewById(R.id.cameraBtn)
        mClassifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)

        //image procesor

        var imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()



        selectBtn.setOnClickListener {
            var intent = Intent()
            intent.setAction(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            startActivityForResult(intent,100)
            resView.setText("")
            resView2.text = ""
        }
        cameraBtn.setOnClickListener {
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(callCameraIntent, 200)
            resView.setText("")
            resView2.text = ""
        }

        predBtn.setOnClickListener(){
            val results = mClassifier.recognizeImage(bitmap).firstOrNull()
            if(results?.confidence == 0.0f){
                resView.text = "No es posible determinarlo"
                resView2.text = "0"
            } else {
                resView.text = results?.title
                resView2.text = ((results?.confidence)?.times(100)).toString() + "%"
            }





        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK){
            var uri = data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            bitmap = scaleImage(bitmap)
            imageView.setImageBitmap(bitmap)
        } else if(requestCode == 200 && resultCode == RESULT_OK){
            bitmap = data?.extras!!.get("data") as Bitmap
            bitmap = scaleImage(bitmap)
            imageView.setImageBitmap(bitmap)
        }
    }
    fun scaleImage(bitmap: Bitmap?): Bitmap {
        val orignalWidth = bitmap!!.width
        val originalHeight = bitmap.height
        val scaleWidth = mInputSize.toFloat() / orignalWidth
        val scaleHeight = mInputSize.toFloat() / originalHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, orignalWidth, originalHeight, matrix, true)
    }
}
