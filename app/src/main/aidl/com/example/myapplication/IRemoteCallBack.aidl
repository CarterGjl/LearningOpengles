// IRemoteCallBack.aidl
package com.example.myapplication;
import com.example.myapplication.local.User;
// Declare any non-default types here with import statements

interface IRemoteCallBack {
    void notifyEvent(String event);
    void showUser(in User user);
}
