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
package com.arm.mbed.sda.proxysdk.utils.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;

import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;

import com.arm.mbed.crypto.CryptoConstants;
import com.arm.mbed.sda.proxysdk.ProxyException;
import com.arm.mbed.sda.proxysdk.utils.ConsoleLogger;

public class FileKeyStore implements IKeyStore {
    ConsoleLogger logger = new ConsoleLogger(this.getClass());

    private static final String PASSWORD                    = "password";
    private static final String defaultKeyStoreFileName     = ".keystore";
    private static final String fileKeyStoreType            = "JCEKS";
    private static final String fileKeyStoreProviderName    = "SunJCE";

    private KeyStore keyStore;
    private String keyStoreFilePath = null;

    /*********************************************** Constructors *****************************************************/
    public FileKeyStore(String path) {
        try {
            keyStore = KeyStore.getInstance(fileKeyStoreType, fileKeyStoreProviderName);
        } catch (KeyStoreException | NoSuchProviderException e) {
            throw new ProxyException("KeyStore.getInstance Failed", e);
        }

        init(path);
    }

    /*********************************************** Public Functions *************************************************/

    public void store() {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(keyStoreFilePath);
            keyStore.store(fileOutputStream, PASSWORD.toCharArray());
            System.out.println("File based keystore saved succesfully");
        } catch(IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new ProxyException("Failed to store keystore", e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    throw new ProxyException("Failed to store keystore", e);
                }
            }
        }
    }

    public boolean containsKey(String alias) {
        try {
            return keyStore.containsAlias(alias);
        } catch (KeyStoreException e) {
            throw new ProxyException("Failed to get key " + alias + " from keystore", e);
        }
    }

    public KeyPair getKeyPair(String alias) {

        PrivateKeyEntry keyEntry = (PrivateKeyEntry) getEntry(alias);

        if (null == keyEntry) {
            return null;
        }
        if (null == keyEntry.getPrivateKey() || null == keyEntry.getCertificate() || null == keyEntry.getCertificate().getPublicKey()) {
            throw new ProxyException("Failed to get key " + alias + ", the key is invalid");
        }

        // Return a key pair
        return new KeyPair(keyEntry.getCertificate().getPublicKey(), keyEntry.getPrivateKey());
    }

    public void setKeyEntry(String alias, KeyPair pair) {

        if ((null == alias) || (alias.isEmpty())) {
            throw new ProxyException("Alias is Empty or Null");
        }

        try {
            Certificate[] chain = new Certificate[] {createPubKeyCertificateForKeyStore(pair)};
            keyStore.setKeyEntry(alias, pair.getPrivate(), PASSWORD.toCharArray(), chain);
            store();
        } catch (KeyStoreException e) {
            throw new ProxyException("Failed to persist key entry in keystore", e);
        }
    }

    public void deleteEntry(String alias) {
        try {
            keyStore.deleteEntry(alias);
            store();
        } catch (KeyStoreException e) {
            throw new ProxyException("Failed to delete entry in keystore", e);
        }
    }

    public static String getDefaultkeystorefilename() {
        return defaultKeyStoreFileName;
    }

    public int size() {
        try {
            return keyStore.size();
        } catch (KeyStoreException e) {
            throw new ProxyException("Failed to get keystore size", e);
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    /*********************************************** Private Functions ************************************************/
    private void init(String path) {
        String workingDir;

        if (null == path || path.isEmpty()) {
            workingDir = System.getProperty("user.dir");
        } else {
            workingDir = path;
        }

        keyStoreFilePath = (new File(workingDir, defaultKeyStoreFileName)).getPath();

        //Create new KeyStore
        if (!(new File(keyStoreFilePath)).exists()) {   // if there is no keystore file
            try {
                keyStore = KeyStore.getInstance(keyStore.getType());
                keyStore.load(null, PASSWORD.toCharArray());
            } catch(IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                throw new ProxyException("Failed to load keystore", e);
            }
            //Store the new KeyStore to a file
            store();
        }
        //(Validate) load the KeyStore from file.
        load();
    }

    private void load() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(keyStoreFilePath);
            keyStore.load(fileInputStream, PASSWORD.toCharArray());
            logger.debug("File keystore '" + keyStoreFilePath + "' loaded successfully");
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new ProxyException("Failed to load keystore", e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    throw new ProxyException("Failed to load keystore", e);
                }
            }
        }
    }

    private Certificate createPubKeyCertificateForKeyStore(KeyPair pair) {
        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + 1000000000);
        X500Name subjectName = new X500Name("CN=for keystore pk storage only, O=Sansa Security, OU=SaPv");
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
        X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
                subjectName,
                serialNumber,
                notBefore,
                notAfter,
                subjectName,
                pair.getPublic());
        try {
            ContentSigner sigGen = new JcaContentSignerBuilder("SHA1withECDSA")
                    .setProvider(CryptoConstants.SC_JCA_PROVIDER_NAME)
                    .build(pair.getPrivate());
            Certificate cert = new JcaX509CertificateConverter()
                    .setProvider(CryptoConstants.SC_JCA_PROVIDER_NAME)
                    .getCertificate(certGen.build(sigGen));
            return cert;
        } catch (OperatorCreationException | CertificateException e) {
            throw new ProxyException("Failed to create X509 certificate", e);
        }
    }

    private Entry getEntry(String alias) {
        try {
            return keyStore.getEntry(alias, new KeyStore.PasswordProtection(PASSWORD.toCharArray()));
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
            throw new ProxyException("Failed to get entry " + alias + " from keystore", e);
        }
    }

}
