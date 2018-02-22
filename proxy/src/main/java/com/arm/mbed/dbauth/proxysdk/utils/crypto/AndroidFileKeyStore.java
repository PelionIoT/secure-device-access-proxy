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
package com.arm.mbed.dbauth.proxysdk.utils.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
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

import com.arm.mbed.dbauth.proxysdk.ProxyException;

public class AndroidFileKeyStore implements IKeyStore {
    private static final String defaultKeyStoreFileName     = ".keystore";
    private static File keyStoreFilePath;

    private KeyStore keyStore;

    /*********************************************** Constructors *****************************************************/
    public AndroidFileKeyStore(String filePath) {
        try {
            keyStore = KeyStore.getInstance("BKS");
        } catch (KeyStoreException e) {
            throw new ProxyException("KeyStore.getInstance Failed", e);
        }
        keyStoreFilePath = new File(filePath, defaultKeyStoreFileName);

        init();
    }

    /*********************************************** Public Functions *************************************************/

    public void store() {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(keyStoreFilePath);
            keyStore.store(fileOutputStream, null);
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
        if (null == keyEntry.getPrivateKey() ||
                null == keyEntry.getCertificate() ||
                null == keyEntry.getCertificate().getPublicKey()) {
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
            keyStore.setKeyEntry(alias, pair.getPrivate(), null, chain);
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

    public Key getKey(String alias) {
        try {
            return keyStore.getKey(alias, null);
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new ProxyException("Failed to get key " + alias + " from keystore", e);
        }
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
    private void init() {
        //Create new KeyStore
        if (!(keyStoreFilePath).exists()) {   // if there is no keystore file
            try {
                keyStore = KeyStore.getInstance(keyStore.getType());
                keyStore.load(null);
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
            keyStore.load(fileInputStream, null);
            System.out.println("File keystore '" + keyStoreFilePath + "' loaded successfully");
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
                    .setProvider(CryptoConstants.PROVIDER_NAME_SPONGY_CASTLE)
                    .build(pair.getPrivate());
            Certificate cert = new JcaX509CertificateConverter()
                    .setProvider(CryptoConstants.PROVIDER_NAME_SPONGY_CASTLE)
                    .getCertificate(certGen.build(sigGen));
            return cert;
        } catch (OperatorCreationException | CertificateException e) {
            throw new ProxyException("Failed to create X509 certificate", e);
        }
    }

    private Entry getEntry(String alias) {
        try {
            return keyStore.getEntry(alias, null);
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
            throw new ProxyException("Failed to get entry " + alias + " from keystore", e);
        }
    }

}
