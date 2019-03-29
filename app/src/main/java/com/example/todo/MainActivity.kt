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

        //reference for add button and listview
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        listViewItems = findViewById<View>(R.id.list_view) as ListView

        fab.setOnClickListener {
            showNewTaskUI()     // When the add button clicked it will trigger the showNewTaskUI
        }
K
        mDatabase = FirebaseDatabase.getInstance().reference
        toDoItemList = mutableListOf<DbModel>()
        adapter = Adapter(this, toDoItemList!!)
        listViewItems?.adapter = adapter
        mDatabase.orderByKey().addListenerForSingleValueEvent(itemListener)
    }

    // Adding New ToDo item UI function
    private fun showNewTaskUI() {
        // Popup as an alert dialog
        val alert = AlertDialog.Builder(this)
        val itemEditText = EditText(this)               // Save the typed text as itemEditText variable
        alert.setTitle("Remind me...")
        /*alert.setMessage("type here...")*/
        alert.setView(itemEditText)
        alert.setIcon(R.drawable.ic_mtrl_chip_checked_circle)
        alert.setPositiveButton("Add") { dialog, positiveButton ->
            val todoItem = DbModel.create()                                     // create a todoItem based on our DbModel
            todoItem.itemText = itemEditText.text.toString()                    // Add the typed text to the model
            todoItem.done = false                                               // By default set the done as false
            val newItem = mDatabase.child(Constants.FIREBASE_ITEM).push()       // first make a push create a new item to get an UniqueID
            todoItem.objectId = newItem.key                                     // save that id as objectId
            newItem.setValue(todoItem)                                          // set all values of the todoItem model to newItem
            dialog.dismiss()                                                    // close the dialog box
            Toast.makeText(this,  todoItem.itemText + " is added to the list... ", Toast.LENGTH_SHORT).show()
            // close the current activity and restart it
            val intent = intent
            finish()
            startActivity(intent)
        }
        alert.show()                            //???
    }

    // Following function will monitor the Data changes and update
    private var itemListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            addDataToList(dataSnapshot)
        }

        override fun onCancelled(databaseError: DatabaseError) {
          //  Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())        //???
        }
    }

    // Updating the List (UI)
    private fun addDataToList(dataSnapshot: DataSnapshot) {

        val items = dataSnapshot.children.iterator()                            // Check if current database contains any collection

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


    /* ******************************************** */

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
