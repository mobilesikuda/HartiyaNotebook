package ru.sikuda.mobile.hartiyanotebook

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.bundleOf
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
                val istream = assets.open("list.html")
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
            text = person.name,
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

//private fun hasPermission(context: Context?, permission: String): Boolean {
//    if (context != null && permission.isNotEmpty()) {
//        //for (permission in permissions) {
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    permission
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                return false
//            }
//        //}
//    }
//    return true
//}

private const val REQUEST = 112
fun CheckCall(context: Context, intent: Intent){
    val permission = Manifest.permission.CALL_PHONE
//    if (!hasPermission(context, permission)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions((context as Activity?)!!, arrayOf(permission), REQUEST)
    } else {
        makeCall(context, intent)
    }
}




fun makeCall(context: Context, intent: Intent) {
    try{
        startActivity(context, intent, bundleOf())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun ItemView(person: Person, switch: (Person) -> Unit) {
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + person.phone))
    val intentMob = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + person.phoneMob))
    val context = LocalContext.current


    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = person.name
        )
        Text(
            text = person.email
        )
        Button( onClick = {
            CheckCall(context, intent)
        }) { Text( person.phone ) }
        Text(
            text = person.phoneAdd
        )
        if (person.phoneMob.isNotEmpty()) {
            Button(onClick = {
                CheckCall(context, intentMob)
            }) { Text(person.phoneMob) }
        }else Divider()
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






