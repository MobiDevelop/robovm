/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.asn1.x509;

/**
 * @hide This class is not part of the Android public SDK API
 */
public interface NameConstraintValidator
{
    void checkPermitted(GeneralName name)
        throws NameConstraintValidatorException;

    void checkExcluded(GeneralName name)
            throws NameConstraintValidatorException;

    void intersectPermittedSubtree(GeneralSubtree permitted);

    void intersectPermittedSubtree(GeneralSubtree[] permitted);

    void intersectEmptyPermittedSubtree(int nameType);

    void addExcludedSubtree(GeneralSubtree subtree);
}
