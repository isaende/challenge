package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaHabitoDia(val data: String, val aguaMl: Long = 0, val passos: Long? = null)
