/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.util;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

/**
 * @hide This class is not part of the Android public SDK API
 */
public interface AsymmetricKeyInfoConverter
{
    PrivateKey generatePrivate(PrivateKeyInfo keyInfo)
        throws IOException;

    PublicKey generatePublic(SubjectPublicKeyInfo keyInfo)
        throws IOException;
}
