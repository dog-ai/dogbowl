/*
 * Copyright (C) 2017, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.client.firebase;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import io.dropwizard.lifecycle.Managed;

public class FirebaseClient implements Managed {
  private static final Logger logger = LoggerFactory.getLogger(FirebaseClient.class);

  private String url;
  private String token;

  private Firebase firebase;
  private AtomicBoolean connected = new AtomicBoolean(false);

  public FirebaseClient(String url, String token) {
    this.url = url;
    this.token = token;

    firebase = new Firebase(url);
  }

  public void start() {
    firebase.authWithCustomToken(token, new Firebase.AuthResultHandler() {
      public void onAuthenticated(AuthData authData) {
        firebase.child(".info/connected").addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot snapshot) {
            connected.set((Boolean) snapshot.getValue());
          }

          @Override
          public void onCancelled(FirebaseError error) {
          }
        });
      }

      public void onAuthenticationError(FirebaseError error) {
        logger.error(error.getMessage());
      }
    });
  }

  public void stop() {
    firebase.getApp().goOffline();
  }

  public String getUrl() {
    return url;
  }

  public boolean isConnected() {
    return connected.get();
  }

  public Firebase getFirebase() {
    return firebase;
  }
}
