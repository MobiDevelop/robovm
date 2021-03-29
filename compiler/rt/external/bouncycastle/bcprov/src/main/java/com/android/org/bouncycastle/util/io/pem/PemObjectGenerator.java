/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.util.io.pem;

/**
 * Base interface for generators of PEM objects.
 * @hide This class is not part of the Android public SDK API
 */
@libcore.api.CorePlatformApi
public interface PemObjectGenerator
{
    /**
     * Generate a PEM object.
     *
     * @return the generated object.
     * @throws PemGenerationException on failure.
     */
    PemObject generate()
        throws PemGenerationException;
}
