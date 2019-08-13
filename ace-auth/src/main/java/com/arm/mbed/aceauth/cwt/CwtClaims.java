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
package com.arm.mbed.aceauth.cwt;

import java.util.Date;
import java.util.List;
import java.math.BigInteger;
import java.security.PublicKey;

import com.arm.mbed.aceauth.cose.CoseUtils;
import com.upokecenter.cbor.CBORObject;

public class CwtClaims {

    private CBORObject cborObject;

    private CwtClaims(CBORObject cborObject) {
        this.cborObject = cborObject;
    }

    public CBORObject getClaims() {
        return cborObject;
    }

    public CBORObject getClaim(CwtClaimsEnum claim) {
        return cborObject.get(getClaimKey(claim));
    }

    private CBORObject getClaimKey(CwtClaimsEnum claim) {
        return CBORObject.FromObject(claim.getClaimKey()).Untag();
    }

    public static class Builder {

        private String issuer;
        private String subject;
        private List<String> audience;
        private String scope;
        private Date expiration;
        private Date notBefore;
        private Date issuedAt;
        private String cwtId;
        private PublicKey cnf;      // the pop public key

        public Builder setIssuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public Builder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder setAudience(List<String> audience) {
            this.audience = audience;
            return this;
        }

        public Builder setScope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder setExpiration(Date expiration) {
            this.expiration = expiration;
            return this;
        }

        public Builder setNotBefore(Date notBefore) {
            this.notBefore = notBefore;
            return this;
        }

        public Builder setIssuedAt(Date issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public Builder setCwtId(String cwtId) {
            this.cwtId = cwtId;
            return this;
        }

        public Builder setCnf(PublicKey cnf) {
            this.cnf = cnf;
            return this;
        }

        public CwtClaims build() {
            validate();

            CBORObject objClaims = CBORObject.NewMap().Untag();	// the CWT claims
            // issuer
            objClaims.set(getClaimKey(CwtClaimsEnum.ISSUER), CBORObject.FromObject(issuer).Untag());
            // subject
            if (null != subject) {
                objClaims.set(getClaimKey(CwtClaimsEnum.SUBJECT), CBORObject.FromObject(subject).Untag());
            }
            // audience
            CBORObject cborAudience = CBORObject.NewArray().Untag();
            for (String audienceItem : audience) {
                cborAudience.Add(CBORObject.FromObject(audienceItem).Untag());
            }
            objClaims.set(getClaimKey(CwtClaimsEnum.AUDIENCE), cborAudience);
            // scope
            objClaims.set(getClaimKey(CwtClaimsEnum.SCOPE), CBORObject.FromObject(scope).Untag());
            // expiration time
            objClaims.set(
                    getClaimKey(CwtClaimsEnum.EXPIRATION_TIME), 
                    CBORObject.FromObject(expiration.getTime()/1000)
                    .Untag());
            // not before
            objClaims.set(getClaimKey(CwtClaimsEnum.NOT_BEFORE), CBORObject.FromObject(notBefore.getTime()/1000).Untag());
            // issued at
            objClaims.set(getClaimKey(CwtClaimsEnum.ISSUED_AT), CBORObject.FromObject(issuedAt.getTime()/1000).Untag());
            // cwt id - byte[] encoding of Muuid
            byte[] cwtIdBytes = new BigInteger(cwtId,16).toByteArray();
            objClaims.set(getClaimKey(CwtClaimsEnum.CWT_ID), CBORObject.FromObject(cwtIdBytes).Untag());
            // cnf : the pop public key
            CBORObject cosePubKey = CoseUtils.encodeCoseKey(cnf);
            objClaims.set(getClaimKey(CwtClaimsEnum.CONFIRMATION), cosePubKey.Untag());

            return new CwtClaims(objClaims);
        }

        private CBORObject getClaimKey(CwtClaimsEnum claim) {
            return CBORObject.FromObject(claim.getClaimKey()).Untag();
        }

        /*
         * validates the completeness and correctness of the various claims; if invalid throws a CwtException
         */
        private void validate() {
            if (null == issuer ||   // check for completeness
                    null == subject ||
                    null == audience ||
                    null == scope ||
                    null == expiration ||
                    null == notBefore ||
                    null == issuedAt ||
                    null == cwtId ||
                    null == cnf) {
                throw new CwtException("Missing CWT claim");
            }
            // TODO for now assume that correctness is checked by the service input validation
        }
    }

}
