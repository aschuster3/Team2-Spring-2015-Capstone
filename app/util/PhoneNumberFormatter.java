package util;


import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * Created by max on 3/31/15.
 */
public class PhoneNumberFormatter {

    public static boolean isValidNumber(String phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            PhoneNumber pn = phoneUtil.parse(phoneNumber, "US");
            return phoneUtil.isValidNumberForRegion(pn, "US");
        } catch (NumberParseException e) {
            return false;
        }
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
            System.out.println("Hello, ");
            formattedString = phoneUtil.format(
                    numberToFormat,
                    PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            System.out.println("Word");
        } catch (NumberParseException e) {
            formattedString = phoneNumber;
        }
        return formattedString;
    }
}
