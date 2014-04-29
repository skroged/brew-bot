package com.brew.brewdroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class SensorAddressEditDialog extends AlertDialog {

	private EditText addressText;
	private SensorAddressEditDialogListener sensorAddressEditDialogListener;

	public SensorAddressEditDialog(Context context, final String[] devices,
			String address,
			SensorAddressEditDialogListener sensorAddressEditDialogListener) {
		super(context);

		this.sensorAddressEditDialogListener = sensorAddressEditDialogListener;

		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.address_select_dialog, null);
		setView(v);

		addressText = (EditText) v.findViewById(R.id.addressText);

		addressText.setText(address);

		setButton(BUTTON_POSITIVE, "OK", clickListener);
		setButton(BUTTON_NEGATIVE, "Cancel", clickListener);

		v.findViewById(R.id.chooseButton).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						AlertDialog.Builder builder = new AlertDialog.Builder(
								getContext());
						builder.setTitle("Choose a device").setItems(devices,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										addressText.setText(devices[which]);
									}
								});
						
						builder.create().show();

					}

				});
	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int which) {

			switch (which) {

			case BUTTON_POSITIVE:

				String address = addressText.getText().toString();

				sensorAddressEditDialogListener.onSaved(address);
				break;

			case BUTTON_NEGATIVE:
				break;

			}
		}

	};

	public static interface SensorAddressEditDialogListener {
		public void onSaved(String address);
	}
}
