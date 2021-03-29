/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.util.io.pem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A generic PEM object - type, header properties, and byte content.
 * @hide This class is not part of the Android public SDK API
 */
@libcore.api.CorePlatformApi
public class PemObject
    implements PemObjectGenerator
{
    private static final List EMPTY_LIST = Collections.unmodifiableList(new ArrayList());

    private String type;
    private List   headers;
    private byte[] content;

    /**
     * Generic constructor for object without headers.
     *
     * @param type pem object type.
     * @param content the binary content of the object.
     */
    @libcore.api.CorePlatformApi
    public PemObject(String type, byte[] content)
    {
        this(type, EMPTY_LIST, content);
    }

    /**
     * Generic constructor for object with headers.
     *
     * @param type pem object type.
     * @param headers a list of PemHeader objects.
     * @param content the binary content of the object.
     */
    @libcore.api.CorePlatformApi
    public PemObject(String type, List headers, byte[] content)
    {
        this.type = type;
        this.headers = Collections.unmodifiableList(headers);
        this.content = content;
    }

    @libcore.api.CorePlatformApi
    public String getType()
    {
        return type;
    }

    public List getHeaders()
    {
        return headers;
    }

    @libcore.api.CorePlatformApi
    public byte[] getContent()
    {
        return content;
    }

    public PemObject generate()
        throws PemGenerationException
    {
        return this;
    }
}
