package com.ferd.foodiegram.data.repositorios;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FollowRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public LiveData<Void> followUser(String targetUid) {
        MutableLiveData<Void> result = new MutableLiveData<>();
        DocumentReference meRef = firestore.collection("usuarios").document(currentUid);
        DocumentReference youRef = firestore.collection("usuarios").document(targetUid);

        //añadir target a mis “seguidos”
        meRef.update("seguidos", FieldValue.arrayUnion(targetUid))
                .addOnSuccessListener(v ->
                        //añadirme a los “seguidores” del otro
                        youRef.update("seguidores", FieldValue.arrayUnion(currentUid))
                                .addOnSuccessListener(v2 -> result.setValue(null))
                                .addOnFailureListener(e -> result.setValue(null))
                )
                .addOnFailureListener(e -> result.setValue(null));

        return result;
    }

    public LiveData<Void> unfollowUser(String targetUid) {
        MutableLiveData<Void> result = new MutableLiveData<>();
        DocumentReference meRef = firestore.collection("usuarios").document(currentUid);
        DocumentReference youRef = firestore.collection("usuarios").document(targetUid);

        meRef.update("seguidos", FieldValue.arrayRemove(targetUid))
                .addOnSuccessListener(v ->
                        youRef.update("seguidores", FieldValue.arrayRemove(currentUid))
                                .addOnSuccessListener(v2 -> result.setValue(null))
                                .addOnFailureListener(e -> result.setValue(null))
                )
                .addOnFailureListener(e -> result.setValue(null));

        return result;
    }

    public LiveData<Boolean> isFollowing(String targetUid) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        firestore.collection("usuarios")
                .document(currentUid)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    @SuppressWarnings("unchecked")
                    List<String> segs = (List<String>) snap.get("seguidos");
                    result.setValue(segs != null && segs.contains(targetUid));
                });
        return result;
    }
}
