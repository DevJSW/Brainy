package com.brainy.erevu.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.R;
import com.brainy.erevu.Pojos.Ad;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AdminPostAdActivity extends AppCompatActivity {

    private RecyclerView adsList;
    LinearLayoutManager mLinearlayout;
    private DatabaseReference mDatabaseAds;

    private Uri resultUri = null;
    private Uri mImageUri = null;
    private static int GALLERY_REQUEST =1;

    String ad_key = null;

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private ImageView adImg;
    private Button saveBtn;
    private ProgressDialog mprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_post_ad);

        Window window = AdminPostAdActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( AdminPostAdActivity.this, R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();

        //ADS LAYOUT
        adsList = (RecyclerView) findViewById(R.id.ads_recycler_view);
        final LinearLayoutManager adsLayoutManager = new LinearLayoutManager(AdminPostAdActivity.this, LinearLayoutManager.HORIZONTAL, false);
        adsList.setLayoutManager(adsLayoutManager);

        mDatabaseAds = FirebaseDatabase.getInstance().getReference().child("Ads");
        mDatabaseAds.keepSynced(true);
        mprogress = new ProgressDialog(this);
        adImg = (ImageView) findViewById(R.id.ad_image);
        adImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        saveBtn = (Button) findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initSave();
            }
        });

        loadAds();
    }

    private void initSave() {
        mprogress.setMessage("Posting, please wait...");
        mprogress.show();

        final DatabaseReference newPost = mDatabaseAds.push();
        StorageReference filepath = mStorage.child("ads_image").child(/*resultUri*/mImageUri.getLastPathSegment());

        filepath.putFile(/*resultUri*/mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                newPost.child("ads_image").setValue(downloadUrl.toString());

                mprogress.dismiss();

                Toast.makeText(AdminPostAdActivity.this, "Saved successfully!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAds() {

        FirebaseRecyclerAdapter<Ad, AdminPostAdActivity.adsViewHolder> firebaseRecyclerAdapter = new  FirebaseRecyclerAdapter<Ad, AdminPostAdActivity.adsViewHolder>(

                Ad.class,
                R.layout.ads_item,
                AdminPostAdActivity.adsViewHolder.class,
                mDatabaseAds


        ) {
            @Override
            protected void populateViewHolder(final AdminPostAdActivity.adsViewHolder viewHolder, final Ad model, int position) {

                ad_key = getRef(position).getKey();

                //viewHolder.setName(model.getSender_name());
                //viewHolder.setImage(getApplicationContext(), model.getSender_image());
                viewHolder.setImage(getApplicationContext(), model.getAds_image());

                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        // custom dialog
                        final Dialog dialog = new Dialog(AdminPostAdActivity.this);
                        dialog.setContentView(R.layout.admin_popup_dialog);
                        dialog.setTitle("Ads Options");

                        LinearLayout deleteLiny = (LinearLayout) dialog.findViewById(R.id.deleteLiny);
                        deleteLiny.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                AlertDialog diaBox = AskOption();
                                diaBox.show();
                                dialog.dismiss();
                            }
                        });

                        LinearLayout deleteAllLiny = (LinearLayout) dialog.findViewById(R.id.deleteAllLiny);
                        deleteAllLiny.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                AlertDialog diaBox = AskOption2();
                                diaBox.show();
                                dialog.dismiss();
                            }
                        });


                        // if button is clicked, close the custom dialog

                        dialog.show();
                        return false;
                    }


                });
            }

            private AlertDialog AskOption2() {

                AlertDialog myQuittingDialogBox =new AlertDialog.Builder(AdminPostAdActivity.this)
                        //set message, title, and icon
                        .setTitle("Delete Alert!")
                        .setMessage("Do you want to delete ALL this Ad?")

                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                //your deleting code

                                mDatabaseAds.removeValue();
                                dialog.dismiss();
                                Toast.makeText(AdminPostAdActivity.this, "ALL Ads deleted!",Toast.LENGTH_SHORT).show();
                            }

                        })


                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        })
                        .create();
                return myQuittingDialogBox;
            }

            private AlertDialog AskOption() {
                AlertDialog myQuittingDialogBox =new AlertDialog.Builder(AdminPostAdActivity.this)
                        //set message, title, and icon
                        .setTitle("Delete Alert!")
                        .setMessage("Do you want to delete this Ad?")

                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                //your deleting code

                                mDatabaseAds.child(ad_key).removeValue();
                                dialog.dismiss();
                                Toast.makeText(AdminPostAdActivity.this, "Ad deleted!",Toast.LENGTH_SHORT).show();
                            }

                        })


                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        })
                        .create();
                return myQuittingDialogBox;
            }

        };

        adsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class adsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageView mPostImg;
        TextView mUnreadTxt, txname;

        public adsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            txname = (TextView) mView.findViewById(R.id.ad_desc);
            mPostImg = (ImageView) mView.findViewById(R.id.ad_image);

        }

        public void setName(String name) {

            TextView post_name = (TextView) mView.findViewById(R.id.ad_desc);
            post_name.setText(name);
        }

        public void setImage(final Context ctx, final String ads_image) {

            final ImageView civ = (ImageView) mView.findViewById(R.id.ad_image);

            Glide.with(ctx)
                    .load(ads_image).asBitmap()
                    .placeholder(R.drawable.ad_book)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .centerCrop()
                    .into(civ);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setAspectRatio(2, 1)
                    .start(AdminPostAdActivity.this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                adImg.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }*/

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();
            adImg.setImageURI(mImageUri);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:

        }

        return super.onOptionsItemSelected(item);
    }

}
