package com.example.brewdroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SensorCalibrationEditDialog extends AlertDialog {

	private float input;
	private float output;
	private EditText inputText;
	private EditText outputText;
	private SensorCalibrationEditDialogListener sensorCalibrationEditDialogListener;

	public SensorCalibrationEditDialog(
			Context context,
			float input,
			float output,
			SensorCalibrationEditDialogListener sensorCalibrationEditDialogListener) {
		super(context);

		this.input = input;
		this.output = output;
		this.sensorCalibrationEditDialogListener = sensorCalibrationEditDialogListener;

		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.dialog_calibration_edit, null);
		setView(v);

		inputText = (EditText) v.findViewById(R.id.inputText);
		outputText = (EditText) v.findViewById(R.id.outputText);

		inputText.setText(input + "");
		outputText.setText(output + "");

		setButton(BUTTON_POSITIVE, "OK", clickListener);
		setButton(BUTTON_NEGATIVE, "Cancel", clickListener);

		v.findViewById(R.id.captureButton).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						float input = SensorCalibrationEditDialog.this.sensorCalibrationEditDialogListener
								.requestCapture();
						
						inputText.setText(input + "");

					}
				});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// LayoutInflater inflater = (LayoutInflater)
		// getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// View v = inflater.inflate(R.layout.dialog_calibration_edit, null);
		// setView(v);
		// setContentView(R.layout.dialog_calibration_edit);

		// inputText = (EditText) v.findViewById(R.id.inputText);
		// outputText = (EditText) v.findViewById(R.id.outputText);
		//
		// inputText.setText(input + "");
		// outputText.setText(output + "");
		//
		// setButton(BUTTON_POSITIVE, "OK", clickListener);
		// setButton(BUTTON_NEGATIVE, "Cancel", clickListener);

	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int which) {

			switch (which) {

			case BUTTON_POSITIVE:

				float input;
				float output;
				try {
					input = Float.parseFloat(inputText.getText().toString());
					if (input == Float.NaN || input == Float.NEGATIVE_INFINITY
							|| input == Float.POSITIVE_INFINITY)
						throw new NumberFormatException();
				} catch (NumberFormatException e) {
					Toast.makeText(getContext(), "Invalid format for input",
							Toast.LENGTH_SHORT).show();
					return;
				}

				try {
					output = Float.parseFloat(outputText.getText().toString());
					if (output == Float.NaN
							|| output == Float.NEGATIVE_INFINITY
							|| output == Float.POSITIVE_INFINITY)
						throw new NumberFormatException();
				} catch (NumberFormatException e) {
					Toast.makeText(getContext(), "Invalid format for output",
							Toast.LENGTH_SHORT).show();
					return;
				}

				sensorCalibrationEditDialogListener.onSaved(input, output);
				break;

			case BUTTON_NEGATIVE:
				break;

			}
		}

	};

	public static interface SensorCalibrationEditDialogListener {
		public void onSaved(float input, float output);

		public float requestCapture();
	}
}
