package com.brainy.erevu.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brainy.erevu.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.brainy.erevu.R.layout.spinner_item;

public class ProfileEditActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private ImageView userImg;
    private EditText inputName, inputBio;
    private Button saveBtn;
    private ProgressDialog mprogress;
    private Uri resultUri = null;
    private Uri mImageUri = null;
    private static int GALLERY_REQUEST =1;

    Spinner genderSpinner, ageSpinner, careerSpinner, studentSpinner;
    String selectedAGE = null;
    String selectedGENDER = null;
    String selectedCAREER = null;
    String selectedCOARSE = null;
    String careerList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        Window window = ProfileEditActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( ProfileEditActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        userImg = (ImageView) findViewById(R.id.userimg);
        inputName = (EditText) findViewById(R.id.input_name);
        inputBio = (EditText) findViewById(R.id.input_bio);
        saveBtn = (Button) findViewById(R.id.save_btn);
        mprogress = new ProgressDialog(this);
        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

               /* if (dataSnapshot.hasChild("status") || dataSnapshot.hasChild("city") || dataSnapshot.hasChild("address")) {*/

               if (dataSnapshot.hasChild("gender")) {
                   String user_gender = dataSnapshot.child("gender").getValue().toString();

               }

               if (dataSnapshot.hasChild("bio")) {
                   String bio = dataSnapshot.child("bio").getValue().toString();
                   inputBio.setText(bio);
               }
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                inputName.setText(name);

               /* Glide.with(getApplicationContext())
                        .load(image)
                        .placeholder(R.drawable.placeholder_image)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(userImg);*/

                Glide.with(getApplicationContext())
                        .load(image).asBitmap()
                        .placeholder(R.drawable.placeholder_image)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .centerCrop()
                        .into(new BitmapImageViewTarget(userImg) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                userImg.setImageDrawable(circularBitmapDrawable);
                            }
                        });

              /*  } else {}*/

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initSave();
            }
        });
        //initSpinners();
    }

   /* private void initSpinners() {

        // Initializing a String Array
        String[] gender = new String[]{
                "Gender",
                "Male",
                "Female"
        };

        final List<String> genderList = new ArrayList<>(Arrays.asList(gender));

        // Initializing a String Array
        String[] age = new String[]{
                "Age",
                "Below - 13yrs",
                "Between (14 - 17)yrs",
                "Between (18 - 26)yrs",
                "Between (27 - 34)yrs",
                "Above - 35yrs"
        };

        final List<String> ageList = new ArrayList<>(Arrays.asList(age));


        /////////////////////////////////////// GENDER SPINNER ////////////////////////////////////////

        genderSpinner = (Spinner) findViewById(R.id.gender_spinner);

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                ProfileEditActivity.this, R.layout.spinner_dialog_item,genderList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint

                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(spinner_item);
        genderSpinner.setAdapter(spinnerArrayAdapter);

        ////GET SELECTED AGE FROM SPINNER
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    selectedGENDER = (String) parent.getItemAtPosition(position);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });


        //////////////////////////////////// AGE SPINNER ////////////////////////////////////////////////////////////

        ageSpinner = (Spinner) findViewById(R.id.age_spinner);

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> ageSpinnerArrayAdapter = new ArrayAdapter<String>(
                ProfileEditActivity.this, R.layout.spinner_dialog_item,ageList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint

                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        ageSpinnerArrayAdapter.setDropDownViewResource(spinner_item);
        ageSpinner.setAdapter(ageSpinnerArrayAdapter);

        ////GET SELECTED AGE FROM SPINNER
        ageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    selectedAGE = (String) parent.getItemAtPosition(position);
                    resetBelowSpinners();
                    checkIfAgeSelected();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

    }

    private void resetBelowSpinners() {

        checkIfBelowAge();

    }

    private void checkIfBelowAge() {
        if (selectedAGE != null) {

            if ("Below - 13yrs".equals(selectedAGE)) {
                //RESET BELOW SPINNERS
                initCareerSpinner();
                initStudentSpinner();
                careerSpinner.setVisibility(View.GONE);
                studentSpinner.setVisibility(View.GONE);

            } else if ("Between (14 - 17)yrs".equals(selectedAGE)) {
                initCareerSpinner();
                initStudentSpinner();
                careerSpinner.setVisibility(View.GONE);
                studentSpinner.setVisibility(View.GONE);
            }

        }
    }

    private void checkIfAgeSelected() {

        if (selectedAGE != null) {

            if ("Between (18 - 26)yrs".equals(selectedAGE)) {
                initCareerSpinner();
                careerSpinner.setVisibility(View.VISIBLE);

            } else if ("Between (27 - 34)yrs".equals(selectedAGE)) {
                initCareerSpinner();

            } if ("Above - 35yrs".equals(selectedAGE)) {
                initCareerSpinner();
            }

        } else {
            careerSpinner = (Spinner) findViewById(R.id.career_spinner);
            careerSpinner.setVisibility(View.GONE);
        }
    }

    private void initCareerSpinner() {
        // Initializing a String Array
        String[] career = new String[]{
                "Career",
                "Student",
                "Employed",
                "Self-employed"
        };

        final List<String> careerList = new ArrayList<>(Arrays.asList(career));

        careerSpinner = (Spinner) findViewById(R.id.career_spinner);
        careerSpinner.setVisibility(View.VISIBLE);

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> careerSpinnerArrayAdapter = new ArrayAdapter<String>(
                ProfileEditActivity.this, R.layout.spinner_dialog_item,careerList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint

                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        careerSpinnerArrayAdapter.setDropDownViewResource(spinner_item);
        careerSpinner.setAdapter(careerSpinnerArrayAdapter);

        ////GET SELECTED CAREER FROM SPINNER
        careerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    selectedCAREER = (String) parent.getItemAtPosition(position);
                    checkIfCareerSelected();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

    }

    private void checkIfCareerSelected() {

        if (selectedCAREER != null) {

                initStudentSpinner();
                studentSpinner.setVisibility(View.VISIBLE);

        } else {
            studentSpinner = (Spinner) findViewById(R.id.student_spinner);
            studentSpinner.setVisibility(View.GONE);
        }
    }

    private void initStudentSpinner() {

        // Initializing a String Array
        String[] coarse = new String[]{
                "Interest",
                "Math",
                "Agriculture",
                "Computer science & ICT",
                "Business & Economics",
                "Law",
                "Languages",
                "Geography & Geology",
                "History & Government",
                "Physics & Electronics",
                "Chemistry & Chemical science",
                "Medical & Health Science",
                "OTHERS"
        };

        final List<String> coarseList = new ArrayList<>(Arrays.asList(coarse));

        studentSpinner = (Spinner) findViewById(R.id.student_spinner);
        studentSpinner.setVisibility(View.VISIBLE);

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> coarseSpinnerArrayAdapter = new ArrayAdapter<String>(
                ProfileEditActivity.this, R.layout.spinner_dialog_item,coarseList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint

                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        coarseSpinnerArrayAdapter.setDropDownViewResource(spinner_item);
        studentSpinner.setAdapter(coarseSpinnerArrayAdapter);

        ////GET SELECTED CAREER FROM SPINNER
        studentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    selectedCOARSE = (String) parent.getItemAtPosition(position);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

    }
*/
    private void initSave() {

       /* if (selectedGENDER == null) {
            Toast.makeText(ProfileEditActivity.this, "Please Select your gender", Toast.LENGTH_LONG).show();
        } else if (selectedAGE == null) {
            Toast.makeText(ProfileEditActivity.this, "Please Select your age", Toast.LENGTH_LONG).show();
        } else if ("Between (18 - 26)yrs".equals(selectedAGE) || "Between (27 - 34)yrs".equals(selectedAGE) || "Above - 35yrs".equals(selectedAGE)) {
                if (selectedCAREER == null){
                    Toast.makeText(ProfileEditActivity.this, "Please Select your career", Toast.LENGTH_LONG).show();
                } else if (selectedCOARSE == null) {
                    Toast.makeText(ProfileEditActivity.this, "Please Select your interest", Toast.LENGTH_LONG).show();
                } else {
                    completeSave();
                }

        } else {*/
            completeSave();
      //  }


    }

    private void completeSave() {
        mprogress.setMessage("Saving, please wait...");
        mprogress.show();

        //final String name = inputName.getText().toString();
        final String bio = inputBio.getText().toString();
        final String name = inputName.getText().toString();
        if (name.equals("")) {
            Toast.makeText(ProfileEditActivity.this, "Enter name!", Toast.LENGTH_LONG).show();
        }
        else if (resultUri != null && resultUri.getLastPathSegment() != null) {
            StorageReference filepath = mStorage.child("Profile_images").child(resultUri.getLastPathSegment());

            filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("user_image").setValue(downloadUrl.toString());

                    mprogress.dismiss();

                    Toast.makeText(ProfileEditActivity.this, "Saved successfully!", Toast.LENGTH_LONG).show();
                }
            });

            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("name").setValue(name);
            /*mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("age").setValue(selectedAGE);
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("gender").setValue(selectedGENDER);
            if (selectedCAREER != null)
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("career").setValue(selectedCAREER);
            if (selectedCOARSE != null)
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("interest").setValue(selectedCOARSE);*/
            if (bio != null)
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("bio").setValue(bio);

        } else if (bio != null){

            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("name").setValue(name);
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("bio").setValue(bio);
           /* mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("name").setValue(name);
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("age").setValue(selectedAGE);
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("gender").setValue(selectedGENDER);
            if (selectedCAREER != null)
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("career").setValue(selectedCAREER);
            if (selectedCOARSE != null)
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("interest").setValue(selectedCOARSE);
            //   mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("user_image").setValue(downloadUrl.toString());*/

            mprogress.dismiss();

            Toast.makeText(ProfileEditActivity.this, "Saved successfully!", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1, 1)
                    .start(ProfileEditActivity.this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                userImg.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
