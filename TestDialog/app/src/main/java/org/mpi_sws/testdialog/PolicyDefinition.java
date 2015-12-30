package org.mpi_sws.testdialog;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Created by herbster on 16/10/15.
 */
public class PolicyDefinition implements Parcelable {

    private List<String> mPolicyList;
    private Map<String,PolicyRules> mPolicyValues;
    static final String NEW_LINE = "\n";
    static final String TAB = "\t";

    public static final String APPLY_FOR_ALL = "all";

    public PolicyDefinition() {
        mPolicyList = new ArrayList<String>();
        mPolicyValues = new HashMap<String,PolicyRules>();
    }

    protected PolicyDefinition(Parcel in) {
        mPolicyList = in.createStringArrayList();
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            String key = in.readString();
            PolicyRules value = in.readParcelable(PolicyRules.class.getClassLoader());
            mPolicyValues.put(key,value);
        }
        String teste = "";
        String[] hello = teste.split(",");
    }

    public static final Creator<PolicyDefinition> CREATOR = new Creator<PolicyDefinition>() {
        @Override
        public PolicyDefinition createFromParcel(Parcel in) {
            return new PolicyDefinition(in);
        }

        @Override
        public PolicyDefinition[] newArray(int size) {
            return new PolicyDefinition[size];
        }
    };

    public boolean addPolicy(String policy) {
        return mPolicyList.add(policy);
    }

    public boolean containsPolicy(String policy) {
        return mPolicyList.contains(policy);
    }

    public PolicyRules getPolicyValues(String policy) {
        return mPolicyValues.get(policy);
    }

    public PolicyRules setPolicyValues(String policy, PolicyRules policyValues) {
        return mPolicyValues.put(policy, policyValues);
    }

    public boolean match(String policyName, String ruleAsString) {
        if ( (policyName == null) || (ruleAsString == null) )
            return false;
        if (!mPolicyList.contains(policyName))
            return false;

        String expressionRegex = "";
        String operationsRegex = "";

        PolicyRules all  = getPolicyValues("all");
        PolicyRules givenRules = getPolicyValues(policyName);

        List<PolicyExpression> allExpressions = new ArrayList<PolicyExpression>();
        List<PolicyOperator> allOperators = new ArrayList<PolicyOperator>();
        if (all.mExpressions != null) {
            allExpressions.addAll(all.mExpressions);
        }

        if (all.mOperators != null) {
            allOperators.addAll(all.mOperators);
        }

        String separator = "";
        for (PolicyExpression expression : allExpressions) {
            expressionRegex += separator + expression.toString();
            separator = "|";
        }

        if (givenRules.mExpressions != null) {
            allExpressions.addAll(givenRules.mExpressions);
        }

        if (givenRules.mOperators != null) {
            allOperators.addAll(givenRules.mOperators);
        }

        separator = "";
        for (PolicyOperator operation : allOperators) {
            operationsRegex += separator + operation.toString();
            separator = "|";
        }

        return ruleAsString.matches("((" + expressionRegex + ")|(" + operationsRegex + ")|(" + expressionRegex + "))");
    }

    public String toXML() {
        final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        String result = XML_HEADER + NEW_LINE;
        result += "<" + PolicyConfigFileParser.POLICIES_DEF_TAG + ">" + NEW_LINE;
        for (String policyName : mPolicyList) {
            result += TAB + "<" + PolicyConfigFileParser.POLICY_TAG + " name=\"" + policyName + "\"/>" + NEW_LINE;
        }
        Set<String> keys = mPolicyValues.keySet();
        for (String key : keys) {
            PolicyRules ruleSet = mPolicyValues.get(key);
            result += TAB + "<" + PolicyConfigFileParser.RULES_TAG + " apply=\"" + key + "\">" + NEW_LINE;
            result += ruleSet.toString();
            result += TAB + "</" + PolicyConfigFileParser.RULES_TAG + ">" + NEW_LINE;
        }
        result += "</" + PolicyConfigFileParser.POLICIES_DEF_TAG + ">" + NEW_LINE;
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeStringList(mPolicyList);
        parcel.writeInt(mPolicyValues.size());
        for(Map.Entry<String,PolicyRules> entry : mPolicyValues.entrySet()){
            parcel.writeString(entry.getKey());
            parcel.writeParcelable(entry.getValue(), flags);
        }
    }

    public static class PolicyRules implements Parcelable {
        List<PolicyExpression> mExpressions;
        List<PolicyOperator> mOperators;

        public PolicyRules() {
            mExpressions = new ArrayList<PolicyExpression>();
            mOperators = new ArrayList<PolicyOperator>();
        }

        protected PolicyRules(Parcel in) {
            mExpressions = in.createTypedArrayList(PolicyExpression.CREATOR);
            mOperators = in.createTypedArrayList(PolicyOperator.CREATOR);
        }

        public static final Creator<PolicyRules> CREATOR = new Creator<PolicyRules>() {
            @Override
            public PolicyRules createFromParcel(Parcel in) {
                return new PolicyRules(in);
            }

            @Override
            public PolicyRules[] newArray(int size) {
                return new PolicyRules[size];
            }
        };

        public String toXML() {
            String result = "";
            if (!mExpressions.isEmpty()) {
                result += TAB + TAB + "<" + PolicyConfigFileParser.EXPRESSION_TAG + ">" + NEW_LINE;
                for (PolicyExpression expr : mExpressions) {
                    result += expr.toString();
                }
                result += TAB + TAB + "</" + PolicyConfigFileParser.EXPRESSION_TAG + ">" + NEW_LINE;
            }

            if (!mOperators.isEmpty()) {
                result += TAB + TAB + "<" + PolicyConfigFileParser.OPERATORS_TAG + ">" + NEW_LINE;
                for (PolicyOperator operat : mOperators) {
                    result += operat.toString();
                }
                result += TAB + TAB + "</" + PolicyConfigFileParser.OPERATORS_TAG + ">" + NEW_LINE;
            }
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeTypedList(mExpressions);
            parcel.writeTypedList(mOperators);
        }
    }

    public static class PolicyExpression implements Parcelable {
        String mName;
        String mFormat;
        String mValue;

        public PolicyExpression(String name, String format, String value) {
            mName = name;
            mFormat = format;
            mValue = value;
        }

        public PolicyExpression(String name, String format) {
            this(name,format,"");
        }

        protected PolicyExpression(Parcel in) {
            mName = in.readString();
            mFormat = in.readString();
            mValue = in.readString();
        }

        public static final Creator<PolicyExpression> CREATOR = new Creator<PolicyExpression>() {
            @Override
            public PolicyExpression createFromParcel(Parcel in) {
                return new PolicyExpression(in);
            }

            @Override
            public PolicyExpression[] newArray(int size) {
                return new PolicyExpression[size];
            }
        };

        public String toXML() {
            String result = TAB + TAB + TAB + "<" + PolicyConfigFileParser.ONE_EXPR_TAG + " name=\"" + mName + "\" format=\"" + mFormat;
            if ( mFormat.equals("expression") ) {
                result += "\" value=\"" + mValue;
            }
            result += "\">" + NEW_LINE;
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(mName);
            parcel.writeString(mFormat);
            parcel.writeString(mValue);
        }

        @Override
        public String toString() {
            return mName + (mFormat.equals("expression") ? "=" + mValue : "");
        }
    }

    public static class PolicyOperator implements Parcelable {
        String mName;
        String mValue;

        public PolicyOperator(String name, String value) {
            mName = name;
            mValue = value;
        }

        protected PolicyOperator(Parcel in) {
            mName = in.readString();
            mValue = in.readString();
        }

        public static final Creator<PolicyOperator> CREATOR = new Creator<PolicyOperator>() {
            @Override
            public PolicyOperator createFromParcel(Parcel in) {
                return new PolicyOperator(in);
            }

            @Override
            public PolicyOperator[] newArray(int size) {
                return new PolicyOperator[size];
            }
        };

        public String toXML() {
            return TAB + TAB + TAB + "<" + PolicyConfigFileParser.ONE_OPER_TAG + " name=\"" + mName + "\" value=\"" + mValue +"\">" + NEW_LINE;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(mName);
            parcel.writeString(mValue);
        }

        @Override
        public String toString() {
            return mValue;
        }

    }

