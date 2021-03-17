package ir.shahabazimi.instagrampickerexample;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import ir.shahabazimi.instagrampicker.InstagramPicker;
import ir.shahabazimi.instagrampicker.classes.MultiListener;
import ir.shahabazimi.instagrampicker.classes.SingleListener;


public class MainActivity extends AppCompatActivity implements SingleListener, MultiListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InstagramPicker a = new InstagramPicker(MainActivity.this);
        //a.setShowHeader(true).setHasCrop(true);
        //a.setHasFilter(true);
        //a.setMultiCamera(true);
        findViewById(R.id.main_button).setOnClickListener(w -> {
             a.show(9,9,100,this);
            // CropXRatio and CropYRatio are ratio for cropping for example if you want to limit the users to
            // only crop in 16:9 put 16,9

            // numberOfPictures allows the user to choose more than on picture between 2 and 1000
            //           a.show(1, 1,5, addresses -> {
            //  receive image addresses in here
//            });

            // this way for just a picture
//            a.show(1, 1, address -> {
//                 receive image address in here
//            });

        });

    }



    @Override
    public void selectedPic(String address,boolean fromCamera) {
        Toast.makeText(this, fromCamera+"", Toast.LENGTH_SHORT).show();
        ImageView viewById = findViewById(R.id.imageView);

        viewById.setImageURI(Uri.parse(address));
    }

    @Override
    public void selectedPics(List<String> addresses,boolean fromCamera) {
        ImageView viewById = findViewById(R.id.imageView);

        viewById.setImageURI(Uri.parse(addresses.get(0)));

        Toast.makeText(this, addresses.toString()+":"+fromCamera, Toast.LENGTH_SHORT).show();

    }
}
