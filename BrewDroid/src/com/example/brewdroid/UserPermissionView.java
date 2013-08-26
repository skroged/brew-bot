package com.example.brewdroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brew.lib.model.CHANNEL_PERMISSION;
import com.brew.lib.model.SOCKET_CHANNEL;

public class UserPermissionView extends RelativeLayout {

	private UserPermissionViewListener userPermissionViewListener;

	public UserPermissionView(Context context, final SOCKET_CHANNEL channel,
			CHANNEL_PERMISSION permission,
			UserPermissionViewListener userPermissionViewListener) {
		super(context);

		this.userPermissionViewListener = userPermissionViewListener;

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.widget_user_permission, this);

		TextView permissionLabelText = (TextView) findViewById(R.id.permissionLabelText);
		TextView permissionValueText = (TextView) findViewById(R.id.permissionValueText);

		permissionLabelText.setText(channel.toString());
		permissionValueText.setText(permission.toString());

		findViewById(R.id.editButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				final CHANNEL_PERMISSION[] perms = CHANNEL_PERMISSION.values();
				String[] values = new String[perms.length];

				for (int i = 0; i < perms.length; i++) {
					values[i] = perms[i].toString();
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(
						getContext());
				builder.setTitle("Set permission").setItems(values,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

								UserPermissionView.this.userPermissionViewListener
										.onEdit(UserPermissionView.this,
												channel, perms[which]);

							}
						});

				builder.create().show();

			}

		});

	}

	public static interface UserPermissionViewListener {
		public void onEdit(UserPermissionView sender, SOCKET_CHANNEL channel,
				CHANNEL_PERMISSION newPermission);
	}

}
