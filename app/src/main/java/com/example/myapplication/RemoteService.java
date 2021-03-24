package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import com.example.myapplication.local.User;


public class RemoteService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public final RemoteCallbackList<IRemoteCallBack> mCallbacks = new RemoteCallbackList<IRemoteCallBack>() {
    };
    private final IRemoteService.Stub binder = new IRemoteService.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
            System.out.println("basicTypes");
            int i = mCallbacks.beginBroadcast();
            for (int i1 = 0; i1 < i; i1++) {
                mCallbacks.getBroadcastItem(i1).notifyEvent("basicTypes");
                User carter = new User("Carter");
                mCallbacks.getBroadcastItem(i1).showUser(carter);
            }
            mCallbacks.finishBroadcast();
        }

        @Override
        public void registerCallback(IRemoteCallBack cb) {
            if (cb != null) {
                mCallbacks.register(cb);
            }
        }

        @Override
        public void unregisterCallback(IRemoteCallBack cb) {
            if (cb != null) {
                mCallbacks.unregister(cb);
            }
        }
    };
}
