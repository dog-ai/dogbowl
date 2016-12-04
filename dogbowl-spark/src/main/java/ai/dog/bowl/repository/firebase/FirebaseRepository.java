/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository.firebase;

import ai.dog.bowl.client.FirebaseRestClient;

public abstract class FirebaseRepository {
  protected static final String FIREBASE_URL = "https://" + System.getenv("FIREBASE_PROJECT_ID") + ".firebaseio.com";
  protected static final String FIREBASE_API_KEY = System.getenv("FIREBASE_API_KEY");

  protected final FirebaseRestClient firebase;

  public FirebaseRepository() {
    this.firebase = new FirebaseRestClient(FIREBASE_URL, FIREBASE_API_KEY);
  }
}
