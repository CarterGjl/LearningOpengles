// IRemoteService.aidl
package com.example.myapplication;
import com.example.myapplication.IRemoteCallBack;

// Declare any non-default types here with import statements

interface IRemoteService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String axString);
    void registerCallback(in IRemoteCallBack cb);
    void unregisterCallback(in IRemoteCallBack cb);
}
