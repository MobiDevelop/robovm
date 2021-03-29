/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto;

/**
 * @hide This class is not part of the Android public SDK API
 */
public interface RawAgreement
{
    void init(CipherParameters parameters);

    int getAgreementSize();

    void calculateAgreement(CipherParameters publicKey, byte[] buf, int off);
}
