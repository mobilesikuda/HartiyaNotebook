package ru.sikuda.mobile.hartiyanotebook

import android.provider.ContactsContract.CommonDataKinds.Organization
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class MainViewModel: ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _persons = MutableStateFlow(allPersons)
    val persons = searchText
        .debounce(1000L)
        .onEach { _isSearching.update { true } }
        .combine(_persons) { text, persons ->
            if(text.isBlank()) {
                persons
            } else {
                //delay(2000L)
                persons.filter {
                    it.doesMatchSearchQuery(text)
                }
            }
        }
        .onEach { _isSearching.update { false } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _persons.value
        )

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }
}

data class Person(
    val Name: String,
    val email: String,
    val phone: String,
    val phone_add: String,
    val phone_mob: String,
    val region: String,
    val address: String,
    val org: String,
    val division: String,
    val position: String,
) {
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            "$Name$email",
            "$Name $email",
            "${Name.first()}",
        )

        return matchingCombinations.any {
            it.contains(query, ignoreCase = true)
        }
    }
}

private val allPersons = listOf(
    Person(
        Name = "Абдиев Рамиль Муратович",
        email = "r.abdiev@hartiya.ru",
        phone = "+74872251498",
        phone_add = "7213",
        phone_mob = "",
        region = "Тула",
        address = "деревня Малая Еловая, 8-ой километр а/д \"Тула-Новомосковск\", строение 1",
        org = "ОП ЭКО-ТЕХНОПАРК «ТУЛА»",
        division = "Производственный участок №1",
        position = "Мастер смены",
    ),
)