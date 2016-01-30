package io.pivotal.weatherbus.app.repositories;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class LocationRepository {
    public Observable<Location> create(final Context context) {
        Observable<Location> location = Observable.create(new OnSubscribe<Location>() {
            @Override
            public void call(Subscriber<? super Location> subscriber) {
                ApiClientConnectionCallbacks callbacks = new ApiClientConnectionCallbacks(subscriber);
                final GoogleApiClient client = new GoogleApiClient.Builder(context)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(callbacks)
                        .addOnConnectionFailedListener(callbacks)
                        .build();
                callbacks.setClient(client);

                try {
                    client.connect();
                } catch(Throwable e) {
                    subscriber.onError(e);
                }

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        if(client.isConnected() || client.isConnecting()) {
                            client.disconnect();
                        }
                    }
                }));
            }
        });
        return location;
    }

    private class ApiClientConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        final private Observer<? super Location> observer;

        private GoogleApiClient client;

        private ApiClientConnectionCallbacks(Observer<? super Location> observer) {
            this.observer = observer;
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            try {
                Location location = LocationServices.FusedLocationApi.getLastLocation(client);
                if (location != null) {
                    observer.onNext(location);
                }
                observer.onCompleted();
            } catch (Throwable e) {
                observer.onError(e);
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            observer.onError(new Throwable("Suspend"));
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            observer.onError(new Throwable("Failed"));
        }

        public void setClient(GoogleApiClient client) {
            this.client = client;
        }
    }
}
