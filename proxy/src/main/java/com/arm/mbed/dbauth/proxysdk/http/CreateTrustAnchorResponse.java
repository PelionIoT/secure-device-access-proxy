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
package com.arm.mbed.dbauth.proxysdk.http;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateTrustAnchorResponse {

    @JsonProperty("public_key")
    private String publicKey;

    private String description;

    @JsonProperty("object")
    protected String objectName;

    protected String id;
    
    private String etag;
    
    @JsonProperty("created_at")
    protected String createdAt;

    public CreateTrustAnchorResponse() {};

    public CreateTrustAnchorResponse(String id, String accountId, String pemPublickKey) {     
        this.id = id;
        this.publicKey = pemPublickKey;
    }

    public String getId() {
        return id;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getDescription() {
        return description;
    }

    public CreateTrustAnchorResponse setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCreatedTime() {
        return createdAt;
    }

    public CreateTrustAnchorResponse setCreatedTime(String createdTime) {
        this.createdAt = createdTime;
        return this;
    }
    
    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }    
}
