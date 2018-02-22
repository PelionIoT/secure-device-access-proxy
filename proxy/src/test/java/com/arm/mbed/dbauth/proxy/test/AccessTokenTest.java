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
package com.arm.mbed.dbauth.proxy.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import com.arm.mbed.dbauth.proxysdk.SdkUtil;
import com.arm.mbed.dbauth.proxysdk.SecuredDeviceAccess;
import com.arm.mbed.dbauth.proxysdk.cose.CoseException;
import com.arm.mbed.dbauth.proxysdk.cose.Message;
import com.arm.mbed.dbauth.proxysdk.cose.Sign1Message;
import com.arm.mbed.dbauth.proxysdk.cwt.CwtClaimsEnum;
import com.arm.mbed.dbauth.proxysdk.cwt.CwtUtils;
import com.arm.mbed.dbauth.proxysdk.server.IAuthServer;
import com.arm.mbed.dbauth.proxysdk.server.LocalServer;
import com.arm.mbed.dbauth.proxysdk.server.UserPasswordServer;
import com.arm.mbed.dbauth.proxysdk.utils.crypto.EccUtils;
import com.upokecenter.cbor.CBORObject;

public class AccessTokenTest {
    
    @Test
    public void getAccessToken() throws CoseException, NoSuchAlgorithmException, NoSuchProviderException, InvalidParameterSpecException, InvalidKeySpecException, IOException {

        // Will auto-set the default based upon OS.
        SecuredDeviceAccess.setKeyStorePath(null);

        // Get Access Token
        List<String> audience = Arrays.asList("ep:dev1");

        String scope = "read-data";
        
        IAuthServer authServer;
        
        // Determine if run locally or with Integration Lab
        if (Environment.getProperty("AUTH_SERVER_PASSWORD") == null) {
            authServer = new LocalServer("http://localhost:8180", Environment.getProperty("JWT_INTEG_TEST"));
        }
        else {
            authServer = new UserPasswordServer(
                    Environment.getProperty("AUTH_SERVER_BASE_URL"), 
                    Environment.getProperty("AUTH_SERVER_ACCOUNT_ID"), 
                    Environment.getProperty("AUTH_SERVER_USERNAME"), 
                    Environment.getProperty("AUTH_SERVER_PASSWORD") 
                    );
        }

        // authServer.acquireJwt();
       
        // --- Get Access Token ---
        String accessToken = SecuredDeviceAccess.getAccessToken(authServer, 
                                                                audience, 
                                                                scope
                                                                );
        
        // 1. Decode Base64 to bytes
        byte[] cwtTokenBytes = Base64.getDecoder().decode(accessToken);
        ByteArrayInputStream bais = new ByteArrayInputStream(cwtTokenBytes);
        BufferedInputStream bufIn = new BufferedInputStream(bais);
        
        // 2. CBOR Read
        CBORObject cborCose61 = CBORObject.Read(bufIn);
        
        // 3. UnTag ALL
        CBORObject cose = cborCose61.UntagOne();
        
        // 4. Validating Signature
        Sign1Message msg = (Sign1Message) Message.DecodeFromBytes(cose.EncodeToBytes());
        
        // 5. Validate Claims:
        CBORObject cwtClaimsMap = CBORObject.DecodeFromBytes(msg.GetContent());
        
        //    Audience
        CBORObject resultAudience = SdkUtil.getClaimsValue(cwtClaimsMap, CwtClaimsEnum.AUDIENCE);
        String resultScope = SdkUtil.getString(cwtClaimsMap, CwtClaimsEnum.SCOPE);
        
        // This is a Fingerprint of the 
        String resultTrustAnchor = SdkUtil.getString(cwtClaimsMap, CwtClaimsEnum.ISSUER);
        CBORObject cborPopPublicKey = SdkUtil.getClaimsValue(cwtClaimsMap, CwtClaimsEnum.CONFIRMATION);
        PublicKey popPublicKeyDecoded = CwtUtils.decodePublicKey(cborPopPublicKey);
        String pemPopPubKeyResult = EccUtils.createPemStringFromKey(popPublicKeyDecoded);
        Date resultExpiration = SdkUtil.getDate(cwtClaimsMap, CwtClaimsEnum.EXPIRATION_TIME);
        Date resultNotBefore  = SdkUtil.getDate(cwtClaimsMap, CwtClaimsEnum.NOT_BEFORE);

        Instant actualExpirationInstant = resultExpiration.toInstant();
        assertNotNull(actualExpirationInstant, "NotBefore must not be null");
        Instant actualNotBeforeInstant = resultNotBefore.toInstant();
        assertNotNull(actualNotBeforeInstant, "NotBefore must not be null");
        
        assertTrue(actualExpirationInstant.isAfter(actualNotBeforeInstant),
                String.format(
                        "Actual Expiration %s MUST BE after Not Before %s",
                        actualExpirationInstant.toString(), 
                        actualNotBeforeInstant.toString())
                );


        assertNotNull(pemPopPubKeyResult, "POP Key must not be null");
        List<String> resultAudienceAsStrings = SdkUtil.getListOfString(resultAudience.getValues());
        assertEquals(resultAudienceAsStrings, audience);
        
        // When the Audience will change to Array, then size > 0.
        assertEquals(resultAudienceAsStrings.size(), 1, String.format("Expected to find a Single String type in Audience but Found an Array or Map.  Size = %d", resultAudienceAsStrings.size()) );
        assertEquals(resultScope, scope);
        assertFalse( StringUtils.isBlank(resultTrustAnchor)); // Just check it exists. signature verification is done anyway later on.
    }


	
}
