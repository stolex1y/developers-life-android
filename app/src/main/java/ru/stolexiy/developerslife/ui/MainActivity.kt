package ru.stolexiy.developerslife.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.skydoves.landscapist.glide.GlideImage
import ru.stolexiy.developerslife.R
import ru.stolexiy.developerslife.data.Gif
import ru.stolexiy.developerslife.data.GifCategory
import ru.stolexiy.developerslife.data.GifRepository
import ru.stolexiy.developerslife.ui.theme.DevelopersLifeTheme
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel> {
        MainViewModel.provideFactory(GifRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DevelopersLifeTheme {
                DevelopersLifeApp(mainViewModel)
            }
        }
    }
}

@Composable
private fun DevelopersLifeApp(mainViewModel: MainViewModel) {
    val uiState = mainViewModel.uiState
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TopBar()
            Tabs(
                currentCategory = uiState.selectedGifCategory,
                onChangeTab = mainViewModel::changeSelectedGifCategory
            )
            Log.d(TAG, "compose DevelopersLifeApp")
            val currentGif = uiState.currentGif
            if (
                uiState.isOnline(LocalContext.current) &&
                !uiState.isError &&
                currentGif != null
            ) {
                GifCard(
                    onReconnect = mainViewModel::updateCurrentGif,
                    currentGif = currentGif,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.9f)
                        .padding(horizontal = 8.dp)
                )
                GifCardControlButtons(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.1f),
                    isPrevButtonEnabled = uiState.hasPrevGif,
                    isNextButtonEnabled = true,
                    onClickPrev = mainViewModel::prevGif,
                    onClickNext = mainViewModel::nextGif
                )
            } else if (!uiState.isOnline(LocalContext.current) || uiState.isError) {
                ConnectionErrorImage(
                    onReconnect = mainViewModel::updateCurrentGif,
                    errorMsgId = uiState.errorMessageId ?: R.string.internal_app_error
                )
            } else {
                Loading()
            }
        }
    }
}

@Composable
fun ConnectionErrorImage(onReconnect: () -> Unit, errorMsgId: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Cloud, contentDescription = null)
        Text(stringResource(id = errorMsgId))
        Button(
            onClick = onReconnect
        ) {
            Text(
                text = stringResource(id = R.string.try_again).uppercase(Locale.getDefault()),
            )
        }
    }
}

@Composable
fun Loading() {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val indicator = createRef()
        CircularProgressIndicator(
            modifier = Modifier.constrainAs(indicator) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )
    }
}

@Composable
fun TopBar() {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.app_name)
            )
        },
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_person_24),
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
fun Tabs(
    modifier: Modifier = Modifier,
    currentCategory: GifCategory,
    onChangeTab: (GifCategory) -> Unit
) {
    val titles = GifCategory
        .values()
        .toList()
        .map(GifCategory::nameId)
        .map { stringResource(id = it) }

    TabRow(
        modifier = modifier,
        selectedTabIndex = currentCategory.ordinal
    ) {
        titles.forEachIndexed { i, title ->
            Tab(
                modifier = Modifier.padding(vertical = 8.dp),
                selected = currentCategory.ordinal == i,
                onClick = { onChangeTab(GifCategory.values()[i]) }
            ) {
                Text(title)
            }
        }
    }
}

@Composable
fun GifCard(
    onReconnect: () -> Unit,
    currentGif: Gif,
    modifier: Modifier = Modifier,
) {
    Log.d(TAG, "compose GifCard, currentGif: $currentGif")
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            GlideImage(
                imageModel = currentGif.gifURL,
                loading = {
                    Loading()
                },
//                success = {
//                    Text("Success..")
//                },
                failure = {
                    ConnectionErrorImage(
                        onReconnect = onReconnect,
                        errorMsgId = R.string.network_error
                    )
                },
                modifier = Modifier.weight(1f)
            )
            Text(text = currentGif.description)
        }
    }
}

/*
@Composable
fun Gif(
    current: Gif,
    modifier: Modifier = Modifier,
    onLoading: () -> Unit = {},
    onSuccess: () -> Unit = {},
    onFailure: () -> Unit = {}
) {

}
*/

@Composable
fun GifCardControlButtons(
    modifier: Modifier = Modifier,
    isPrevButtonEnabled: Boolean,
    isNextButtonEnabled: Boolean,
    onClickPrev: () -> Unit,
    onClickNext: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedButton(
            onClick = onClickPrev,
            enabled = isPrevButtonEnabled
        ) {
            Image(
                painterResource(id = R.drawable.ic_previous_card_24),
                stringResource(id = R.string.button_prev)
            )
        }
        OutlinedButton(
            onClick = onClickNext,
            enabled = isNextButtonEnabled
        ) {
            Image(
                painterResource(id = R.drawable.ic_next_card_24),
                stringResource(id = R.string.button_next)
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    DevelopersLifeTheme {
//        DevelopersLifeApp()
//    }
//}