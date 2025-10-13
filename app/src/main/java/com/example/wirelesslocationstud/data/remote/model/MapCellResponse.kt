package com.example.wirelesslocationstud.data.remote.model

data class MapCellResponse(
    val x: Int,
    val y: Int,
    val strength1: Int?,
    val strength2: Int?,
    val strength3: Int?
)

