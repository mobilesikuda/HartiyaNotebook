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
import org.jsoup.nodes.Element
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

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

    fun loadPersons(istream: InputStream) {

        viewModelScope.launch(Dispatchers.IO) {

//            var doc: Document? = null
//            try {
//                //doc = Jsoup.connect("http://phonebook.hartiya.ru/telefons.htm").get().
//                val url = "http://phonebook.hartiya.ru/telefons.htm"
//                doc = Jsoup.parse(URL(url).openStream(), "windows-1251", url)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
            //val title: String = doc2?.title() ?: ""

            val results = StringBuilder("")
            BufferedReader(InputStreamReader(istream, "windows-1251")).forEachLine { results.append(it) }
            val resultsAsString = results.toString()
            val listPersons = mutableListOf<Person>()
            val doc = Jsoup.parse(resultsAsString)

            val selectElem = doc?.select("tbody > tr > td") ?: listOf<Element>()
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
          _persons.emit(listPersons)
        }
    }
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

//private var allPersons2 = mutableListOf(
//    Person(
//        Name = "Абдиев Рамиль Муратович",
//        email = "r.abdiev@hartiya.ru",
//        phone = "+74872251498",
//        phone_add = "7213",
//        phone_mob = "",
//        region = "Тула",
//        address = "деревня Малая Еловая, 8-ой километр а/д \"Тула-Новомосковск\", строение 1",
//        org = "ОП ЭКО-ТЕХНОПАРК «ТУЛА»",
//        division = "Производственный участок №1",
//        position = "Мастер смены",
//    ),
//    Person(
//        Name = "Абдуллин Рафаэль Рифатович",
//        email = "r.abdiev@hartiya.ru",
//        phone = "+74872251498",
//        phone_add = "7213",
//        phone_mob = "",
//        region = "Тула",
//        address = "деревня Малая Еловая, 8-ой километр а/д \"Тула-Новомосковск\", строение 1",
//        org = "ОП ЭКО-ТЕХНОПАРК «ТУЛА»",
//        division = "Производственный участок №1",
//        position = "Мастер смены",
//    ),
//)

fun emptyPerson(): Person {
    return Person("", "", "", "", "", "", "", "", "", "")
}