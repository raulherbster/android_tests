package org.mpi_sws.testdialog;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by herbster on 29/12/15.
 */
public class SelectSecureChannelsDialog extends ListActivity implements DialogInterface.OnClickListener {

    private static final String TAG = "SelectSecureChannels";
    private static final String TAG_CONTACT_NAME = "NAME";
    private static final String TAG_CONTACT_ADDRESS = "ADDRESS";

    private Button mOKButton;
    private Button mCancelButton;

    private HashMap<String,String> mSelectedContacts;
    private List<HashMap<String, String>> mExistingContacts;

    private boolean mContactsLoaded;

    private ListAdapter mContactListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mExistingContacts = new ArrayList<HashMap<String, String>>();
        mSelectedContacts = new HashMap<String,String>();

        try {
            loadExistingContacts();

            mContactListAdapter = new SimpleAdapter(this, mExistingContacts,
                    R.layout.listitem_contact, new String[] { TAG_CONTACT_NAME, TAG_CONTACT_ADDRESS }, new int[] { R.id.contact_name, R.id.contact_address });

            setAdapter(mContactListAdapter);

        } catch (IOException e) {
            Log.d(TAG,"Cannot load existing contacts");
            e.printStackTrace();
        }
    }

    private void setAdapter(ListAdapter adapter) {
        ListView lview = getListView();

        lview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        lview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView vAddr = (TextView) view.findViewById(R.id.contact_address);
                TextView vName = (TextView) view.findViewById(R.id.contact_name);
                mSelectedContacts.put(vName.getText().toString(), vAddr.getText().toString());
            }
        });
    }
/*
    private void onOkClicked() {
        dismiss();
    }

    private void onCancelClicked() {
        dismiss();
    }
*/
    public void onClick(DialogInterface dialog, int which) {
  /*      switch (which) {
            case BUTTON_POSITIVE:
                onOkClicked();
                break;

            case BUTTON_NEGATIVE:
            default:
                onCancelClicked();
                break;
        }
        */
    }

    private void loadExistingContacts() throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("contacts.txt")));

        String mLine;
        while ((mLine = reader.readLine()) != null) {
           String[] tokens = mLine.split(",");
           String addr = tokens[0];
           String id = tokens[1];
           HashMap<String,String> entry = new HashMap<String, String>();
           entry.put(TAG_CONTACT_NAME,id);
           entry.put(TAG_CONTACT_ADDRESS,addr);
           mExistingContacts.add(entry);
        }
    }
}
