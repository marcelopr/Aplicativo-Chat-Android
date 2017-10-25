package com.celosoft.fastchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mImageStorage;

    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private Button mStatusBtn, mImageBtn;

    private static final int GALLERY_PICK = 1;

    private ProgressDialog mProgressDialog;

    private Toolbar settings_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings_bar = (Toolbar)findViewById(R.id.settings_bar);
        setSupportActionBar(settings_bar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("");

        mDisplayImage = (CircleImageView)findViewById(R.id.settings_image);
        mName = (TextView) findViewById(R.id.settings_name);
        mStatus = (TextView)findViewById(R.id.settings_status);
        mStatusBtn = (Button)findViewById(R.id.settings_status_btn);
        mImageBtn = (Button)findViewById(R.id.settings_image_btn);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        //OffLine Capability
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
                //Colocando imagem no ImageView

                if( !image.equals("default")) {// se for default ele deixa a image que esta no SRC do circle view, no layout
                    //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default__user).into(mDisplayImage);
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default__user).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default__user).into(mDisplayImage);

                        }
                    });

                    //IMPORTANTE: o placeholder é a imagem que é mostrada até que o carregamento da imagem do firebase estaja completo
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }
        );

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("status", mStatus.getText().toString());
                startActivity(statusIntent);

            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // start picker to get image for cropping and then use the image in cropping activity
//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(SettingsActivity.this);

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        mUserDatabase.child("online").setValue(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserDatabase.child("online").setValue(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();
            // start cropping activity for pre-acquired image saved on the device

            CropImage.activity(imageUri).setAspectRatio(1, 1).start(this);

            //Toast.makeText(SettingsActivity.this, imageUri, Toast.LENGTH_LONG).show();

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setMessage("Alterando foto...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                final File thumb_filePath = new File(resultUri.getPath());
                Log.d("TESTE-FILE", ""+thumb_filePath);
                String idUsuario = mCurrentUser.getUid();

                Bitmap thum_bitmap = new Compressor(this)
                        .setMaxHeight(200)
                        .setMaxWidth(200)
                        .setQuality(40)
                        .compressToBitmap(thumb_filePath);
                Log.d("TESTE-BITMAP", ""+thum_bitmap);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Log.d("TESTE-BAOS", ""+baos);
                thum_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte [] thumb_byte = baos.toByteArray();
                Log.d("TESTE-BYTE", ""+thumb_byte);

                StorageReference filepath = mImageStorage.child("profile_images").child(idUsuario + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(idUsuario + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful()){

                                        Map update_hashMap = new HashMap();
                                        update_hashMap.put("image", downloadUrl);
                                        update_hashMap.put("thumb_image", thumb_downloadUrl);

                                        //UPDATE aqui na referencia do user
                                        mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Foto atualizada!", Toast.LENGTH_LONG).show();
                                                }else{
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Não foi possível alterar sua foto!", Toast.LENGTH_LONG).show();
                                                }

                                            }
                                        });

                                    }else{
                                        Toast.makeText(SettingsActivity.this, "Erro ao upar imagem!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        }else{
                            mProgressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Erro ao trocar imagem!", Toast.LENGTH_LONG).show();
                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(15);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

}
