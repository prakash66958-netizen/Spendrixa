package com.vault.lumina

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import es.antonborri.home_widget.HomeWidgetBackgroundIntent
import com.vault.lumina.R

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

            // Trigger the Dart background callback
            val pendingIntent = HomeWidgetBackgroundIntent.getBroadcast(
                this,
                Uri.parse("quickadd://add_custom?category=$category&amount=$amountStr&type=$type")
            )
            pendingIntent.send()

            Toast.makeText(this, "Adding transaction...", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
