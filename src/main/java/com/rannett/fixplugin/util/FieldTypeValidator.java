package com.rannett.fixplugin.util;

public class FieldTypeValidator {

    public static boolean isValueValidForType(String type, String value) {
        if (type == null || value == null) {
            return true;
        }
        switch (type.toUpperCase()) {
            case "INT":
            case "LENGTH":
            case "SEQNUM":
                return value.matches("^-?\\d+$");
            case "CHAR":
                return value.length() == 1;
            case "PRICE":
            case "FLOAT":
            case "QTY":
            case "AMT":
            case "PERCENTAGE":
                return value.matches("^-?\\d+(\\.\\d+)?$");
            case "BOOLEAN":
                return value.equals("Y") || value.equals("N");
            case "DAYOFMONTH":
                try {
                    int day = Integer.parseInt(value);
                    return day >= 1 && day <= 31;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "COUNTRY":
                return value.matches("^[A-Z]{2}$");
            case "CURRENCY":
                return value.matches("^[A-Z]{3}$");
            case "MONTHYEAR":
                return value.matches("^(\\d{4}(0[1-9]|1[0-2]))" +  // YYYYMM
                        "((0[1-9]|[12][0-9]|3[01])|(w[1-5]))?$");  // Optional DD or w1-w5
            case "UTCTIMESTAMP":
                return value.matches("^\\d{8}-\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,3})?$");
            case "UTCTIMEONLY":
                return value.matches("^\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,3})?$");
            case "UTCDATEONLY":
            case "LOCALMKTDATE":
                return value.matches("^\\d{8}$"); // YYYYMMDD
            case "TZTIMEONLY":
                return value.matches("^\\d{2}:\\d{2}(:\\d{2})?([Zz]|([+-]\\d{2}:\\d{2}))?$");
            case "TZTIMESTAMP":
                return value.matches("^\\d{8}-\\d{2}:\\d{2}(:\\d{2})?([Zz]|([+-]\\d{2}:\\d{2}))?$");
            case "TENOR":
                return value.matches("^[DWMY][1-9]\\d*$");
            default:
                return true;
        }
    }
}
