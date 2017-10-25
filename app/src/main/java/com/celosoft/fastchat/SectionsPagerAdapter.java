package com.celosoft.fastchat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Marcelo on 12/06/2017.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter{


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            case 2:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            default:
                return null;
        }

    }

    public CharSequence getPageTitle(int position){

        switch (position){
            case 0:
                return "Conversas";
            case 1:
                return "Amigos";
            case 2:
                return "Pedidos";
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }
}
