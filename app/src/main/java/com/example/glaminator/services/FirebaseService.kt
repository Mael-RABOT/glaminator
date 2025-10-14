package com.example.glaminator.services

import com.google.firebase.database.FirebaseDatabase

class FirebaseService {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun getDatabaseReference() = database.reference
}
