/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.symmetric;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.spec.PBEParameterSpec;

import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseAlgorithmParameters;
import com.android.org.bouncycastle.jcajce.provider.util.AlgorithmProvider;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class PBEPKCS12
{
    private PBEPKCS12()
    {

    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class AlgParams
        extends BaseAlgorithmParameters
    {
        PKCS12PBEParams params;

        protected byte[] engineGetEncoded()
        {
            try
            {
                return params.getEncoded(ASN1Encoding.DER);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Oooops! " + e.toString());
            }
        }

        protected byte[] engineGetEncoded(
            String format)
        {
            if (this.isASN1FormatString(format))
            {
                return engineGetEncoded();
            }

            return null;
        }

        protected AlgorithmParameterSpec localEngineGetParameterSpec(
            Class paramSpec)
            throws InvalidParameterSpecException
        {
            if (paramSpec == PBEParameterSpec.class)
            {
                return new PBEParameterSpec(params.getIV(),
                    params.getIterations().intValue());
            }

            throw new InvalidParameterSpecException("unknown parameter spec passed to PKCS12 PBE parameters object.");
        }

        protected void engineInit(
            AlgorithmParameterSpec paramSpec)
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof PBEParameterSpec))
            {
                throw new InvalidParameterSpecException("PBEParameterSpec required to initialise a PKCS12 PBE parameters algorithm parameters object");
            }

            PBEParameterSpec pbeSpec = (PBEParameterSpec)paramSpec;

            this.params = new PKCS12PBEParams(pbeSpec.getSalt(),
                pbeSpec.getIterationCount());
        }

        protected void engineInit(
            byte[] params)
            throws IOException
        {
            this.params = PKCS12PBEParams.getInstance(ASN1Primitive.fromByteArray(params));
        }

        protected void engineInit(
            byte[] params,
            String format)
            throws IOException
        {
            if (this.isASN1FormatString(format))
            {
                engineInit(params);
                return;
            }

            throw new IOException("Unknown parameters format in PKCS12 PBE parameters object");
        }

        protected String engineToString()
        {
            return "PKCS12 PBE Parameters";
        }
    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class Mappings
        extends AlgorithmProvider
    {
        private static final String PREFIX = PBEPKCS12.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {
            provider.addAlgorithm("AlgorithmParameters.PKCS12PBE", PREFIX + "$AlgParams");
        }
    }
}
