package com.celosoft.fastchat;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class RequestsFragment extends Fragment {

    private RecyclerView mRequestList;
    private DatabaseReference mRequestsDatabase;
    private DatabaseReference mProfileDatabase;
    private FirebaseAuth mAuth;

    private View mMainView;

    private String current_user_id;

    private RelativeLayout requests_rl_null;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mRequestList = (RecyclerView) mMainView.findViewById(R.id.friends_request);
        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        mRequestsDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(current_user_id);
        mProfileDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        requests_rl_null = (RelativeLayout) mMainView.findViewById(R.id.requests_rl_null);

        verificaPedidos();

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, RequestsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, RequestsViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                RequestsViewHolder.class,
                mRequestsDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestsViewHolder viewHolder, Users model, int position) {

                final String idRequest = getRef(position).getKey();

                if (model.getReq_type().equals("received")) {

                    mProfileDatabase.child(idRequest).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            viewHolder.userNameView.setText(dataSnapshot.child("name").getValue().toString());
                            viewHolder.userStatusView.setText(dataSnapshot.child("status").getValue().toString());
                            Picasso.with(getContext()).load(dataSnapshot.child("thumb_image").getValue().toString()).placeholder(R.drawable.default__user).into(viewHolder.userImageView);

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                    profileIntent.putExtra("user_id", idRequest);
                                    startActivity(profileIntent);
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    viewHolder.mView.setVisibility(View.GONE);
                }

            }
        };

        mRequestList.setAdapter(firebaseRecyclerAdapter);

    }

    private void verificaPedidos() {

        mRequestsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {

                    mRequestList.setVisibility(View.GONE);
                    requests_rl_null.setVisibility(View.VISIBLE);
                }
//                 else if( dataSnapshot.exists() ){
//                    if (dataSnapshot.child("req_type").getValue().toString().equals("sent")) {
//                        mRequestList.setVisibility(View.GONE);
//                        requests_rl_null.setVisibility(View.VISIBLE);
//                    }
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private static class RequestsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView userStatusView, userNameView;
        CircleImageView userImageView;

        public RequestsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            userStatusView = (TextView) itemView.findViewById(R.id.user_single_status);
            userNameView = (TextView) itemView.findViewById(R.id.chat_single_name);
            userImageView = (CircleImageView) itemView.findViewById(R.id.chat_image);

        }

    }
}
