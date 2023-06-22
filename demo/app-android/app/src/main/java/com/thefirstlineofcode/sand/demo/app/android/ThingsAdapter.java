package com.thefirstlineofcode.sand.demo.app.android;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList;
import com.thefirstlineofcode.sand.demo.protocols.AuthorizedThing;
import com.thefirstlineofcode.sand.demo.protocols.LanNode;
import com.thefirstlineofcode.sand.protocols.thing.ThingIdentity;

public class ThingsAdapter extends BaseExpandableListAdapter {
	private final MainActivity mainActivity;
	private String host;
	private AuthorizedThing[] things;
	
	public ThingsAdapter(MainActivity mainActivity, String host, AuthorizedThing[] things) {
		this.mainActivity = mainActivity;
		this.host = host;
		this.things = things;
	}
	
	public void setThings(AuthorizedThing[] things) {
		this.things = things;
	}
	public AuthorizedThing[] getThings() {
		return things;
	}
	
	@Override
	public int getGroupCount() {
		if (things == null)
			return 0;
		
		return things.length;
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
		AuthorizedThing thing = things[groupPosition];
		if (!thing.isConcentrator())
			return 0;
		
		return thing.getNodes().size();
	}
	
	@Override
	public Object getGroup(int groupPosition) {
		return things[groupPosition];
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		AuthorizedThing thing = things[groupPosition];
		return thing.getNodes().get(childPosition);
	}
	
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		AuthorizedThing thing = things[groupPosition];
		
		AuthorizedThingViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mainActivity).inflate(R.layout.authorized_thing_view, parent, false);
			
			viewHolder = new AuthorizedThingViewHolder();
			viewHolder.tvThingId = convertView.findViewById(R.id.tv_thing_id);
			viewHolder.tvUserRole = convertView.findViewById(R.id.tv_user_role);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (AuthorizedThingViewHolder)convertView.getTag();
		}
		
		viewHolder.tvThingId.setText(thing.getThingId());
		viewHolder.tvUserRole.setText(thing.getRole().toString());
		
		ControlSpinner spnControlActions = convertView.findViewById(R.id.spn_control_actions);
		String[] sActions = getAuthorizedThingActions(thing.getModel());
		ArrayAdapter<String> actionsAdapter = new ArrayAdapter<>(mainActivity,
				android.R.layout.simple_spinner_dropdown_item, sActions);
		spnControlActions.setAdapter(actionsAdapter);
		
		if (thing.getRole() != AccessControlList.Role.OWNER &&
				thing.getRole() != AccessControlList.Role.CONTROLLER) {
			TextView tvControl = convertView.findViewById(R.id.tv_control);
			tvControl.setVisibility(View.INVISIBLE);
			spnControlActions.setVisibility(View.INVISIBLE);
		} else {
			spnControlActions.setOnItemSelectedListener(new ControlActionsListener(spnControlActions, thing.getThingId()));
		}
		
		return convertView;
	}
	
	private static class AuthorizedThingViewHolder {
		private TextView tvThingId;
		private TextView tvUserRole;
	}
	
	private static class LanNodeViewHolder {
		private TextView tvThingId;
		private TextView tvLanId;
	}
	
	@NonNull
	private String[] getAuthorizedThingActions(String model) {
		if (model.startsWith("LGE-")) {
			return new String[] {"Change Working Mode"};
		} else if (model.equals("LGSC-01")) {
			return new String[] {
					"Change Working Mode",
					"Reset DAC Service",
					"Sync Nodes",
					"Pull LAN Follows",
					"Take a Photo",
					"Take a Video",
					"Show Recorded Videos",
					"Open Live Streaming",
					"Stop",
					"Shutdown System"
			};
		} else if (model.startsWith("LG-")) {
			return new String[] {
					"Change Working Mode",
					"Reset DAC Service"
			};
		} else if (model.startsWith("SL-")) {
			return new String[] {"Flash", "Turn On", "Turn Off"};
		} else {
			throw new RuntimeException(String.format("Unknown thing model: %s.", model));
		}
	}
	
	@NonNull
	private String[] getLanNodeActions(String model) {
		if (model.startsWith("SLE-") || model.startsWith("SL-")) {
			return new String[] {"Flash", "Turn On", "Turn Off", "Follow It", "Reset Thing", "Remove Node"};
		} else if (model.startsWith("STR-")) {
			return new String[] {"Watch Temperature", "Reset Thing", "Remove Node"};
		} else {
			throw new RuntimeException(String.format("Unknown thing model: %s.", model));
		}
	}
	
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		AuthorizedThing thing = things[groupPosition];
		LanNode lanNode = thing.getNodes().get(childPosition);
		
		LanNodeViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mainActivity).inflate(R.layout.lan_node_view, parent, false);
			
			viewHolder = new LanNodeViewHolder();
			viewHolder.tvThingId = convertView.findViewById(R.id.tv_thing_id);
			viewHolder.tvLanId = convertView.findViewById(R.id.tv_lan_id);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (LanNodeViewHolder)convertView.getTag();
		}
		
		viewHolder.tvThingId.setText(lanNode.getThingId());
		viewHolder.tvLanId.setText(String.valueOf(lanNode.getLanId()));
		
		ControlSpinner spnControlActions = convertView.findViewById(R.id.spn_control_actions);
		String[] sActions = getLanNodeActions(lanNode.getModel());
		ArrayAdapter<String> actionsAdapter = new ArrayAdapter<>(mainActivity,
				android.R.layout.simple_spinner_dropdown_item, sActions);
		spnControlActions.setAdapter(actionsAdapter);
		
		if (thing.getRole() != AccessControlList.Role.OWNER &&
				thing.getRole() != AccessControlList.Role.CONTROLLER) {
			TextView tvControl = convertView.findViewById(R.id.tv_control);
			tvControl.setVisibility(View.INVISIBLE);
			spnControlActions.setVisibility(View.INVISIBLE);
		} else {
			spnControlActions.setOnItemSelectedListener(new ThingsAdapter.ControlActionsListener(spnControlActions, lanNode.getThingId()));
		}
		
		return convertView;
	}
	
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	
	public static class ControlSpinner extends androidx.appcompat.widget.AppCompatSpinner {
		private int lastSelection = -1;
		
		public ControlSpinner(Context context) {
			super(context);
		}
		
		public ControlSpinner(Context context, int mode) {
			super(context, mode);
		}
		
		public ControlSpinner(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
		
		public ControlSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
			super(context, attrs, defStyleAttr);
		}
		
		public ControlSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
			super(context, attrs, defStyleAttr, mode);
		}
		
		public ControlSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme popupTheme) {
			super(context, attrs, defStyleAttr, mode, popupTheme);
		}
		
		@Override
		public void setSelection(int position) {
			super.setSelection(position);
			
			if (lastSelection != -1 && position == lastSelection) {
				getOnItemSelectedListener().onItemSelected(this, null, position, 0);
			}
			lastSelection = position;
		}
		
		@Override
		public void setSelection(int position, boolean animate) {
			super.setSelection(position, animate);
			
			if (lastSelection != -1 && position == lastSelection) {
				getOnItemSelectedListener().onItemSelected(this, null, position, 0);
			}
			lastSelection = position;
		}
	}
	
	private class ControlActionsListener implements AdapterView.OnItemSelectedListener {
		private ControlSpinner spinner;
		private final String thingId;
		private boolean initialState;
		private int lastSelection;
		
		public ControlActionsListener(ControlSpinner spinner, String thingId) {
			this.spinner = spinner;
			this.thingId = thingId;
			initialState = true;
			lastSelection = -1;
		}
		
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			if (initialState) {
				initialState = false;
				spinner.lastSelection = pos;
				return;
			}
			
			String selectedItem = parent.getItemAtPosition(pos).toString();
			switch (selectedItem) {
				case "Change Working Mode":
					mainActivity.changeWorkingMode(getJidTargetByThingId(thingId));
					break;
				case "Reset DAC Service":
					mainActivity.resetDacService(getJidTargetByThingId(thingId));
					break;
				case "Sync Nodes":
					mainActivity.syncNodes(getJidTargetByThingId(thingId));
					break;
				case "Pull LAN Follows":
					mainActivity.pullLanFollows(getJidTargetByThingId(thingId));
					break;
				case "Take a Photo":
					mainActivity.takeAPhoto(getJidTargetByThingId(thingId));
					break;
				case "Take a Video":
					mainActivity.takeAVideo(getJidTargetByThingId(thingId));
					break;
				case "Show Recorded Videos":
					mainActivity.showRecordedVideos(thingId);
					break;
				case "Open Live Streaming":
					mainActivity.openLiveSteaming(getJidTargetByThingId(thingId));
					break;
				case "Stop":
					mainActivity.stop(getJidTargetByThingId(thingId));
					break;
				case "Shutdown System":
					mainActivity.shutdownSystem(getJidTargetByThingId(thingId));
					break;
				case "Flash":
					mainActivity.flash(getJidTargetByThingId(thingId));
					break;
				case "Turn On":
					mainActivity.turnOn(getJidTargetByThingId(thingId));
					break;
				case "Turn Off":
					mainActivity.turnOff(getJidTargetByThingId(thingId));
					break;
				case "Follow It":
					mainActivity.followThing(getJidTargetByThingId(thingId));
					break;
				case "Watch Temperature":
					mainActivity.watchTemperature(getJidTargetByThingId(thingId));
					break;
				case "Reset Thing":
					mainActivity.resetThing(getJidTargetByThingId(thingId));
					break;
				case "Remove Node":
					mainActivity.removeNode(getJidTargetByThingId(thingId));
					break;
				default:
					throw new RuntimeException("Unknown command: " + selectedItem);
			}
		}
		
		@Override
		public void onNothingSelected(AdapterView<?> parent) {}
		
		private JabberId getJidTargetByThingId(String thingId) {
			for (AuthorizedThing thing : things) {
				if (thingId.equals(thing.getThingId()))
					return new JabberId(thing.getThingName(), host, ThingIdentity.DEFAULT_RESOURCE_NAME);
				
				if (thing.isConcentrator() && thing.getNodes() != null && thing.getNodes().size() > 0) {
					for (LanNode lanNode : thing.getNodes()) {
						if (thingId.equals(lanNode.getThingId()))
							return new JabberId(thing.getThingName(), host, String.valueOf(lanNode.getLanId()));
					}
				}
			}
			
			throw new RuntimeException(String.format("Can't find the thing which's thing ID is %s.", thingId));
		}
	}
}
