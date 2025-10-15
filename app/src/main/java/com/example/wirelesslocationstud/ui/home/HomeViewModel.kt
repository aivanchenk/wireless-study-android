package com.example.wirelesslocationstud.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.example.wirelesslocationstud.data.local.dao.MapCellDao
import com.example.wirelesslocationstud.data.local.dao.MapMetadataDao
import com.example.wirelesslocationstud.data.local.database.WirelessDatabase
import com.example.wirelesslocationstud.data.local.entity.MapCellEntity
import com.example.wirelesslocationstud.data.remote.api.RetrofitClient
import com.example.wirelesslocationstud.data.repository.WirelessMapRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WirelessMapRepository
    private val mapCellDao: MapCellDao
    private val mapMetadataDao: MapMetadataDao

    init {
        val database = WirelessDatabase.getDatabase(application)
        mapCellDao = database.mapCellDao()
        mapMetadataDao = database.mapMetadataDao()
        repository = WirelessMapRepository(
            RetrofitClient.wirelessMapApi,
            mapCellDao,
            mapMetadataDao
        )
    }

    private val _selectedTab = MutableLiveData<TabType>().apply {
        value = TabType.CANVAS
    }
    val selectedTab: LiveData<TabType> = _selectedTab

    // Observe all map cells from the database
    val mapCells: LiveData<List<MapCellEntity>> = repository.observeAllMapCells().asLiveData()

    fun selectTab(tabType: TabType) {
        _selectedTab.value = tabType
    }

    enum class TabType {
        CANVAS, IMAGE
    }
}