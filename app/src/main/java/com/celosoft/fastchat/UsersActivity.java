package com.celosoft.fastchat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;

    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = (Toolbar)findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Todos Usuários");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersList = (RecyclerView)findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, usersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, usersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                usersViewHolder.class,
                mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(usersViewHolder viewHolder, Users users, int position) {

                viewHolder.setUserProfile(users.getName(), users.getStatus());
                viewHolder.setUserImage(users.getThumb_image(), getApplicationContext());//Passando o Contexto aqui, pq o view holder é subclasse, e da erro se pegar contexto la dentro


                //Pegando Key do usuario clidado
                final String user_id = getRef(position).getKey();

                //setando click no viewHolder daqui, bastar chamar o mView, mas pega toda a posição
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);

                    }
                });
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class usersViewHolder extends RecyclerView.ViewHolder{ // static pq é classe interna

        View mView;

        public usersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUserProfile(String name, String status){
            TextView userNameView = (TextView)mView.findViewById(R.id.chat_single_name);
            TextView userStatusView = (TextView)mView.findViewById(R.id.user_single_status);

            userNameView.setText(name);
            userStatusView.setText(status);
        }

        public void setUserImage(String thumb_image, Context context){

            CircleImageView userImage = (CircleImageView)mView.findViewById(R.id.chat_image);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.default__user).into(userImage);

        }

    }

}
