package com.example.wirelesslocationstud.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _selectedTab = MutableLiveData<TabType>().apply {
        value = TabType.CANVAS
    }
    val selectedTab: LiveData<TabType> = _selectedTab

    private val _contentText = MutableLiveData<String>().apply {
        value = "This is Canvas view - you can see the canvas content here"
    }
    val contentText: LiveData<String> = _contentText

    fun selectTab(tabType: TabType) {
        _selectedTab.value = tabType
        _contentText.value = when (tabType) {
            TabType.CANVAS -> "This is Canvas view - you can see the canvas content here"
            TabType.IMAGE -> "This is Image view - you can see the image content here"
        }
    }

    enum class TabType {
        CANVAS, IMAGE
    }
}