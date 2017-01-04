// IA2DPService.aidl
package com.panocean.audio;

// Declare any non-default types here with import statements

interface IA2DPService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
    //        double aDouble, String aString);
    String getVersion();
}
