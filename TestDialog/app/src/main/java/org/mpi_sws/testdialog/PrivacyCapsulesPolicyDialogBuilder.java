package org.mpi_sws.testdialog;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by herbster on 15/10/15.
 */
public class PrivacyCapsulesPolicyDialogBuilder {

    private static PrivacyCapsulesPolicyDialogBuilder singleton = new PrivacyCapsulesPolicyDialogBuilder();

    private List<String> groupList;
    private List<String> childList;
    private Map<String, List<String>> policiesMapping;

    private PrivacyCapsulesPolicyDialogBuilder() {

    }

    public static synchronized PrivacyCapsulesPolicyDialogBuilder getInstance() {
        return singleton;
    }

    public Dialog buildPolicyDialog(Activity activity) {
        final Dialog policiesDialog = new Dialog(activity);

        loadPolicyStaticFiles();

        policiesDialog.setContentView(R.layout.privacy_capsules_policies_dialog);
        policiesDialog.setTitle("Privacy Capsules Policies");

        Button cancelButton = (Button) policiesDialog.findViewById(R.id.button_cancel);
        // if button is clicked, close the custom dialog
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                policiesDialog.dismiss();
            }
        });

        Button saveButton = (Button) policiesDialog.findViewById(R.id.button_save);
        // if button is clicked, close the custom dialog
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                policiesDialog.dismiss();
            }
        });

        ExpandableListView expListView = (ExpandableListView) policiesDialog.findViewById(R.id.list_view);
        final ExpandableListAdapter expListAdapter = new PrivacyCapsulesExpandableListAdapter(activity, groupList, policiesMapping);
        expListView.setAdapter(expListAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                final String selected = (String) expListAdapter.getChild(
                        groupPosition, childPosition);
                return true;
            }
        });

        return policiesDialog;
    }

    private void loadPolicyStaticFiles() {
        createGroupList();
        createChildGroupList();
    }

    private void createGroupList() {
        groupList = new ArrayList<String>();
        groupList.add("HP");
        groupList.add("Dell");
        groupList.add("Lenovo");
        groupList.add("Sony");
        groupList.add("HCL");
        groupList.add("Samsung");
    }

    private void createChildGroupList() {
        String[] hpModels = { "HP Pavilion G6-2014TX", "ProBook HP 4540",
                "HP Envy 4-1025TX" };
        String[] hclModels = { "HCL S2101", "HCL L2102", "HCL V2002" };
        String[] lenovoModels = { "IdeaPad Z Series", "Essential G Series",
                "ThinkPad X Series", "Ideapad Z Series" };
        String[] sonyModels = { "VAIO E Series", "VAIO Z Series",
                "VAIO S Series", "VAIO YB Series" };
        String[] dellModels = { "Inspiron", "Vostro", "XPS" };
        String[] samsungModels = { "NP Series", "Series 5", "SF Series" };

        policiesMapping = new LinkedHashMap<String, List<String>>();

        for (String laptop : groupList) {
            if (laptop.equals("HP")) {
                loadChild(hpModels);
            } else if (laptop.equals("Dell"))
                loadChild(dellModels);
            else if (laptop.equals("Sony"))
                loadChild(sonyModels);
            else if (laptop.equals("HCL"))
                loadChild(hclModels);
            else if (laptop.equals("Samsung"))
                loadChild(samsungModels);
            else
                loadChild(lenovoModels);

            policiesMapping.put(laptop, childList);
        }
    }

    private void loadChild(String[] laptopModels) {
        childList = new LinkedList<String>();
        for (String model : laptopModels)
            childList.add(model);

    }

}
