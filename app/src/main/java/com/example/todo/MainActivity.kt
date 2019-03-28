package com.example.todo

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

 lateinit var mDatabase: DatabaseReference

var toDoItemList: MutableList<DbModel>? = null
lateinit var adapter: Adapter
private var listViewItems: ListView? = null

class MainActivity : AppCompatActivity(), ItemRowListener {

    // For Delete a ToDo Item
    override fun onItemDelete(itemObjectId: String) {
        // Find the specific item on the Firebase DB based on the itemObjectId of the ToDo item
        val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        // remove the item from the DB
        itemReference.removeValue()
        // Display a toast notification on Successful Deletion of the Item
        Toast.makeText(this,  "Removed Successfully ", Toast.LENGTH_SHORT).show()
        // Finish the current activity and start again.. Technically Restart the activity
        val intent = intent
        finish()
        startActivity(intent)
    }

    // For Update a Todo Item
    override fun modifyItemState(itemObjectId: String, isDone: Boolean) {
        // Find the specific item on the Firebase DB based on the itemObjectId of the ToDo item
        val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        // Set the Value
        itemReference.child("done").setValue(isDone)
    }

    // Main function
    override fun onCreate(savedInstanceState: Bundle?) {
        // Get the savedInstance from the previous state
        super.onCreate(savedInstanceState)
        // Load the relevant Layout for the activity
        setContentView(R.layout.activity_main)
        // To Display the Top Toolbar / Disabled
        /*setSupportActionBar(toolbar)*/

        //Initialize the Firebase
        FirebaseApp.initializeApp(this)

        //reference for FAB
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        listViewItems = findViewById<View>(R.id.list_view) as ListView

        fab.setOnClickListener {
            showNewTaskUI()
        }

        mDatabase = FirebaseDatabase.getInstance().reference
        toDoItemList = mutableListOf<DbModel>()
        adapter = Adapter(this, toDoItemList!!)
        listViewItems?.adapter = adapter
        mDatabase.orderByKey().addListenerForSingleValueEvent(itemListener)
    }

    private fun showNewTaskUI() {
        val alert = AlertDialog.Builder(this)
        val itemEditText = EditText(this)
        alert.setMessage("type here...")
        alert.setTitle("Add New To Do Item")
        alert.setView(itemEditText)
        alert.setPositiveButton("Add") { dialog, positiveButton ->
            val todoItem = DbModel.create()
            todoItem.itemText = itemEditText.text.toString()
            todoItem.done = false
            //We first make a push so that a new item is made with a unique ID
                val newItem = mDatabase.child(Constants.FIREBASE_ITEM).push()
                todoItem.objectId = newItem.key
                //then, we used the reference to set the value on that ID
                newItem.setValue(todoItem)
                dialog.dismiss()
            Toast.makeText(this,  todoItem.itemText + " is added to the list... ", Toast.LENGTH_SHORT).show()
            val intent = intent
            finish()
            startActivity(intent)
        }
        alert.show()



    }

    var itemListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // Get Post object and use the values to update the UI
            addDataToList(dataSnapshot)

        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    private fun addDataToList(dataSnapshot: DataSnapshot) {

        val items = dataSnapshot.children.iterator()
        //Check if current database contains any collection
        if (items.hasNext()) {
            val toDoListindex = items.next()
            val itemsIterator = toDoListindex.children.iterator()

            //check if the collection has any to do items or not
            while (itemsIterator.hasNext()) {


                //get current item
                val currentItem = itemsIterator.next()
                val todoItem = DbModel.create()


                //get current data in a map
                val map = currentItem.value as HashMap<String, Any>


                //key will return Firebase ID
                todoItem.objectId = currentItem.key
                todoItem.done = map["done"] as Boolean?
                todoItem.itemText = map["itemText"] as String?

                toDoItemList!!.add(todoItem)
            }
        }


        //alert adapter that has changed
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
