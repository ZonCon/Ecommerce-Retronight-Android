package com.megotechnologies.ecommerce_retronight.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

public class Validator {

	public static boolean isValidEmail(CharSequence target) {
		if (TextUtils.isEmpty(target)) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
	}

	public static boolean isValidPassword(CharSequence target) {

		if(target.length() >= 8) {
			return true;	
		} else {
			return false;
		}

	}

	public static boolean isValidPhone(CharSequence target) {

		if(target.length() >= 8) {

			String expression = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$";
			Pattern pattern = Pattern.compile(expression);  
			Matcher matcher = pattern.matcher(target);  
			if(matcher.matches()){  
				return true; 
			} else {
				return false;
			}

		} else {
			return false;
		}

	}
	
	public static boolean isValidPincode(CharSequence target) {

		if(target.length() >= 0) {

			String expression = "^[-+]?[0-9]*\\.?[0-9]+$";
			Pattern pattern = Pattern.compile(expression);  
			Matcher matcher = pattern.matcher(target);  
			if(matcher.matches()){  
				return true; 
			} else {
				return false;
			}

		} else {
			return false;
		}

	}

}
