package com.celosoft.fastchat;


import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mChatsList;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mChatsDatabase;
    private DatabaseReference mSeenDatabase;
    private FirebaseAuth mAuth;

    private View mMainView;

    private String current_user_id;

    private RelativeLayout chats_rl_null;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mChatsList = (RecyclerView)mMainView.findViewById(R.id.chats_list);
        mChatsList.setHasFixedSize(true);
        mChatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        //caso id seja nulo, fazer método que mostre mensagem de reiniciar na tela

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mSeenDatabase = FirebaseDatabase.getInstance().getReference().child("Chat");
        mChatsDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(current_user_id);
        //Salvando off
        mChatsDatabase.keepSynced(true);

        chats_rl_null = (RelativeLayout)mMainView.findViewById(R.id.chats_rl_null);

        verificaRegistros();

        return mMainView;
    }

    private void verificaRegistros(){

        mChatsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( !dataSnapshot.exists() ){
                    mChatsList.setVisibility(View.GONE);
                    chats_rl_null.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Messages, ChatsViewHolder> chatsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Messages, ChatsViewHolder>(
                Messages.class,
                R.layout.chat_single_layout,
                ChatsViewHolder.class,
                mChatsDatabase

        ) {
            @Override
            protected void populateViewHolder(final ChatsViewHolder viewHolder, Messages model, int position) {

                final String idFriend = getRef(position).getKey();

                mUserDatabase.child(idFriend).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //Pegar dados do amigo
                        viewHolder.chatName.setText((String)dataSnapshot.child("name").getValue());
                        Picasso.with(getContext()).load((String)dataSnapshot.child("thumb_image").getValue()).placeholder(R.drawable.default__user).into(viewHolder.chatImageView);

                        if(dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }

                        viewHolder.chatImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                profileIntent.putExtra("user_id", idFriend);
                                startActivity(profileIntent);
                            }
                        });

                        final String friendName = (String)dataSnapshot.child("name").getValue();

                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", idFriend);
                                chatIntent.putExtra("user_name", friendName);
                                startActivity(chatIntent);
                            }
                        });

                    }



                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //carrega Visto

                //carrega ultima msg
                final DatabaseReference mLastMessage = mChatsDatabase.child(idFriend);
                Query query = mLastMessage.limitToLast(1);
                query.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Messages message = dataSnapshot.getValue(Messages.class);

                        String msg = message.getMessage();

                        if ( msg.length() > 25 ){
                            viewHolder.chat3dots.setVisibility(View.VISIBLE);
                        }

                        viewHolder.chatLastMessage.setText( msg );

                        //Verifica se a ultima msg é do user Logado para colocar o icone
                        if( message.getFrom().equals(current_user_id) ) {

                            viewHolder.chatSeenIcon.setVisibility(View.VISIBLE);

                            //Verifico na referencia Chat/idUser/idAmigo/seen
                            mSeenDatabase.child(current_user_id).child(idFriend).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if( dataSnapshot.exists() ) {
                                        boolean seen = (boolean) dataSnapshot.child("seen").getValue();

                                        if (seen == true) {
                                            viewHolder.chatSeenIcon.setImageResource(R.drawable.ic_seen_true2);
                                        } else if (seen == false) {
                                            viewHolder.chatSeenIcon.setImageResource(R.drawable.ic_seen_false2);
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }else{
                            viewHolder.chatSeenIcon.setVisibility(View.INVISIBLE);
                        }

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
        };

        mChatsList.setAdapter(chatsRecyclerViewAdapter);

    }

    private static class ChatsViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView chatImageView;
        private TextView chatName, chatLastMessage, chat3dots;
        private ImageView chatSeenIcon;

        public ChatsViewHolder(View itemView) {
            super(itemView);

            chatImageView = (CircleImageView)itemView.findViewById(R.id.chat_image);
            chatSeenIcon = (ImageView)itemView.findViewById(R.id.chat_seen_icon);
            chatName = (TextView)itemView.findViewById(R.id.chat_single_name);
            chatLastMessage = (TextView)itemView.findViewById(R.id.chat_last_message);
            chat3dots = (TextView)itemView.findViewById(R.id.chat_3dots);

        }

        public void setUserOnline(String online_status){

            ImageView userOnlineView = (ImageView)itemView.findViewById(R.id.chat_single_online_icon);

            if( online_status.equals("true")){//Ja verifica se esta true com esse metodo
                userOnlineView.setVisibility(View.VISIBLE);
            }else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }

    }

}
