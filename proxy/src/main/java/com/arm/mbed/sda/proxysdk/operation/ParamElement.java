// ----------------------------------------------------------------------------
//   The confidential and proprietary information contained in this file may
//   only be used by a person authorized under and to the extent permitted
//   by a subsisting licensing agreement from ARM Limited or its affiliates.
//
//          (C)COPYRIGHT 2018 ARM Limited or its affiliates.
//              ALL RIGHTS RESERVED
//
//   This entire notice must be reproduced on all copies of this file
//   and copies of this file may only be made by a person if such person is
//   permitted to do so under the terms of a subsisting license agreement
//   from ARM Limited or its affiliates.
// ----------------------------------------------------------------------------
package com.arm.mbed.sda.proxysdk.operation;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ParamElement {
	private OperationArgumentType argType;
	private String stringValue;
	private Integer intValue; 
	final static char DELIM = ';';

	/**
	 * 
	 * @param type : 1 is integer  2 is String
	 * @param value
	 */
	public ParamElement(String typeStr, String value) {
		OperationArgumentType type = OperationArgumentType.valueOf(typeStr.toUpperCase());
		if (!StringUtils.isEmpty(value)) {
			switch (type) {
			case INT:
				argType = type;
				try {
					intValue = Integer.parseInt(value);				
				} catch(Exception e) {
					throw new IllegalArgumentException("value supplied: " + value + "cannot be parsed as integer!");
				}
				break;
			case STR:
				argType = type;				
				stringValue = value;
				break;
			}
		} else {
			throw new IllegalArgumentException("value supplied cannot be null!");
		}
	}
	
	public ParamElement(OperationArgumentType type, String value) {
		this.argType = type;
		if (!StringUtils.isEmpty(value)) {			
			switch (argType) {
			case INT:				
				try {
					intValue = Integer.parseInt(value);				
				} catch(Exception e) {
					throw new IllegalArgumentException("value supplied: " + value + "cannot be parsed as integer!");
				}
				break;
			case STR:								
				stringValue = value;
				break;
			}
		} else {
			throw new IllegalArgumentException("value supplied cannot be null!");
		}
	}


	public static ParamElement parseParamElement(String el) {
		if (el != null && el.length() > 2 && el.indexOf(DELIM) > 0 && el.indexOf(DELIM) < el.length()-1) {
			String [] elAr = el.split(String.valueOf(DELIM));
			if (elAr.length == 2) {
				String type = elAr[0];
				String val = elAr[1];
				return new ParamElement(type, val);
			} else {
				throw new IllegalArgumentException("Cannot parse param element (type,value) from " + el);
			}
		} else {
			throw new IllegalArgumentException("Cannot parse param element (type,value) from " + el);
		}
	}

	public static List<ParamElement> parseParamElements(List<String> opParams) {
		List<ParamElement> result = new ArrayList<ParamElement>();
		for (String el :opParams) {
			ParamElement pe = parseParamElement(el);
			result.add(pe);
		}
		return result;
	}


	public OperationArgumentType getType() {
		return argType;
	}


	public void setType(OperationArgumentType argType) {
		this.argType = argType;
	}


	public String getStringValue() {
		return stringValue;
	}


	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}


	public Integer getIntValue() {
		return intValue;
	}


	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}

	public String toString() {
		switch (argType){
		case INT:
			return "{ " + argType.name() + ","+ getIntValue()+ " }";			
		case STR:
			return "{ " + argType.name() + ","+ getStringValue()+ " }";			
		}
		return "";
	}
}
