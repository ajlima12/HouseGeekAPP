package com.example.geek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.geek.Screens.HomeScreen
import com.example.geek.Screens.LoginScreen
import com.example.geek.Screens.RegisterScreen
import com.example.geek.Screens.homeADM
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class MainActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o Firebase
        FirebaseApp.initializeApp(this)
        setContent {
            MainScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()  // Inicializa o FirebaseAuth

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        topBar = {
            if (currentRoute != "login" && currentRoute != "register") {
                TopBar(

                )
            }
        },
        bottomBar = {
            // Só exibe o BottomNavigationBar nas telas diferentes de login e register
            if (currentRoute != "login" && currentRoute != "register") {
                BottomNavigationBar(navController)
            }
        },
        content = { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                Navigation(navController = navController, auth = auth)  // Passa auth para Navigation
            }
        }
    )
}



@Composable
fun Navigation(navController: NavHostController, auth: FirebaseAuth) {
    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController, auth = auth)
        }
        composable("register") {
            RegisterScreen(navController = navController, auth = auth)
        }
        composable("home") {
            HomeScreen()
        }
        composable("homeADM") {
            homeADM()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 22.sp, // Ajustado para mais destaque
                fontWeight = FontWeight.ExtraBold, // Mais forte
                color = Color.White, // Cor do texto do título
                style = MaterialTheme.typography.h6 // Usando a tipografia do tema
            )
        },
        navigationIcon = {
            IconButton(onClick = { /* Ação ao clicar no ícone de navegação */ }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = Color.White // Cor do ícone de navegação
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Ação para o botão de ações extras */ }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Color.White // Cor do ícone de pesquisa
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF313134) // Um roxo mais escuro para sofisticação
        ),
        modifier = Modifier
            .shadow(8.dp, shape = RectangleShape) // Sombra mais pronunciada
            .fillMaxWidth() // Ocupa toda a largura
    )
}




@Composable
fun BottomNavigationBar(navController: NavController) {
    BottomAppBar(
        backgroundColor = Color(0xFF313134),
        content = {
            Text(
                text = "© 2024 Ana Julia - - Todos os direitos reservados.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .padding(16.dp)
            )
        }
    )
}