//    public class ExpressionEvaluator {
//
//        public static final String AND_OP = "and";
//        public static final String OR_OP = "or";
//        public static final String NOT_OP = "not";
//
//        public static final String TIMEZONE_EXPR = "timezone";
//        public static final String TIMEFRAME_EXPR = "timeframe";
//        public static final String GEO_AREA_EXPR = "geo_area";
//
//
//        /**
//         * The default operator precedence: NOT, AND, OR.
//         */
//        private Map<String, Integer> defaultPrecedenceMap;
//
//        static {
//
//        }
//
//        public ExpressionEvaluator() {
//            defaultPrecedenceMap = new HashMap<>();
//            defaultPrecedenceMap.put(NOT_OP, 1);
//            defaultPrecedenceMap.put(AND_OP, 2);
//            defaultPrecedenceMap.put(OR_OP, 3);
//        }
//
//        /**
//         * Calls for Shunting Yard algorithm with default operator precedence.
//         *
//         * @param tokens the tokens in infix notation.
//         * @return the list of tokens in postfix notation, or <code>null</code> if
//         * the input token list is invalid.
//         */
//        public List<String> shuntingYard(final Deque<String> tokens) {
//            switch () {
//                case 1:
//                    break;
//
//            }
//            return shuntingYard(tokens, defaultPrecedenceMap);
//        }
//
//        /**
//         * Returns a list of tokens in postfix notation. If the input list of tokens
//         * is invalid (by having, say, incorrect parenthesization),
//         * <code>null</code> is returned.
//         *
//         * @param tokens        the tokens in infix notation.
//         * @param precedenceMap the operator precedence map.
//         * @return the list of tokens in postfix notation, or <code>null</code> if
//         * the input token list is invalid.
//         */
//        public List<String> shuntingYard(final Deque<String> tokens,
//                     final Map<String, Integer> precedenceMap) {
//            final Deque<String> operatorStack = new LinkedList<>();
//            final Deque<String> outputQueue = new LinkedList<>();
//            String previousToken = "";
//
//            while (!tokens.isEmpty()) {
//                final String currentToken = tokens.removeFirst();
//
//                if (isVariableOrConstantName(currentToken)) {
//                    outputQueue.add(currentToken);
//                } else if (isOperator(currentToken)) {
//                    while (!operatorStack.isEmpty()) {
//                        if (isOperator(operatorStack.getLast())
//                                && precedenceCompare(operatorStack.getLast(),
//                                currentToken,
//                                precedenceMap)) {
//                            outputQueue.addLast(operatorStack.removeLast());
//                        } else {
//                            break;
//                        }
//                    }
//
//                    operatorStack.addLast(currentToken);
//                } else if (currentToken.equals("(")) {
//                    if (isVariableOrConstantName(previousToken)) {
//                        return null;
//                    }
//
//                    operatorStack.addLast("(");
//                } else if (currentToken.equals(")")) {
//                    if (!isVariableOrConstantName(previousToken)) {
//                        return null;
//                    }
//
//                    while (!operatorStack.isEmpty() &&
//                            !operatorStack.getLast().equals("(")) {
//                        outputQueue.addLast(operatorStack.removeLast());
//                    }
//
//                    if (operatorStack.isEmpty()) {
//                        // Parenthesis structure is invalid.
//                        return null;
//                    } else {
//                        // remove left parenthesis '('
//                        operatorStack.removeLast();
//                    }
//                } else {
//                    throw new IllegalStateException(
//                            "Could not recognize a token: " + currentToken);
//                }
//
//                previousToken = currentToken;
//            }
//
//            while (!operatorStack.isEmpty()) {
//                final String operator = operatorStack.removeLast();
//
//                // Parenthesis structure is invalid.
//                if (operator.equals("(") || operator.equals(")")) {
//                    return null;
//                }
//
//                outputQueue.addLast(operator);
//            }
//
//            return new ArrayList<>(outputQueue);
//        }
//
//        /**
//         * Compares <code>stackTopToken</code> and <code>token</code> by their
//         * precedences.
//         *
//         * @param stackTopToken the token at the top of operator stack.
//         * @param token         the token to compare against.
//         * @param precedenceMap the operator precedence map.
//         * @return <code>true</code> if the token at the top of the stack precedes
//         * <code>token</code>.
//         */
//        private boolean precedenceCompare(final String stackTopToken,
//                          final String token,
//                          final Map<String, Integer> precedenceMap) {
//            return precedenceMap.get(stackTopToken) < precedenceMap.get(token);
//        }
//
//        /**
//         * Checks whether the input token is a variable or constant name.
//         *
//         * @param token the token to check.
//         * @return <code>true</code> if and only if <code>token</code> is a constant
//         * (such as "0" or "1") or any other token that cannot be recognized
//         * as an operator, parenthesis, or a constant.
//         */
//        private boolean isVariableOrConstantName(final String token) {
//            if (isOperator(token)) {
//                return false;
//            }
//
//            if (token.equals("(")) {
//                return false;
//            }
//
//            if (token.equals(")")) {
//                return false;
//            }
//
//            return !token.isEmpty();
//        }
//
//        /**
//         * Checks whether the input token denotes an operator such as AND, NOT, OR.
//         *
//         * @param token the token to check.
//         * @return <code>true</code> if and only if <code>token</code> is an
//         * operator.
//         */
//        private boolean isOperator(final String token) {
//            switch (token) {
//                case AND_OP:
//                case NOT_OP:
//                case OR_OP:
//                    return true;
//
//                default:
//                    return false;
//            }
//        }
//
//        /**
//         * Splits the input text into a list of tokens.
//         *
//         * @param text the text to split.
//         * @return the list of tokens.
//         */
//        private Deque<String> toTokenList(final String text) {
//            final Deque<String> tokenList = new ArrayDeque<>();
//
//            int index = 0;
//
//            while (index < text.length()) {
//                if (text.substring(index).startsWith(AND_OP)) {
//                    index += AND_OP.length();
//                    tokenList.add(AND_OP);
//                } else if (text.substring(index).startsWith(OR_OP)) {
//                    index += OR_OP.length();
//                    tokenList.add(OR_OP);
//                } else if (text.substring(index).startsWith(NOT_OP)) {
//                    index += NOT_OP.length();
//                    tokenList.add(NOT_OP);
//                } else if (text.charAt(index) == '(') {
//                    ++index;
//                    tokenList.add("(");
//                } else if (text.charAt(index) == ')') {
//                    ++index;
//                    tokenList.add(")");
//                } else if (text.charAt(index) == '0') {
//                    ++index;
//                    tokenList.add("0");
//                } else if (text.charAt(index) == '1') {
//                    ++index;
//                    tokenList.add("1");
//                } else {
//                    int index2 = index;
//
//                    while (index2 < text.length()
//                            && !Character.isWhitespace(text.charAt(index2))
//                            && text.charAt(index2) != '('
//                            && text.charAt(index2) != ')') {
//                        ++index2;
//                    }
//
//                    final String variableName = text.substring(index, index2);
//                    index += variableName.length();
//                    tokenList.add(variableName);
//                }
//
//                index = advancePastWhitespace(text, index);
//            }
//
//            return tokenList;
//        }
//
//        /**
//         * Advances the input index to a position with non-whitespace character.
//         *
//         * @param text  the text.
//         * @param index the index.
//         * @return the index no less than <code>index</code> at which text contains
//         * a non-whitespace character.
//         */
//        private int advancePastWhitespace(final String text, int index) {
//            while (index < text.length()
//                    && Character.isWhitespace(text.charAt(index))) {
//                ++index;
//            }
//
//            return index;
//        }
//    }


}
