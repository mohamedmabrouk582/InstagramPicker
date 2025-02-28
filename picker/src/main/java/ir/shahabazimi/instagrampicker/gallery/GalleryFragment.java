package ir.shahabazimi.instagrampicker.gallery;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import ir.shahabazimi.instagrampicker.InstagramPicker;
import ir.shahabazimi.instagrampicker.R;
import ir.shahabazimi.instagrampicker.classes.BackgroundActivity;
import ir.shahabazimi.instagrampicker.classes.TouchImageView;
import ir.shahabazimi.instagrampicker.filter.FilterActivity;

import static android.app.Activity.RESULT_OK;
import static ir.shahabazimi.instagrampicker.classes.Statics.INTENT_FILTER_ACTION_NAME;


public class GalleryFragment extends Fragment {

    public GalleryFragment() {
    }

    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private ImageView multiSelectBtn;
    private TouchImageView preview;
    private boolean multiSelect = false;
    private List<GalleryModel> data = new ArrayList<>();
    private String selectedPic = "";
    private List<String> selectedPics;
    private Context context;
    private FragmentActivity activity;


    public static GalleryFragment getInstance(ArrayList<String> imgs){
        GalleryFragment fragment=new GalleryFragment();
        Bundle bundle=new Bundle();
        bundle.putStringArrayList("SELECTED_IMG",imgs);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(InstagramPicker.hasShowHeader?R.layout.fragment_gallery_with_header:R.layout.fragment_gallery, container, false);
        context = getContext();
        activity = getActivity();
        assert activity != null;
        ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(getString(R.string.instagrampicker_gallery_title));
        actionBar.show();
        setHasOptionsMenu(true);
        try {
            selectedPics= getArguments().getStringArrayList("SELECTED_IMG");
        }catch (Exception e){}

        multiSelectBtn = v.findViewById(R.id.gallery_multiselect);
        if (!InstagramPicker.multiSelect) {
            multiSelectBtn.setVisibility(View.GONE);
        }else {
            multiSelect=true;
        }
        multiSelectBtn.setOnClickListener(w -> {

            int positionView = ((GridLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).findFirstVisibleItemPosition();
            multiSelect = !multiSelect;
            multiSelectBtn.setImageResource(multiSelect ? R.mipmap.ic_multi_enable : R.mipmap.ic_multi_disable);
            adapter = new GalleryAdapter(data, new GalleySelectedListener() {
                @Override
                public void onSingleSelect(String address) {
                    // preview.setImageURI(Uri.parse(address));
                    try {
                        preview.setImageBitmap(scale(address));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    selectedPic = address;
                }

                @Override
                public void onMultiSelect(List<String> addresses) {
                    if (!addresses.isEmpty()) {
                        selectedPic = "";
                        //  preview.setImageURI(Uri.parse(addresses.get(addresses.size() - 1)));
                        try {
                            preview.setImageBitmap(scale(addresses.get(addresses.size() - 1)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        selectedPics = addresses;
                    }

                }
            }, multiSelect);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            recyclerView.getLayoutManager().scrollToPosition(positionView);
        });

        preview = v.findViewById(R.id.gallery_view);

        recyclerView = v.findViewById(R.id.gallery_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4, RecyclerView.VERTICAL, false));

        adapter = new GalleryAdapter(data, new GalleySelectedListener() {
            @Override
            public void onSingleSelect(String address) {
                // preview.setImageURI(Uri.parse(address));
                try {
                    preview.setImageBitmap(scale(address));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                selectedPic = address;
                open(selectedPic);
            }

            @Override
            public void onMultiSelect(List<String> addresses) {
                selectedPic = "";
                if (!addresses.isEmpty()) {
                    //   preview.setImageURI(Uri.parse(addresses.get(0)));
                    try {
                        preview.setImageBitmap(scale(addresses.get(0)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    selectedPics = addresses;
                }

            }
        }, multiSelect);
        recyclerView.setAdapter(adapter);

        getPicturePaths();

        return v;
    }

    private void open(String url){
        if (InstagramPicker.hasCrop){
            CropImage.activity(Uri.parse(url))
                    .setAspectRatio(InstagramPicker.x, InstagramPicker.y)
                    .start(context, this);
        } else if (InstagramPicker.hasFilter){
            Intent in = new Intent(requireContext(), FilterActivity.class);
            FilterActivity.picAddress = Uri.parse(url);
            FilterActivity.position = -1;
            startActivityForResult(in, 123);
            activity.finish();
        } else {
            if(InstagramPicker.addresses==null){
                InstagramPicker.addresses = new ArrayList<>();
            }
            InstagramPicker.addresses.add(url);
            activity.sendBroadcast(new Intent(INTENT_FILTER_ACTION_NAME));
            activity.finish();
            Objects.requireNonNull(BackgroundActivity.getInstance().getActivity()).finish();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Intent in = new Intent(getContext(), FilterActivity.class);
                in.putExtra("uri", resultUri);
                FilterActivity.picAddress = resultUri;
                startActivity(in);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_open) {
            int x = InstagramPicker.x;
            int y = InstagramPicker.y;



            if (!selectedPic.isEmpty()) {
                open(selectedPic);
//                if (InstagramPicker.hasCrop){
//                     CropImage.activity(Uri.parse(selectedPic))
//                        .setAspectRatio(x, y)
//                        .start(context, this);
//                }else {
//                    Intent in = new Intent(requireContext(), FilterActivity.class);
//                    FilterActivity.picAddress = Uri.parse(selectedPic);
//                    FilterActivity.position = 0;
//                    startActivityForResult(in, 123);
//                }

            } else if (selectedPics.size() == 1) {
                open(selectedPics.get(0));
//                if (InstagramPicker.hasCrop){
//                    CropImage.activity(Uri.parse(selectedPics.get(0)))
//                            .setAspectRatio(x, y)
//                            .start(context, this);
//                }else {
////                    Intent i = new Intent(getActivity(), MultiSelectActivity.class);
////                    MultiSelectActivity.addresses = selectedPics;
////                    startActivity(i);
////                    activity.overridePendingTransition(R.anim.bottom_up_anim, R.anim.bottom_down_anim);
//                    Intent in = new Intent(requireContext(), FilterActivity.class);
//                    FilterActivity.picAddress = Uri.parse(selectedPics.get(0));
//                    FilterActivity.position = 0;
//                    startActivityForResult(in, 123);
//                }

            } else if (selectedPics.size() > 1) {
                    Intent i = new Intent(getActivity(), MultiSelectActivity.class);
                    MultiSelectActivity.addresses = selectedPics;
                    startActivity(i);
                    activity.overridePendingTransition(R.anim.bottom_up_anim, R.anim.bottom_down_anim);
             }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() != 1) {
                cursor.close();
                return -1;
            }

            cursor.moveToFirst();
            return cursor.getInt(0);
        } else
            return -1;

    }

    private Bitmap scale(String address) throws Exception {
        Uri photoUri = Uri.parse(address);
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        int MAX_IMAGE_DIMENSION = 960;
        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
            float maxRatio = Math.max(widthRatio, heightRatio);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        return srcBitmap;
    }

    private void getPicturePaths() {
        int pos=0;
        Uri allImagesuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media._ID};

        try {
            if (getActivity() != null) {
                Cursor cursor = getActivity().getContentResolver().query(allImagesuri, projection, null, null, MediaStore.Images.Media.DATE_ADDED);

                if (cursor != null && cursor.moveToFirst()) {

                    do {
                        String datapath = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))).toString();
                        GalleryModel model = new GalleryModel(datapath, false);
                          try {
                              Log.d("erererer",equals(BitmapFactory.decodeFile(datapath),BitmapFactory.decodeFile(selectedPics.get(pos++)))+"");
                              model.setSelected(datapath.equals(selectedPics.get(pos++)));
                          }catch (Exception e){}
                        data.add(0, model);
                        adapter.notifyItemInserted(data.size());

                    } while (cursor.moveToNext());
                    if (!data.get(0).getAddress().isEmpty()) {
                        selectedPic = data.get(0).getAddress();
                        //   preview.setImageURI(Uri.parse(selectedPic));
                        try {
                            preview.setImageBitmap(scale(selectedPic));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cursor.close();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean equals(Bitmap bitmap1, Bitmap bitmap2) {
        ByteBuffer buffer1 = ByteBuffer.allocate(bitmap1.getHeight() * bitmap1.getRowBytes());
        bitmap1.copyPixelsToBuffer(buffer1);

        ByteBuffer buffer2 = ByteBuffer.allocate(bitmap2.getHeight() * bitmap2.getRowBytes());
        bitmap2.copyPixelsToBuffer(buffer2);

        return Arrays.equals(buffer1.array(), buffer2.array());
    }

}
