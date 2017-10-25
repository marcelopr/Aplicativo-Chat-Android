package com.celosoft.fastchat;


import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter;

    private String mCurrent_user_id;

    private View mMainView;

    private RelativeLayout friends_rl_null;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList = (RecyclerView)mMainView.findViewById(R.id.friends_list);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        friends_rl_null = (RelativeLayout)mMainView.findViewById(R.id.friends_rl_null);

        verificaAmigos();

        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, final Friends friends, int position) {

                //Pegando a key da Posição (id do usuario, para visitar perfil!).
                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String userStatus = dataSnapshot.child("status").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnline(userOnline);
                        }

                        friendsViewHolder.userStatusView.setText(userStatus);
                        friendsViewHolder.userNameView.setText(userName);
                        Picasso.with(getContext()).load(userThumb).placeholder(R.drawable.default__user).into(friendsViewHolder.userImageView);
                        //friendsViewHolder.setUserImage(userThumb, getContext());

                        friendsViewHolder.userImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                profileIntent.putExtra("user_id", list_user_id);
                                startActivity(profileIntent);
                            }
                        });

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mFriendsList.setAdapter(friendsRecyclerViewAdapter);

    }

    private void verificaAmigos(){

        mFriendsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( !dataSnapshot.exists() ){
                    mFriendsList.setVisibility(View.GONE);
                    friends_rl_null.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView userStatusView, userNameView;
        CircleImageView userImageView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            userStatusView = (TextView)itemView.findViewById(R.id.user_single_status);
            userNameView = (TextView)itemView.findViewById(R.id.chat_single_name);
            userImageView = (CircleImageView)itemView.findViewById(R.id.chat_image);

        }

//        public void setDate(String date){
//           // TextView userStatusView = (TextView)mView.findViewById(R.id.user_single_status);
//            userStatusView.setText(date);
//        }
//
//        public void setName(String name){
//           // TextView userNameView = (TextView)mView.findViewById(R.id.user_single_name);
//            userNameView.setText(name);
//        }
//
//        public void setUserImage(String thumb_image, Context ctx){
//
//           // CircleImageView userImageView = (CircleImageView)mView.findViewById(R.id.user_image);
//            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default__user).into(userImageView);
//        }

        public void setUserOnline(String online_status){
            ImageView userOnlineView = (ImageView)mView.findViewById(R.id.user_single_online_icon);
//
//            if( online_status.equals("true")){//Ja verifica se esta true com esse metodo
//                userOnlineView.setImageResource(R.drawable.img_icone_online);
//            }else{
//                userOnlineView.setImageResource(R.drawable.img_icone_offline);
//            }

            if( online_status.equals("true")){//Ja verifica se esta true com esse metodo
                userOnlineView.setVisibility(View.VISIBLE);
            }else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
