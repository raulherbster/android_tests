package org.mpi_sws.testdialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by herbster on 15/10/15.
 */
public class PrivacyCapsulesExpandableListAdapter  extends BaseExpandableListAdapter {

    private Activity context;
    private Map<String, List<String>> policiesValueMapping;
    private List<String> policies;
    //private Map<String, List<CheckBox>> checkBoxes;

    public PrivacyCapsulesExpandableListAdapter(Activity context, List<String> policies,
                                                Map<String, List<String>> policiesValueMapping) {
        this.context = context;
        this.policiesValueMapping = policiesValueMapping;
        this.policies = policies;
        //checkBoxes = new LinkedHashMap<String, List<CheckBox>>();
    }

    public Object getChild(int groupPosition, int childPosition) {
        return policiesValueMapping.get(policies.get(groupPosition)).get(childPosition);
    }

    private boolean belongsToGroup(int groupPosition, String name) {
        List<String> collection = policiesValueMapping.get(policies.get(groupPosition));
        return collection.contains(name);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String laptop = (String) getChild(groupPosition, childPosition);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.pc_policy_child_item, null);
        }

        final EditText item = (EditText) convertView.findViewById(R.id.privacy_capsules_policy_textfield);

//        List<CheckBox> checkBoxList = checkBoxes.get(policies.get(groupPosition));
//
//        if (checkBoxList == null) {
//            checkBoxes.put(policies.get(groupPosition),new ArrayList<CheckBox>());
//        }
//
//        checkBoxes.get(policies.get(groupPosition)).add(item);

        item.setText(laptop);

//        item.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                item.setChecked(b);
//                List<CheckBox> checkBoxGroup = checkBoxes.get(policies.get(groupPosition));
//                if (b) {
//                    for (CheckBox checkBox : checkBoxGroup) {
//                        if (checkBox != item) {
//                            checkBox.setChecked(false);
//                        }
//                    }
//                }
//            }
//        });

        return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        return policiesValueMapping.get(policies.get(groupPosition)).size();
    }

    public Object getGroup(int groupPosition) {
        return policies.get(groupPosition);
    }

    public int getGroupCount() {
        return policies.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String laptopName = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.pc_policy_group_item,
                    null);
        }
        TextView item = (TextView) convertView.findViewById(R.id.privacy_capsules_policy_name);
        item.setTypeface(null, Typeface.BOLD);
        item.setText(laptopName);
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
