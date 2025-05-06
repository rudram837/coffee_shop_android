package com.example.myapplication.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.Model.CategoryModel
import com.example.myapplication.Model.ItemsModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainViewModel: ViewModel() {

    private val firebaseDatabase=FirebaseDatabase.getInstance()
    private val _category = MutableLiveData<MutableList<CategoryModel>>()
    private val _popular = MutableLiveData<MutableList<ItemsModel>>()
    private val _brew = MutableLiveData<MutableList<ItemsModel>>()
    private val _offer = MutableLiveData<MutableList<ItemsModel>>()

    val category: LiveData<MutableList<CategoryModel>> = _category
    val popular: LiveData<MutableList<ItemsModel>> = _popular
    val brewBased: LiveData<MutableList<ItemsModel>> = _brew
    val offer: LiveData<MutableList<ItemsModel>> = _offer

    fun loadCategory(){
        val Ref=firebaseDatabase.getReference("Category")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists= mutableListOf<CategoryModel>()
                for (childSnapshot in snapshot.children){
                    val list= childSnapshot.getValue(CategoryModel::class.java)
                    if(list!=null){
                        lists.add(list)
                    }
                }
                _category.value = lists
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun loadPopular() {
        val Ref = firebaseDatabase.getReference("Items")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<ItemsModel>()

                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(ItemsModel::class.java)
                    if (list!=null) {
                        lists.add(list)
                    }
                }
                _popular.value = lists
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun loadbrewBased() {
        val Ref = firebaseDatabase.getReference("BrewBased")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<ItemsModel>()

                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(ItemsModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _brew.value = lists // Correct field assignment
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun loadOffer() {
        val Ref = firebaseDatabase.getReference("Offers")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<ItemsModel>()

                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(ItemsModel::class.java)
                    if (list!=null) {
                        lists.add(list)
                    }
                }
                _offer.value = lists
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}