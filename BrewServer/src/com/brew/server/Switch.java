package com.brew.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.brew.lib.model.SWITCH_NAME;

public class Switch {

	private boolean value;
	private SWITCH_NAME name;

	private static Map<SWITCH_NAME, Switch> switches;

	public static Map<SWITCH_NAME, Switch> getSwitches() {
		return switches;
	}

	public static void init() {

		switches = new Hashtable<SWITCH_NAME, Switch>();

		Switch hltPump = new Switch();
		hltPump.name = SWITCH_NAME.HLT_PUMP;
		switches.put(hltPump.name, hltPump);

		Switch hltBurner = new Switch();
		hltBurner.name = SWITCH_NAME.HLT_BURNER;
		switches.put(hltBurner.name, hltBurner);

		Switch hltHlt = new Switch();
		hltHlt.name = SWITCH_NAME.HLT_HLT;
		switches.put(hltHlt.name, hltHlt);

		Switch hltMlt = new Switch();
		hltMlt.name = SWITCH_NAME.HLT_MLT;
		switches.put(hltMlt.name, hltMlt);

		Switch mltPump = new Switch();
		mltPump.name = SWITCH_NAME.MLT_PUMP;
		switches.put(mltPump.name, mltPump);

		Switch mltBurner = new Switch();
		mltBurner.name = SWITCH_NAME.MLT_BURNER;
		switches.put(mltBurner.name, mltBurner);

		Switch mltMlt = new Switch();
		mltMlt.name = SWITCH_NAME.MLT_MLT;
		switches.put(mltMlt.name, mltMlt);

		Switch mltBk = new Switch();
		mltBk.name = SWITCH_NAME.MLT_BK;
		switches.put(mltBk.name, mltBk);

		Switch bkPump = new Switch();
		bkPump.name = SWITCH_NAME.BK_PUMP;
		switches.put(bkPump.name, bkPump);

		Switch bkBurner = new Switch();
		bkBurner.name = SWITCH_NAME.BK_BURNER;
		switches.put(bkBurner.name, bkBurner);

		Switch bkBk = new Switch();
		bkBk.name = SWITCH_NAME.BK_BK;
		switches.put(bkBk.name, bkBk);

		Switch bkFerm = new Switch();
		bkFerm.name = SWITCH_NAME.BK_FERM;
		switches.put(bkFerm.name, bkFerm);

		Switch igniter = new Switch();
		igniter.name = SWITCH_NAME.IGNITER;
		switches.put(igniter.name, igniter);
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {

		boolean changed = value ^ this.value;

		if (changed) {
			Logger.log("DATA", name + ": " + value);
		}

		this.value = value;

		if (changed) {
			notifyChanged();
		}
	}

	public SWITCH_NAME getName() {
		return name;
	}

	public void setName(SWITCH_NAME name) {
		this.name = name;
	}

	private void notifyChanged() {

		synchronized (switchListeners) {

			for (SwitchListener sl : switchListeners) {

				sl.onValueChanged(this);
			}

		}
	}

	public static void registerSwitchListener(SwitchListener switchListener) {

		synchronized (switchListeners) {

			switchListeners.add(switchListener);

		}
	}

	private static List<SwitchListener> switchListeners = Collections
			.synchronizedList(new ArrayList<SwitchListener>());

	public static interface SwitchListener {

		void onValueChanged(Switch sensor);

	}

}
