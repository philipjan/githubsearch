package com.example.githubsearch.activity

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.githubsearch.model.BaseResponse
import com.example.githubsearch.model.Items
import com.example.githubsearch.service.GithubService
import com.example.githubsearch.ui.theme.GithubSearchTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class MainActivity : ComponentActivity() {

    private var currentPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val scope = rememberCoroutineScope()
            val vm: ItemViewModel = viewModel()
            val searchView: String by vm.searchText.collectAsState()

            vm.initialFeed()

            GithubSearchTheme {
                Scaffold(
                    topBar = {
                             SearchView(
                                 currentValue = searchView,
                                 valueChanged = {
                                     vm.newText(it)
                                     scope.launch {
                                         delay(500)
                                         vm.search(vm.searchText.value)
                                     }
                                 }
                             )
                    },
                    content = {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ){
                            items(
                                items = vm.baseResponse.value?.items ?: emptyList(), itemContent = {
                                    ItemView(
                                        value = it
                                    ) { repo ->
                                        startIntent(
                                            url = it.url,
                                            ctx = this@MainActivity
                                        )
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    }

}

// Start new intent VIEW
fun startIntent(url: String?, ctx: Context) {

    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)

    ContextCompat.startActivity(
         ctx,
        intent,
        null
    )
}

private fun showLogs(str: String) {
    Log.d("MainActivity", str)
}


@Composable
fun SearchView(currentValue: String, valueChanged: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = currentValue,
        onValueChange = valueChanged,
        label = { Text(text = "Repository") }
    )
}

@Composable
fun ItemView(value: Items, onClick: (Items) -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(4.dp)
            .clickable {
                onClick(value)
            }
    ) {
        Column(
            Modifier.padding(10.dp)
        ) {
            Text(
                text = value.fullName ?: "",
                style = MaterialTheme.typography.h6
            )

            Text(
                text = value.description ?: "",
                style = MaterialTheme.typography.caption,
                fontStyle = FontStyle.Italic,
                maxLines = 1
            )
        }
    }
}

// ViewModel that handles data and its state to be used by Composable Views
class ItemViewModel : ViewModel() {

    val searchText = MutableStateFlow("")
    val baseResponse: MutableState<BaseResponse?> = mutableStateOf(null)

    fun newText(newText: String) {
        searchText.value = newText
    }

    fun initialFeed() {
        GithubService.search(
            url = "https://api.github.com/search/repositories?q=1&per_page=500&page=1",
            response
        )
    }

    fun search(value: String) {
        GithubService.search(
            url = "https://api.github.com/search/repositories?q=${Uri.parse(value)}&per_page=500&page=1",
            response
        )
    }

    val response = object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            showLogs("onFailure: ${e.stackTrace}")
        }

        override fun onResponse(call: Call, response: Response) {
            val data = GithubService.getGson().fromJson(response.body?.string(), BaseResponse::class.java)
            showLogs("onResponse: ${data.items.isNullOrEmpty() }")
            baseResponse.value = data
        }

    }

}