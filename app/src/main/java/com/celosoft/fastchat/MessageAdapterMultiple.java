package com.celosoft.fastchat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Marcelo on 29/07/2017.
 */

public class MessageAdapterMultiple extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private String mChatUser;
    private Context context;
    private Users users;

    public MessageAdapterMultiple(Context context, List<Messages> mMessageList, String mChatUser) {
        this.context = context;
        this.mMessageList = mMessageList;
        this.mChatUser = mChatUser;
        this.users = new Users();
    }


    @Override
    public int getItemViewType(int position) {

        Messages messages = mMessageList.get(position);
        String from = messages.getFrom();

        if (from.equals(mChatUser)) {
            return 1; //Friend layout chat
        } else if (!from.equals(mChatUser)) {
            return 2; //User layout chat
        }

        return super.getItemViewType(position);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case 1:
                //fazer if e colocar o 5 aqui
                View friendView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout_friend, parent, false);
                Log.d("OnCreateVH", "1");
                return new MessageFriendViewHolder(friendView);
            case 2:
                View userView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout_user, parent, false);
                Log.d("OnCreateVH", "2");
                return new MessageUserViewHolder(userView);

        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Messages c = mMessageList.get(position);

        switch (holder.getItemViewType()) {

            case 1:
                final MessageFriendViewHolder messageFriendViewHolder =  (MessageFriendViewHolder)holder;

                messageFriendViewHolder.messageTextFriend.setText(c.getMessage());
                messageFriendViewHolder.messageTimeFriend.setText(c.getTime());

//                DatabaseReference mThumbDataase = FirebaseDatabase.getInstance().getReference().child("Users").child(mChatUser);
//                mThumbDataase.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        String thumb = (String)dataSnapshot.child("thumb_image").getValue();
//                        Picasso.with(context).load(thumb).placeholder(R.drawable.default__user).into(messageFriendViewHolder.profileImageFriend);
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });

                break;

            case 2:

                MessageUserViewHolder messageUserViewHolder =  (MessageUserViewHolder) holder;

                messageUserViewHolder.messageTextUser.setText(c.getMessage());
                messageUserViewHolder.messageTimeUser.setText(c.getTime());

            break;

        }

    }


    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public class MessageFriendViewHolder extends RecyclerView.ViewHolder {

        private TextView messageTextFriend, messageTimeFriend;
      //  private CircleImageView profileImageFriend;

        public MessageFriendViewHolder(View itemView) {
            super(itemView);

            messageTextFriend = (TextView) itemView.findViewById(R.id.message_text_layout);
            messageTimeFriend = (TextView) itemView.findViewById(R.id.time_text_layout);
//            profileImageFriend = (CircleImageView) itemView.findViewById(R.id.message_profile_layout);

        }

    }

    public class MessageUserViewHolder extends RecyclerView.ViewHolder {

        private TextView messageTextUser, messageTimeUser;

        public MessageUserViewHolder(View itemView) {
            super(itemView);

            messageTextUser = (TextView) itemView.findViewById(R.id.message_text_layout_user);
            messageTimeUser = (TextView) itemView.findViewById(R.id.time_text_layout_user);

        }

    }

}
