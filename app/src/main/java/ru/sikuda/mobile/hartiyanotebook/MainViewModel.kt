package ru.sikuda.mobile.hartiyanotebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL

class MainViewModel : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _isOpenCard = MutableStateFlow(false)
    val isOpenCard = _isOpenCard.asStateFlow()
    var currentPerson = emptyPerson()

    private val _persons = MutableStateFlow(allPersons)

    @OptIn(FlowPreview::class)
    val persons = searchText
        .debounce(1000L)
        .onEach { _isSearching.update { true } }
        .combine(_persons) { text, persons ->
            if (text.isBlank()) {
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

    fun onSwitchCard(person: Person) {
        currentPerson = person.copy()
        _isOpenCard.value = !_isOpenCard.value
    }

    fun initPersons(istream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            val results = StringBuilder()
            BufferedReader(
                InputStreamReader(istream, "windows-1251")
            )
                .forEachLine { results.append(it) }

            val listPersons = loadPersons(Jsoup.parse(results.toString()))
            if (listPersons.isNotEmpty()) _persons.emit(listPersons)
        }
    }

    fun updatePersons() {
        viewModelScope.launch(Dispatchers.IO) {
            var doc: Document? = null
            try {
                val url = "http://phonebook.hartiya.ru/telefons.htm"
                doc = Jsoup.parse(URL(url).openStream(), "windows-1251", url)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (doc != null) loadPersons(doc)
            doc?.let {
                val listPersons = loadPersons(it)
                if (listPersons.isNotEmpty()) _persons.emit(listPersons)
            }
        }
    }
}

fun loadPersons(doc: Document): MutableList<Person> {

    val listPersons = mutableListOf<Person>()
    val selectElem = doc.select("tbody > tr > td")
    var iNumField = 1
    var person = emptyPerson()
    for (item in selectElem) {
        val text = item.text().toString()
        when (iNumField) {
            1 -> person.name = text
            2 -> person.email = text
            3 -> person.phone = text
            4 -> person.phoneAdd = text
            5 -> person.phoneMob = text
            6 -> person.region = text
            7 -> person.address = text
            8 -> person.org = text
            9 -> person.division = text
            10 -> person.position = text
        }
        iNumField++
        if (iNumField == 11) {
            listPersons.add(person)
            person = emptyPerson()
            iNumField = 1
        }
    }
    listPersons.sortBy {
        it.name
    }
    return listPersons
}

data class Person(
    var name: String,
    var email: String,
    var phone: String,
    var phoneAdd: String,
    var phoneMob: String,
    var region: String,
    var address: String,
    var org: String,
    var division: String,
    var position: String,
) {
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            "$name$email$phone$phoneAdd$phoneMob$region$address$org$division$position",
            "$name $email $phone $phoneAdd $phoneMob $region $address $org $division $position"
        )

        return matchingCombinations.any {
            it.contains(query, ignoreCase = true)
        }
    }
}

private var allPersons = mutableListOf<Person>()

fun emptyPerson(): Person {
    return Person("", "", "", "", "", "", "", "", "", "")
}