/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.params;


/**
 * @hide This class is not part of the Android public SDK API
 */
public class DHKeyParameters
    extends AsymmetricKeyParameter
{
    private DHParameters    params;

    protected DHKeyParameters(
        boolean         isPrivate,
        DHParameters    params)
    {
        super(isPrivate);

        this.params = params;
    }   

    public DHParameters getParameters()
    {
        return params;
    }

    public boolean equals(
        Object  obj)
    {
        if (!(obj instanceof DHKeyParameters))
        {
            return false;
        }

        DHKeyParameters    dhKey = (DHKeyParameters)obj;

        if (params == null)
        {
            return dhKey.getParameters() == null;
        }
        else
        { 
            return params.equals(dhKey.getParameters());
        }
    }
    
    public int hashCode()
    {
        int code = isPrivate() ? 0 : 1;
        
        if (params != null)
        {
            code ^= params.hashCode();
        }
        
        return code;
    }
}
