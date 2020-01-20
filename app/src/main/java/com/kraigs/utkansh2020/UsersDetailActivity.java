package com.kraigs.utkansh2020;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class UsersDetailActivity extends AppCompatActivity {

    DatabaseReference rootRef;
    FirebaseAuth mAuth;
    String currentUserID;
    String gender;
    ProgressDialog loadingBar;
    private static Uri uri = null;

    @BindView(R.id.userPic)
    CircleImageView userPic;
    @BindView(R.id.userNameEt)
    EditText userNameEt;
    @BindView(R.id.branchEt)
    EditText branchEt;
    @BindView(R.id.collegeEt)
    EditText collegeET;
    @BindView(R.id.boySrc)
    ImageView boySrc;
    @BindView(R.id.boyRl)
    RelativeLayout boyRl;
    @BindView(R.id.girlSrc)
    ImageView girlSrc;
    @BindView(R.id.girlRl)
    RelativeLayout girlRl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_detail);
        ButterKnife.bind(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingBar = new ProgressDialog(this);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        boyRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gender = "Male";
                girlSrc.setColorFilter(getResources().getColor(R.color.grey));
                boySrc.setColorFilter(getResources().getColor(R.color.red));

            }
        });

        girlRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gender = "Female";
                girlSrc.setColorFilter(getResources().getColor(R.color.red));
                boySrc.setColorFilter(getResources().getColor(R.color.grey));

            }
        });

        userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, 32);
            }
        });

        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("name")) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        userNameEt.setText(name);
                    }
                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(userPic);
                    }
                    if (dataSnapshot.hasChild("branch")) {
                        String city = dataSnapshot.child("branch").getValue().toString();
                        branchEt.setText(city);
                    }
                    if (dataSnapshot.hasChild("college")) {
                        String vechile = dataSnapshot.child("college").getValue().toString();
                        collegeET.setText(vechile);
                    }

                    if (dataSnapshot.hasChild("gender")) {
                        String genderSt = dataSnapshot.child("gender").getValue().toString();
                        if (genderSt.equals("Male")) {
                            girlSrc.setColorFilter(getResources().getColor(R.color.grey));
                            boySrc.setColorFilter(getResources().getColor(R.color.red));
                            gender = genderSt;
                        } else if (genderSt.equals("Female")) {

                            gender = genderSt;

                            girlSrc.setColorFilter(getResources().getColor(R.color.red));
                            boySrc.setColorFilter(getResources().getColor(R.color.grey));

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 32 && resultCode == RESULT_OK && data != null) {

            uri = data.getData();
            Picasso.get().load(uri.toString()).into(userPic);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_save, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.action_save) {
            saveDetails();
        }

        return true;

    }

    private void saveDetails() {

        String name = userNameEt.getText().toString();
        String college = collegeET.getText().toString();
        String branch = branchEt.getText().toString();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(branch) && !TextUtils.isEmpty(gender) && !TextUtils.isEmpty(college)) {
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            if (uri != null) {

                final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("ProfilePic").child(currentUserID + ".jpg");
                filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("name", name);
                                    map.put("college", college);
                                    map.put("gender", gender);
                                    map.put("image", uri.toString());
                                    map.put("branch",branch);

                                    rootRef.child("Users").child(currentUserID)
                                            .updateChildren(map)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        loadingBar.dismiss();

                                                        Intent intent = new Intent(UsersDetailActivity.this, MainActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(intent);
                                                        finish();

                                                        Toast.makeText(UsersDetailActivity.this, "Your Details updated successfully!", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        String message = task.getException().toString();
                                                        Toast.makeText(UsersDetailActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                        } else {

                            String message = task.getException().toString();
                            Toast.makeText(UsersDetailActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                        }
                    }
                });

            } else {

                HashMap<String, Object> map = new HashMap<>();
                map.put("name", name);
                map.put("college",college);
                map.put("gender", gender);
                map.put("branch", branch);

                rootRef.child("Users").child(currentUserID)
                        .updateChildren(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    loadingBar.dismiss();
                                    Intent intent = new Intent(UsersDetailActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();

                                    Toast.makeText(UsersDetailActivity.this, "Your Details saved successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    String message = task.getException().toString();
                                    Toast.makeText(UsersDetailActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
            }
        } else {
            Toast.makeText(UsersDetailActivity.this, "Please fill all details!", Toast.LENGTH_SHORT).show();
        }
    }

}
