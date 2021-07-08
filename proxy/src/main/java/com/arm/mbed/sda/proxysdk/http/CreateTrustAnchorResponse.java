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
package com.arm.mbed.sda.proxysdk.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
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
