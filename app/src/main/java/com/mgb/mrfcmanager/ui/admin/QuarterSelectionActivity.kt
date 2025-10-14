package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.mgb.mrfcmanager.R

class QuarterSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quarter_selection)

        setupToolbar()
        setupQuarterCards()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupQuarterCards() {
        val quarters = listOf(
            Pair("1st Quarter 2025", R.id.cardQ1),
            Pair("2nd Quarter 2025", R.id.cardQ2),
            Pair("3rd Quarter 2025", R.id.cardQ3),
            Pair("4th Quarter 2025", R.id.cardQ4)
        )

        quarters.forEach { (quarterName, cardId) ->
            findViewById<MaterialCardView>(cardId).setOnClickListener {
                openAgenda(quarterName)
            }
        }
    }

    private fun openAgenda(quarter: String) {
        val intent = Intent(this, AgendaManagementActivity::class.java)
        intent.putExtra("QUARTER", quarter)
        startActivity(intent)
    }
}
