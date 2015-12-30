package org.mpi_sws.testdialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IPrivacyCapsulesActivityService;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    Dialog policiesDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doBindService();

        setUpPolicyList();

        Button dialogButton = (Button) this.findViewById(R.id.show_dialog_button);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAct();
            }
        });
    }

    private void startAct() {
        final Intent intent = new Intent(this, SelectSecureChannelsDialog.class);
        startActivity(intent);
    }

    private WindowManager.LayoutParams getAttributes(Dialog d) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        return lp;
    }

    private void setUpPolicyList() {
        Button dialogButton = (Button) this.findViewById(R.id.dialog_button);
        dialogButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (policiesDialog != null) {
                    policiesDialog.show();
                    policiesDialog.getWindow().setAttributes(getAttributes(policiesDialog));
                }
            }
        });

        Button parserButton = (Button) this.findViewById(R.id.parser_button);
        parserButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                try {
                    InputStream configFile = getAssets().open("privacy_capsules_policies_def.xml");
                    PolicyDefinition policyDefinition = PolicyConfigFileParser.getSingleton().parseConfigFile(configFile);

                    PolicyDefinition policyDefDefault = new PolicyDefinition();
                    policyDefDefault.addPolicy("sensors");
                    policyDefDefault.addPolicy("location");

                    List<PolicyDefinition.PolicyExpression> expressions = new ArrayList<PolicyDefinition.PolicyExpression>();
                    expressions.add(new PolicyDefinition.PolicyExpression("timezone", "plain"));
                    expressions.add(new PolicyDefinition.PolicyExpression("geo_area", "plain"));
                    expressions.add(new PolicyDefinition.PolicyExpression("timeframe", "expression", "[1-9][1-9]*"));

                    List<PolicyDefinition.PolicyOperator> operators = new ArrayList<PolicyDefinition.PolicyOperator>();
                    operators.add(new PolicyDefinition.PolicyOperator("and", "and"));
                    operators.add(new PolicyDefinition.PolicyOperator("or", "or"));

                    PolicyDefinition.PolicyRules rules = new PolicyDefinition.PolicyRules();
                    rules.mOperators = operators;
                    rules.mExpressions = expressions;

                    policyDefDefault.setPolicyValues("all", rules);

                    boolean resultA = policyDefDefault.match("sensors", "timezone");
                    boolean resultB = policyDefDefault.match("sensors", "timeframe=10");
                    boolean resultC = policyDefDefault.match("sensors", "timeframe");
                    boolean resultD = policyDefDefault.match("location", "timezone and geo_area");

                    policyDefDefault.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        createPoliciesDialog();
    }

    private String mServiceName = "org.mpi_sws.wtrackerclient.PrivacyCapsulesActivityService";
    private String mFileName;

    private boolean mIsBound;

    private String mViewDescription;
    private String mViewScreenshoot;

    private IPrivacyCapsulesActivityService mService;

    private static final String TAG = "MainActivity";

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IPrivacyCapsulesActivityService.Stub.asInterface(service);
            try {
                mViewDescription = mService.getViewDescription(mFileName);
                mViewScreenshoot = mService.getViewScreenshootLocation(mFileName);
                Log.d(TAG,"----------------- " + mViewDescription + " : " + mViewScreenshoot);
            } catch(RemoteException e) {
                Log.e(TAG,"Error while calling service from " + mServiceName);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    void doBindService() {
        try {
            //Intent intent = new Intent(MainActivity.this, Class.forName(mServiceName));
            Intent intent = new Intent(MainActivity.this, Class.forName(mServiceName));
            Log.d(TAG,"===================== " + Class.forName(mServiceName));
            intent.setAction(IPrivacyCapsulesActivityService.class.getName());
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG,"Service binded!");
            mIsBound = true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error while binding the service " + e.getLocalizedMessage());
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }


    private void createPoliciesDialog() {
        policiesDialog = PrivacyCapsulesPolicyDialogBuilder.getInstance().buildPolicyDialog(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
