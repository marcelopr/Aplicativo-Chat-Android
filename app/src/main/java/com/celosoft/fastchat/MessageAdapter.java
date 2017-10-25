package com.celosoft.fastchat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Marcelo on 29/07/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //2º a executar
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout_friend, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder viewHolder, int i) {

        //3º a executar
        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();
        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();

        if ( from_user.equals(current_user_id) ){

            viewHolder.profileImage.setVisibility(View.INVISIBLE);
            viewHolder.messageText.setBackgroundResource(R.drawable.shape_message_user);


        }else{

            viewHolder.messageText.setBackgroundResource(R.drawable.shape_message_friend);

        }


        viewHolder.messageText.setText(c.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public CircleImageView profileImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView)itemView.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_layout);

        }

    }

}
