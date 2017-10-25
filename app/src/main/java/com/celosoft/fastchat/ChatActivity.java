package com.celosoft.fastchat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private TextView mTitleView, mLastSeenView;
    private CircleImageView mProfileImage;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private ImageButton mChatAddBtn, mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;

    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapterMultiple mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private int itemPos = 0;
    private String mLastKey = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = (Toolbar)findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");
        //getSupportActionBar().setTitle(userName);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        //Itens barra
        mTitleView = (TextView)findViewById(R.id.custom_bar_title);
        mLastSeenView = (TextView)findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);

        mChatAddBtn = (ImageButton)findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageButton)findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText)findViewById(R.id.chat_message_view);

        mAdapter = new MessageAdapterMultiple(getApplicationContext(), messagesList, mChatUser);
        mMessagesList = (RecyclerView)findViewById(R.id.messages_list);
        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.message_swipe_layout);

        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        loadMessages();

        mTitleView.setText(userName);

        //Setando status e foto na barra de tarefas
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String thumb = dataSnapshot.child("thumb_image").getValue().toString();

                Users users = new Users();
                users.setThumb_image(thumb);
                Log.d("imagemamigochat", ""+users.getThumb_image());
                Picasso.with(ChatActivity.this).load(thumb).placeholder(R.drawable.default__user).into(mProfileImage);

                if(online.equals("true")){
                    mLastSeenView.setText("Online");
                }else{
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //enviar msg
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();

            }
        });

        //Colocando seen
        updateSeen();

    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                messagesList.add(itemPos++, message);

                if(itemPos == 1){
                    mLastKey = dataSnapshot.getKey();
                }

                mAdapter.notifyDataSetChanged();

               //mMessagesList.scrollToPosition(messagesList.size()-1); //Ir para baixo quando algm fala

                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        //Limitando para 10 as mensagens que aparecem

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos == 1){
                    mLastKey = dataSnapshot.getKey();
                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size()-1); //Ir para baixo quando algm fala

                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {

        String message = mChatMessageView.getText().toString();

        if( !TextUtils.isEmpty(message) ){

            //criando referencia como String
            String current_user_ref = "messages/"+mCurrentUserId+"/"+mChatUser;
            String chat_user_ref = "messages/"+mChatUser+"/"+mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();
            String push_id = user_message_push.getKey();

            final String diaHora = PegarDataAtual.diaHoraPost();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            //messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("time", diaHora);
            messageMap.put("from", mCurrentUserId);

            //update nas duas ao mesmo tempo
            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref+"/"+push_id, messageMap);
            messageUserMap.put(chat_user_ref+"/"+push_id, messageMap);

            mChatMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if( databaseError != null){
                        Log.d("ChatLog", databaseError.getMessage().toString());
                        //Colocar false no id de quem envia

                    }else{
                        mChatMessageView.setText("");
                        Map seenMap = new HashMap();
                        seenMap.put("seen", false);
                        seenMap.put("time", diaHora);

                        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).updateChildren(seenMap);
                    }
                }
            });

        }
    }

    private void updateSeen(){

        //Colocar seen true na referencia do amigo para ele visualizar no fragmento
        mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String diaHora = PegarDataAtual.diaHoraPost();

                Map seenMap = new HashMap();
                seenMap.put("seen", true);
                seenMap.put("time", diaHora);
                mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).updateChildren(seenMap);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
