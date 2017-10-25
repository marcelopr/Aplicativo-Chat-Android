package com.celosoft.fastchat;

import android.app.ProgressDialog;
import android.icu.text.DateFormat;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private CircleImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn;
    private Button mProfileDeclineBtn;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    private String mCurrent_state;

    private Toolbar profile_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profile_bar = (Toolbar)findViewById(R.id.profile_bar);
        setSupportActionBar(profile_bar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("");

        //Pegando Id do usuário visitado!
        final String userId = getIntent().getStringExtra("user_id");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (CircleImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);
        mProfileDeclineBtn = (Button) findViewById(R.id.profile_decline_btn);

        mCurrent_state = "not_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Carregando perfil");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String display_status = dataSnapshot.child("status").getValue().toString();
                String display_image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(display_status);
                Picasso.with(ProfileActivity.this).load(display_image).placeholder(R.drawable.default__user).into(mProfileImage);

                //checando por convites

                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //verifica se o Usuario do perfil é amigo
                        if(dataSnapshot.hasChild(userId)){
                            String req_type = dataSnapshot.child(userId).child("req_type").getValue().toString();

                            if(req_type.equals("received")){

                                mCurrent_state = "req_received";//Mudando o status para PEDIDO RECEBIDO
                                mProfileSendReqBtn.setText("Aceitar amizade");

                                mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineBtn.setEnabled(true);

                            }else if(req_type.equals("sent")){
                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancelar pedido de amizade");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            }

                            mProgressDialog.dismiss();

                        }else{
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(userId)){
                                        //quer dizer que já é amigo
                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Desfazer amizade");

                                        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineBtn.setEnabled(false);
                                    }

                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // NAO SÃO AMIGOS

                mProfileSendReqBtn.setEnabled(false);

                //ENVIANDO PEDIDO DE AMIZADE

                if (mCurrent_state.equals("not_friends")) {//se não forem amigos

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(userId).push();
                    //pegando push Key
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<String, String>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    //IMPORTANTE AQUI: feito um mapa que coloca as referencias na database de uma vez, mais rápido!

                    Map requestMap = new HashMap<>();
                    requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + userId + "/req_type", "sent");
                    requestMap.put("Friend_req/" + userId + "/" + mCurrentUser.getUid() + "/req_type", "received");
                    requestMap.put("notifications/" +userId + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                           if(databaseError != null){

                               Toast.makeText(ProfileActivity.this, "Erro ao enviar pedido!", Toast.LENGTH_SHORT).show();

                           }

                            mProfileSendReqBtn.setEnabled(true);
                            mCurrent_state = "req_sent";
                            mProfileSendReqBtn.setText("Cancelar pedido de amizade");

                        }
                    });

                }

                //PEDIDO ENVIADO: CANCELANDO

                if(mCurrent_state.equals("req_sent")){
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mProfileSendReqBtn.setEnabled(true);
                            mCurrent_state = "not_friends";
                            mProfileSendReqBtn.setText("Enviar pedido de amizade");

                            mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                            mProfileDeclineBtn.setEnabled(false);

                        }
                    });
                }


                // PEDIDO DE AMIZADE RECEBIDO: ACEITANDO

                if( mCurrent_state.equals("req_received")){

                    final String currentDate = java.text.DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/"+mCurrentUser.getUid()+"/"+userId+"/date", currentDate);
                    friendsMap.put("Friends/"+userId+"/"+mCurrentUser.getUid()+"/date", currentDate);

//                    friendsMap.put("Friends_req/"+mCurrentUser.getUid()+"/"+userId+"/date", null);
//                    friendsMap.put("Friends_req/"+userId+"/"+mCurrentUser.getUid()+"/date", null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null ){
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendReqBtn.setText("Desfazer amizade");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            }else{
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    //Retirar dos pedidos os id's.

                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(userId).removeValue();
                    mFriendReqDatabase.child(userId).child(mCurrentUser.getUid()).removeValue();

                }

                //DESFAZER AMIZADE

                if(mCurrent_state.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/"+ mCurrentUser.getUid()+ "/" +userId, null);
                    unfriendMap.put("Friends/"+ userId+ "/" +mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){
                                mCurrent_state = "not_friends";
                                mProfileSendReqBtn.setText("Enviar pedido de amizade");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            }else{
                                Toast.makeText(ProfileActivity.this, "Erro ao enviar pedido!", Toast.LENGTH_SHORT).show();
                            }

                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });

                }

            }

        });

        //NEGANDO PEDIDO

        mProfileDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mFriendReqDatabase.child( mCurrentUser.getUid() ).child(userId).removeValue();

                mFriendReqDatabase.child( userId ).child( mCurrentUser.getUid() ).removeValue();

                finish();

            }
        });

    }
}
