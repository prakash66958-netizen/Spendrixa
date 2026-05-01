package com.spendrixa.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import es.antonborri.home_widget.HomeWidgetBackgroundIntent
import com.spendrixa.app.R

class QuickAddActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Please login to Spendrixa first", Toast.LENGTH_LONG).show()

            // Open the main app (which will show the login screen)
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }

            finish()
            return
        }

        // Make the activity window transparent to show our custom rounded corners
        window.setBackgroundDrawableResource(android.R.color.transparent)

        setContentView(R.layout.activity_quick_add)

        val editAmount = findViewById<EditText>(R.id.editAmount)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val radioExpense = findViewById<RadioButton>(R.id.radioExpense)
        val layoutNote = findViewById<LinearLayout>(R.id.layoutNote)
        val editNote = findViewById<EditText>(R.id.editNote)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        // Setup categories
        val categories = arrayOf(
            "FOOD", "TRANSPORT", "SHOPPING", "ENTERTAINMENT",
            "HEALTH", "TRAVEL", "HOUSING", "OTHER"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = adapter
        spinnerCategory.setSelection(categories.indexOf("OTHER"))

        // Show/Hide note based on category
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = categories[position]
                if (selected == "OTHER") {
                    layoutNote.visibility = View.VISIBLE
                    editNote.requestFocus()
                } else {
                    layoutNote.visibility = View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            val amountStr = editAmount.text.toString().trim()
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = spinnerCategory.selectedItem.toString()
            val type = if (radioExpense.isChecked) "EXPENSE" else "INCOME"
            val note = editNote.text.toString().trim()

            // Trigger the Dart background callback
            val pendingIntent = HomeWidgetBackgroundIntent.getBroadcast(
                this,
                Uri.parse("quickadd://add_custom?category=$category&amount=$amountStr&type=$type&note=${Uri.encode(note)}")
            )
            pendingIntent.send()

            Toast.makeText(this, "Adding transaction...", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
