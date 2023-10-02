package ru.sikuda.mobile.hartiyanotebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.sikuda.mobile.hartiyanotebook.ui.theme.HartiyaNotebookTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HartiyaNotebookTheme {
                //val context = LocalContext.current
                val viewModel = viewModel<MainViewModel>()
                val istream = assets.open("list.html", )
                viewModel.loadPersons(istream)

                val searchText by viewModel.searchText.collectAsState()
                val persons by viewModel.persons.collectAsState()
                val isSearching by viewModel.isSearching.collectAsState()
                val isOpenCard by viewModel.isOpenCard.collectAsState()

                if (isOpenCard) {
                    ItemView(viewModel.currentPerson, viewModel::onSwitchCard)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        TextField(
                            value = searchText,
                            onValueChange = viewModel::onSearchTextChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = "Search") }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (isSearching) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                items(persons) { person ->
                                    ItemList(person, viewModel::onSwitchCard)
                                    //Toast.makeText(context, person.Name, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemList(
    person: Person,
    onClickItem: (Person) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClickItem(person)
            }
    )
    {
        Text(
            text = person.Name,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp)
        )
        Text(
            text = "${person.email} ${person.phone}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp)
        )
    }
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun ItemView(person: Person, switch: (Person) -> Unit) {

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = person.Name
        )
        Text(
            text = person.email
        )
        Text(
            text = person.phone
        )
        Text(
            text = person.phone_add
        )
        Text(
            text = person.phone_mob
        )
        Text(
            text = person.region
        )
        Text(
            text = person.address
        )
        Text(
            text = person.org
        )
        Text(
            text = person.division
        )
        Text(
            text = person.position
        )
        Button(onClick = { switch(person) }) {
            Text(text = "Закрыть")
        }
    }
}






