// ----------------------------------------------------------------------------
// Copyright 2017-2019 ARM Ltd.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
