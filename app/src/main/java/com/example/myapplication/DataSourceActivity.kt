package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import kotlinx.android.synthetic.main.activity_data_source.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

class DataSourceActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val key = preferencesKey<Int>(name = "my_counter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_source)
        val dataStore = createDataStore(name = "settings")

        val map = dataStore.data.map {
            it[key] ?: 0
        }
        GlobalScope.launch {
            map.collectLatest {
                withContext(Dispatchers.Main) {
                    tvSetting.text = it.toString()
                }
            }
        }
        launch(Dispatchers.IO) {

        }

        GlobalScope.launch {
            dataStore.edit { settings ->
                settings[key] = 5
            }
        }
    }
}