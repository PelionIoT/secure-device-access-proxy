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

import org.apache.commons.lang3.ArrayUtils;

import com.upokecenter.cbor.CBORObject;

public class Operation {

    private int type;
    private String function;
    private ParamElement[] params;

    public CBORObject getCBOREncoded() {
        CBORObject cborOp = CBORObject.NewArray().Untag();
        cborOp.Add(CBORObject.FromObject(type).Untag());
        //Adding function name
        cborOp.Add(CBORObject.FromObject(function).Untag());
        //Adding the arguments for the function if exists
        CBORObject cborParams = CBORObject.NewArray().Untag();
        cborOp.Add(cborParams);
        if (!ArrayUtils.isEmpty(params)) {
        	for (ParamElement pe  : params) {
        		switch(pe.getType()) {
        		case INT:
        			cborParams.Add(CBORObject.FromObject(pe.getIntValue()).Untag());
        			break;
        		case STR:
        			cborParams.Add(CBORObject.FromObject(pe.getStringValue()).Untag());
        			break;
        		}
        	}
        }
        return cborOp;
    }

    private Operation(int type, String function, ParamElement[] params) {
        this.type = type;
        this.function = function;
        this.params = params;
    }

    public int getType() {
        return type;
    }

    public String getFunction() {
        return function;
    }

    public ParamElement[] getParams() {
        return params;
    }

    public static class Builder {
        private int type = -1;
        private String function;
        private ParamElement[] params;

        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        public Builder setFunction(String function) {
            this.function = function;
            return this;
        }

        public Builder setParams(ParamElement[] params) {
            this.params = params;
            return this;
        }

        public Operation build() {
            validate();

            return new Operation(type, function, params);
        }

        private void validate() {
            if (-1 == type ||
                    null == function) {
                throw new OperationException("Operation type and/or function is missing");
            }
        }
    }

}
