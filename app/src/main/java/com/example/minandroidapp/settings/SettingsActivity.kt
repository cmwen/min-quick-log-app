package com.example.minandroidapp.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.minandroidapp.R
import com.example.minandroidapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.settingsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.settingsToolbar.setNavigationOnClickListener { finish() }

        val saved = ThemeManager.getSavedOption(this)
        when (saved) {
            ThemeManager.ThemeOption.SYSTEM -> binding.themeSystem.isChecked = true
            ThemeManager.ThemeOption.LIGHT -> binding.themeLight.isChecked = true
            ThemeManager.ThemeOption.DARK -> binding.themeDark.isChecked = true
        }

        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val option = when (checkedId) {
                R.id.themeLight -> ThemeManager.ThemeOption.LIGHT
                R.id.themeDark -> ThemeManager.ThemeOption.DARK
                else -> ThemeManager.ThemeOption.SYSTEM
            }
            ThemeManager.updateTheme(this, option)
        }
    }
}
