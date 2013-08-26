package com.example.brewdroid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brew.client.socket.SocketManager;
import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.CHANNEL_PERMISSION;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.User;
import com.brew.lib.model.UserChannelPermission;
import com.example.brewdroid.UserPermissionView.UserPermissionViewListener;

public class UserPermissionGroupView extends LinearLayout {

	private LinearLayout permissionHolder;
	private TextView nameText;
	private User user;

	public UserPermissionGroupView(Context context) {
		super(context);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.widget_user_permission_group, this);

		permissionHolder = (LinearLayout) findViewById(R.id.permissionHolder);
		nameText = (TextView) findViewById(R.id.nameText);

	}

	public void setUser(User user) {
		this.user = user;

		nameText.setText(user.getUsername());

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		permissionHolder.removeAllViews();
		for (SOCKET_CHANNEL sc : SOCKET_CHANNEL.values()) {
			CHANNEL_PERMISSION perm = user.getPermissionForChannel(sc);
			UserPermissionView view = new UserPermissionView(getContext(), sc,
					perm, userPermissionViewListener);
			permissionHolder.addView(view, lp);
		}

	}

	private UserPermissionViewListener userPermissionViewListener = new UserPermissionViewListener() {

		@Override
		public void onEdit(UserPermissionView sender, SOCKET_CHANNEL channel,
				CHANNEL_PERMISSION newPermission) {

			List<UserChannelPermission> perms = user.getPermissions();

			boolean exists = false;
			for (UserChannelPermission ucp : perms) {

				if (ucp.getChannel() == channel) {
					exists = true;
					ucp.setPermission(newPermission);
					break;
				}
			}

			if (!exists) {
				UserChannelPermission ucp = new UserChannelPermission();
				ucp.setPermission(newPermission);
				ucp.setChannel(channel);
				user.getPermissions().add(ucp);
			}

			BrewMessage message = new BrewMessage();
			message.setGuaranteeId(UUID.randomUUID().toString());
			message.setMethod(SOCKET_METHOD.UPDATE_USERS);

			BrewData data = new BrewData();
			message.setData(data);

			List<User> users = new ArrayList<User>();
			users.add(user);
			data.setUsers(users);

			SocketManager.sendMessage(message);

		}

	};

}
