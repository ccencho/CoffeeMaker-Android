package com.eveexite.coffeemaker_android.data.firebase;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;

/**
 * Created by ivan on 6/1/17.
 */

public abstract class DataSnapshotMapper<T, U> implements Function<T, U> {

    private DataSnapshotMapper() {
    }

    public static <U> DataSnapshotMapper<DataSnapshot, U> of(Class<U> clazz) {
        return new TypedDataSnapshotMapper<U>(clazz);
    }

    public static <U> DataSnapshotMapper<DataSnapshot, List<U>> listOf(Class<U> clazz) {
        return new TypedListDataSnapshotMapper<>(clazz);
    }

    public static <U> DataSnapshotMapper<DataSnapshot, LinkedHashMap<String, U>> mapOf(Class<U> clazz) {
        return new TypedMapDataSnapshotMapper<>(clazz);
    }

    public static <U> DataSnapshotMapper<DataSnapshot, U> of(GenericTypeIndicator<U> genericTypeIndicator) {
        return new GenericTypedDataSnapshotMapper<U>(genericTypeIndicator);
    }

    public static <U> DataSnapshotMapper<RxFirebaseChildEvent<DataSnapshot>, RxFirebaseChildEvent<U>> ofChildEvent(Class<U> clazz) {
        return new ChildEventDataSnapshotMapper<U>(clazz);
    }

    private static <U> U getDataSnapshotTypedValue(DataSnapshot dataSnapshot, Class<U> clazz) {
        U value = dataSnapshot.getValue(clazz);
        if (value == null) {
            throw Exceptions.propagate(new RxFirebaseDataCastException(
                    "unable to cast firebase data response to " + clazz.getSimpleName()));
        }
        return value;
    }

    private static class TypedDataSnapshotMapper<U> extends DataSnapshotMapper<DataSnapshot, U> {

        private final Class<U> clazz;

        public TypedDataSnapshotMapper(final Class<U> clazz) {
            this.clazz = clazz;
        }

        @Override
        public U apply(DataSnapshot dataSnapshot) throws Exception {
            if (dataSnapshot.exists()) {
                return getDataSnapshotTypedValue(dataSnapshot, clazz);
            } else {
                return null;
            }
        }
    }

    private static class TypedListDataSnapshotMapper<U> extends DataSnapshotMapper<DataSnapshot, List<U>> {

        private final Class<U> clazz;

        public TypedListDataSnapshotMapper(final Class<U> clazz) {
            this.clazz = clazz;
        }

        @Override
        public List<U> apply(DataSnapshot dataSnapshot) throws Exception {
            List<U> items = new ArrayList<>();
            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                items.add(getDataSnapshotTypedValue(childSnapshot, clazz));
            }
            return items;
        }
    }

    private static class TypedMapDataSnapshotMapper<U> extends DataSnapshotMapper<DataSnapshot, LinkedHashMap<String, U>> {

        private final Class<U> clazz;

        public TypedMapDataSnapshotMapper(final Class<U> clazz) {
            this.clazz = clazz;
        }

        @Override
        public LinkedHashMap<String, U> apply(DataSnapshot dataSnapshot) throws Exception {
            LinkedHashMap<String, U> items = new LinkedHashMap<>();
            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                items.put(childSnapshot.getKey(), getDataSnapshotTypedValue(childSnapshot, clazz));
            }
            return items;
        }
    }

    private static class GenericTypedDataSnapshotMapper<U> extends DataSnapshotMapper<DataSnapshot, U> {

        private final GenericTypeIndicator<U> genericTypeIndicator;

        public GenericTypedDataSnapshotMapper(GenericTypeIndicator<U> genericTypeIndicator) {
            this.genericTypeIndicator = genericTypeIndicator;
        }

        @Override
        public U apply(DataSnapshot dataSnapshot) throws Exception {
            if (dataSnapshot.exists()) {
                U value = dataSnapshot.getValue(genericTypeIndicator);
                if (value == null) {
                    throw Exceptions.propagate(new RxFirebaseDataCastException(
                            "unable to cast firebase data response to generic type"));
                }
                return value;
            } else {
                return null;
            }
        }
    }

    private static class ChildEventDataSnapshotMapper<U>
            extends DataSnapshotMapper<RxFirebaseChildEvent<DataSnapshot>, RxFirebaseChildEvent<U>> {

        private final Class<U> clazz;

        public ChildEventDataSnapshotMapper(final Class<U> clazz) {
            this.clazz = clazz;
        }

        @Override
        public RxFirebaseChildEvent<U> apply(@NonNull RxFirebaseChildEvent<DataSnapshot> dataSnapshotRxFirebaseChildEvent) throws Exception {
            DataSnapshot dataSnapshot = dataSnapshotRxFirebaseChildEvent.getValue();
            if (dataSnapshot.exists()) {
                return new RxFirebaseChildEvent<U>(
                        dataSnapshot.getKey(),
                        getDataSnapshotTypedValue(dataSnapshot, clazz),
                        dataSnapshotRxFirebaseChildEvent.getPreviousChildName(),
                        dataSnapshotRxFirebaseChildEvent.getEventType());
            } else {
                throw Exceptions.propagate(new RuntimeException("child dataSnapshot doesn't exist"));
            }
        }

    }

}
