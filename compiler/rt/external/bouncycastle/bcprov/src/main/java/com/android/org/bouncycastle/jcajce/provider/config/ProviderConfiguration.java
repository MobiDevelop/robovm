/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.config;

import java.security.spec.DSAParameterSpec;
import java.util.Map;
import java.util.Set;

import javax.crypto.spec.DHParameterSpec;

import com.android.org.bouncycastle.jce.spec.ECParameterSpec;

/**
 * @hide This class is not part of the Android public SDK API
 */
public interface ProviderConfiguration
{
    ECParameterSpec getEcImplicitlyCa();

    DHParameterSpec getDHDefaultParameters(int keySize);

    DSAParameterSpec getDSADefaultParameters(int keySize);

    Set getAcceptableNamedCurves();

    Map getAdditionalECParameters();
}
