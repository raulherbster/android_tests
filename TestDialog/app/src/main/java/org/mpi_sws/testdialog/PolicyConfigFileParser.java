package org.mpi_sws.testdialog;

import android.util.Pair;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by herbster on 16/10/15.
 */
public class PolicyConfigFileParser {

    public static final String POLICIES_DEF_TAG = "policies_definition";
    public static final String POLICY_TAG = "policy";
    public static final String RULES_TAG = "rules";
    public static final String EXPRESSION_TAG = "expressions";
    public static final String ONE_EXPR_TAG = "expression";
    public static final String OPERATORS_TAG = "operators";
    public static final String ONE_OPER_TAG = "operator";

    public static final String NAME_ATTR = "name";
    public static final String APPLY_ATTR = "apply";
    public static final String FORMAT_ATTR = "format";
    public static final String VALUE_ATTR = "value";

    private static PolicyConfigFileParser singleton = new PolicyConfigFileParser();

    private PolicyConfigFileParser() {

    }

    public static synchronized PolicyConfigFileParser getSingleton() {
        return singleton;
    }

    private Map<String,Map<String,String>> mPolicyPackageMap;

    public boolean addPolicyRule(String packageName, String policyName, String policyRule) {
        Map<String,String> pRules = mPolicyPackageMap.get(packageName);

        if (pRules == null) {
            Map<String,String> policyEntry = new HashMap<String,String>();
            policyEntry.put(policyName,policyRule);
            mPolicyPackageMap.put(packageName,policyEntry);
            return true;
        }

        if (pRules.containsKey(policyName))
            return false;
        else {
            pRules.put(policyName,policyRule);
            return true;
        }
    }

    public boolean updatePolicyRule(String packageName, String policyName, String newPolicyRule) {
        Map<String,String> pRules = mPolicyPackageMap.get(packageName);

        if (pRules == null)
            return false;

        if (!pRules.containsKey(policyName))
            return false;
        else {
            pRules.put(policyName,newPolicyRule);
            return true;
        }
    }


    public PolicyDefinition parseConfigFile(InputStream in) {
        PolicyDefinition parsedFile = new PolicyDefinition();

        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(in,null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, "", POLICIES_DEF_TAG);
            while (parser.next() != XmlPullParser.END_TAG) {

                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();

                if (name.equals(POLICY_TAG)) {
                    String policyName = readPolicyName(parser);
                    parsedFile.addPolicy(policyName);
                } else if (name.equals(RULES_TAG)) {
                    Pair<String,PolicyDefinition.PolicyRules> pair = readPolicyRules(parser);
                    parsedFile.setPolicyValues(pair.first,pair.second);
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return parsedFile;
    }

    private String readPolicyName(XmlPullParser parser) throws IOException, XmlPullParserException {
        String policyName = "";
        parser.require(XmlPullParser.START_TAG, "", POLICY_TAG);
        String tag = parser.getName();
        if (tag.equals(POLICY_TAG))
            policyName = parser.getAttributeValue(null, NAME_ATTR);
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, "", POLICY_TAG);
        return policyName;
    }

    private Pair<String,PolicyDefinition.PolicyRules> readPolicyRules(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, "", RULES_TAG);
        String applyAttr = parser.getAttributeValue(null, APPLY_ATTR);
        List<PolicyDefinition.PolicyExpression> expressions = null;
        List<PolicyDefinition.PolicyOperator> operators = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(EXPRESSION_TAG)) {
                expressions = readExpressions(parser);
            } else if (name.equals(OPERATORS_TAG)) {
                operators = readOperators(parser);
            }
        }

        PolicyDefinition.PolicyRules rules = new PolicyDefinition.PolicyRules();
        rules.mExpressions = expressions;
        rules.mOperators = operators;

        return new Pair<String, PolicyDefinition.PolicyRules> (applyAttr,rules);
    }

    private  List<PolicyDefinition.PolicyExpression> readExpressions(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<PolicyDefinition.PolicyExpression> policyExpressions = new ArrayList<PolicyDefinition.PolicyExpression>();
        parser.require(XmlPullParser.START_TAG, "", EXPRESSION_TAG);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(ONE_EXPR_TAG)) {
                parser.require(XmlPullParser.START_TAG, "", ONE_EXPR_TAG);

                String nameAttr = parser.getAttributeValue(null, NAME_ATTR);
                String formatAttr = parser.getAttributeValue(null, FORMAT_ATTR);
                String valueAttr = "";
                if (formatAttr.equals("expression"))
                     valueAttr = parser.getAttributeValue(null, VALUE_ATTR);

                PolicyDefinition.PolicyExpression expr = new PolicyDefinition.PolicyExpression(nameAttr,formatAttr,valueAttr);

                policyExpressions.add(expr);

                parser.nextTag();
                parser.require(XmlPullParser.END_TAG, "", ONE_EXPR_TAG);
            }
        }

        return policyExpressions;
    }

    private List<PolicyDefinition.PolicyOperator> readOperators(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<PolicyDefinition.PolicyOperator> policyOperators = new ArrayList<PolicyDefinition.PolicyOperator>();
        parser.require(XmlPullParser.START_TAG, "", OPERATORS_TAG);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(ONE_OPER_TAG)) {
                parser.require(XmlPullParser.START_TAG, "", ONE_OPER_TAG);

                String nameAttr = parser.getAttributeValue(null, NAME_ATTR);
                String valueAttr = parser.getAttributeValue(null, VALUE_ATTR);

                PolicyDefinition.PolicyOperator oper = new PolicyDefinition.PolicyOperator(nameAttr,valueAttr);

                policyOperators.add(oper);

                parser.nextTag();
                parser.require(XmlPullParser.END_TAG, "", ONE_OPER_TAG);
            }
        }

        return policyOperators;
    }

    public String generateConfigFile(PolicyDefinition policyDef) {
        return "";
    }
}
