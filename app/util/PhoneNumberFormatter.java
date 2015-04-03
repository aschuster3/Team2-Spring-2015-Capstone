package util;


import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import java.util.regex.Pattern;

/**
 * Created by max on 3/31/15.
 */
public class PhoneNumberFormatter {

    public static boolean isValidNumber(String phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            PhoneNumber pn = phoneUtil.parse(phoneNumber, "US");

            /*
             * Note: if we want to allow subscriber number ONLY
             * (i.e. no area code),
             * then use phoneUtil.isPossibleNumberWithReason()
             */
            return phoneUtil.isValidNumberForRegion(pn, "US");
        } catch (NumberParseException e) {
            return false;
        }
    }

    public static String getExampleNumber() {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        PhoneNumber example = phoneUtil.getExampleNumberForType(
                "US",
                PhoneNumberUtil.PhoneNumberType.PERSONAL_NUMBER);
        String exampleString = phoneUtil.format(
                phoneUtil.getExampleNumberForType("US", PhoneNumberUtil.PhoneNumberType.PERSONAL_NUMBER),
                PhoneNumberUtil.PhoneNumberFormat.NATIONAL);

        return exampleString;
    }

    /**
     * Checks if a parse-able phoneNumber contains seven digits.
     * @return true if number is parse-able and has only seven digits
     */
    public static boolean isMissingAreaCode(String phoneNumber) {
        String sevenDigitsRegex = "^(\\D*\\d){7}\\D*$";
        try {
            PhoneNumberUtil.getInstance().parse(phoneNumber, "US");
            System.out.println("Parsed!");
        } catch (NumberParseException e) {
            return false;
        }
        return phoneNumber.matches(sevenDigitsRegex);
    }

    /**
     * For displaying phone numbers in a consistent way.
     *
     * "Safe", meaning it will not throw an exception for invalid numbers.
     *
     * If phoneNumber is an invalid number, this method will return
     * phoneNumber (the input string).
     *
     * @return Formatted phone number string, or the original phoneNumber
     * if there is a parse exception
     */
    public static String safeTransformToCommonFormat(String phoneNumber) {
        if (!isValidNumber(phoneNumber)) {
            return phoneNumber;
        }

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String formattedString;
        try {
            PhoneNumber numberToFormat = phoneUtil.parse(phoneNumber, "US");
            formattedString = phoneUtil.format(
                    numberToFormat,
                    PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        } catch (NumberParseException e) {
            formattedString = phoneNumber;
        }
        return formattedString;
    }
}
