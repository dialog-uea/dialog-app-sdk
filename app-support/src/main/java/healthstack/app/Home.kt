package healthstack.app

import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import healthstack.app.HomeScreenState.HOME
import healthstack.app.HomeScreenState.TASK
import healthstack.app.pref.AppStage
import healthstack.app.pref.AppStage.Education
import healthstack.app.pref.AppStage.Profile
import healthstack.app.pref.AppStage.Settings
import healthstack.app.pref.AppStage.StudyInformation
import healthstack.app.status.StatusDataType
import healthstack.app.viewmodel.TaskViewModel
import healthstack.app.viewmodel.TaskViewModel.TasksState
import healthstack.kit.task.base.Task
import healthstack.kit.theme.AppTheme
import healthstack.kit.ui.BottomBarNavigation
import healthstack.kit.ui.BottomNavItem
import healthstack.kit.ui.DropdownMenuItemData
import healthstack.kit.ui.TopBarWithDropDown
import healthstack.kit.ui.WeeklyCard
import java.time.LocalDate

/**
 * Enum class representing the possible states of the home screen.
 * @property title A string representing the title of the state.
 */
enum class HomeScreenState(val title: String) {
    TASK("Task"),
    HOME("Home")
}

/**
 * A composable function representing the entire application.
 *
 * @param dataTypeStatus the list of status data types to be displayed in the UI.
 * @param viewModel the view model for the tasks in the UI.
 * @param changeNavigation a function to change the current navigation state.
 */

@Composable
fun Home(
    dataTypeStatus: List<StatusDataType>,
    viewModel: TaskViewModel,
    changeNavigation: (AppStage) -> Unit,
) {

    val firebaseAuth = FirebaseAuth.getInstance()
    var selectedTask by remember {
        mutableStateOf<Task?>(null)
    }

    val state = remember { mutableStateOf(HOME.title) }
    val changeState = { newValue: HomeScreenState -> state.value = newValue.title }

    Scaffold(
        backgroundColor = AppTheme.colors.background,
        topBar = {
            if (state.value == AppStage.Home.title) TopBarWithDropDown(
                "Continue assim, ${firebaseAuth.currentUser?.displayName}!",
                AppTheme.typography.headline3,
                AppTheme.colors.onSurface,
                listOf(
                    DropdownMenuItemData("Perfil", Icons.Default.Person) { changeNavigation(Profile) },
                    DropdownMenuItemData("Configurações", Icons.Default.Settings) { changeNavigation(Settings) },
                    DropdownMenuItemData(
                        "Informação de estudo",
                        Icons.Default.Info
                    ) { changeNavigation(StudyInformation) },
                )
            )
        },
        bottomBar = {
            if (state.value == AppStage.Home.title) BottomBarNavigation(
                listOf(
                    BottomNavItem(AppStage.Home.title, Icons.Default.Home) {
                        changeNavigation(AppStage.Home)
                    },
                    BottomNavItem(Education.title, Icons.Default.MenuBook) {
                        changeNavigation(Education)
                    }
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (selectedTask == null) {
                DailyTaskView(
                    LocalDate.now(),
                    dataTypeStatus,
                    viewModel,
                    changeState
                ) { selectedTask = it }
            } else {
                selectedTask?.let { task ->
                    task.callback = {
                        viewModel.done(task)
                        selectedTask = null
                    }
                    task.canceled = {
                        selectedTask = null
                    }
                    task.Render()
                }
            }
        }
    }
}

/**
 * A composable function that displays the daily tasks.
 *
 * @param date the current date.
 * @param dataTypeStatus the list of status data types to be displayed in the UI.
 * @param viewModel the view model for the tasks in the UI.
 * @param changeState a function to change the current state of the app.
 * @param onStartTask a callback function to start a task.
 */

@Composable
private fun DailyTaskView(
    date: LocalDate,
    dataTypeStatus: List<StatusDataType>,
    viewModel: healthstack.app.viewmodel.TaskViewModel,
    changeState: (HomeScreenState) -> Unit,
    onStartTask: (Task) -> Unit,
) {
    val scrollState = rememberScrollState()
    changeState(HOME)

    Column(
        Modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
    ) {
        Spacer(Modifier.height(10.dp))
        WeeklyCard(date)
        Spacer(Modifier.height(32.dp))

        StatusCards(dataTypeStatus, viewModel)
        Spacer(Modifier.height(40.dp))

        HomeTaskCard(
                "Próximas tarefas",
            viewModel.activeTasks.collectAsState().value,
            { viewModel.syncTasks() }
        ) {
            changeState(TASK)
            onStartTask(it)
        }
        Spacer(Modifier.height(32.dp))
        HomeTaskCard(
            "Hoje",
            viewModel.todayTasks.collectAsState().value
        )
        Spacer(Modifier.height(32.dp))
        HomeTaskCard(
            "Tarefas concluídas",
            viewModel.completedTasks.collectAsState().value
        )
        Spacer(Modifier.height(60.dp))
    }
}

/**
 * A composable function that displays a card view for a list of tasks.
 *
 * @param title the title of the card.
 * @param state the state of the tasks.
 * @param onReload a callback function to reload the tasks.
 * @param onStartTask a callback function to start a task.
 */

@Composable
fun HomeTaskCard(
    title: String,
    state: TasksState,
    onReload: () -> Unit = { },
    onStartTask: (Task) -> Unit = { },
) {
    Column {
        Row(
            horizontalArrangement = SpaceBetween,
            verticalAlignment = CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                title,
                style = AppTheme.typography.headline3,
                color = AppTheme.colors.onSurface
            )
            IconButton(onClick = {
                onReload()
            }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    tint = AppTheme.colors.primary,
                    contentDescription = null,
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        state.tasks.forEach {
            it.CardView {
                onStartTask(it)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
