package com.eveexite.coffeemaker_android.data.firebase;

import android.support.annotation.NonNull;

/**
 * Created by ivan on 6/1/17.
 */

public class RxFirebaseDataCastException extends Exception{

    public RxFirebaseDataCastException() {
    }

    public RxFirebaseDataCastException(@NonNull String detailMessage) {
        super(detailMessage);
    }

    public RxFirebaseDataCastException(@NonNull String detailMessage, @NonNull Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RxFirebaseDataCastException(@NonNull Throwable throwable) {
        super(throwable);
    }

}
