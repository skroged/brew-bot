package com.example.brewdroid;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.brew.lib.model.User;

public class UsersPermissionAdapter extends ArrayAdapter<User> {

	public UsersPermissionAdapter(Context context, List<User> users) {
		super(context, 0, users);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		
		UserPermissionGroupView view = (UserPermissionGroupView) convertView;
		User user = getItem(position);
		
		if(view == null){
			view = new UserPermissionGroupView(getContext());
		}
		
		view.setUser(user);
		
		return view;
		
	}
	
	
	

}
