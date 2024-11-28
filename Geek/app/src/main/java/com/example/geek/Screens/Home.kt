package com.example.geek.Screens


import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.geek.R

import kotlinx.coroutines.delay
import com.google.accompanist.pager.*
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import kotlinx.coroutines.yield
import kotlinx.coroutines.yield

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val colecaoRevistas = "revistas"

    var hqs by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var carrinho by remember { mutableStateOf(mutableListOf<Map<String, Any>>()) }
    var isCartOpen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) } // Estado para controle de loading
    var userName by remember { mutableStateOf("Usuário") } // Variável para armazenar o nome do usuário

    // Função para buscar o nome do usuário logado
    fun buscarNomeUsuario() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userName = document.getString("name") ?: "Usuário"
                    } else {
                        userName = "Usuário"
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Erro ao buscar nome: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Função para buscar as HQs
    fun buscarHqs() {
        db.collection(colecaoRevistas)
            .get()
            .addOnSuccessListener { result ->
                hqs = result.map {
                    it.data.toMutableMap().apply {
                        put("id", it.id)
                    }
                }
                isLoading = false // Finaliza o carregamento após obter as HQs
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Erro ao buscar HQs: ${exception.message}", Toast.LENGTH_SHORT).show()
                isLoading = false // Finaliza o carregamento mesmo em caso de erro
            }
    }

    // Buscar HQs e nome do usuário ao iniciar a tela
    LaunchedEffect(Unit) {
        buscarHqs()
        buscarNomeUsuario() // Buscar nome do usuário logado
    }

    fun adicionarAoCarrinho(hq: Map<String, Any>) {
        carrinho.add(hq) // Adicionar HQ ao carrinho
        Toast.makeText(context, "HQ adicionada ao carrinho com sucesso!", Toast.LENGTH_SHORT).show()
    }

    // Carrossel de banners
    val bannerPagerState = rememberPagerState(initialPage = 0)
    val bannerImages = listOf(
        R.drawable.banner1,  // Imagem 1 no drawable
        R.drawable.banner2,  // Imagem 2 no drawable
        R.drawable.banner3   // Imagem 3 no drawable
    )

    LaunchedEffect(Unit) {
        while (true) {
            yield()
            delay(3000)
            bannerPagerState.animateScrollToPage(
                page = (bannerPagerState.currentPage + 1) % (bannerPagerState.pageCount)
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TopAppBar(
            title = {
                Text(
                    text = "Bem-vindo, $userName",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                    color = Color.Black
                ) },
            actions = {
                IconButton(onClick = { isCartOpen = true }) {
                    Box {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Carrinho",
                            modifier = Modifier.size(32.dp),
                            tint = Color.Black
                        )
                        if (carrinho.size > 0) {
                            // Exibir bolinha vermelha com contador
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color.Red, CircleShape)
                            ) {
                                Text(
                                    text = carrinho.size.toString(),
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        )
        
        // Carrossel de banners
        HorizontalPager(
            state = bannerPagerState,
            count = bannerImages.size, // Usando count para número de páginas
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Garantindo altura fixa
                .padding(5.dp)
        ) { page ->
            // Ajuste na Image para garantir que ela ocupe toda a área
            Image(
                painter = painterResource(id = bannerImages[page]),
                contentDescription = "Banner ${page + 1}",
                modifier = Modifier
                    .fillMaxWidth() // Isso faz a imagem ocupar toda a largura do Pager
                    .height(100.dp)
                    .width(100.dp)// Ajuste a altura conforme necessário
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,  // Ajusta as imagens para preencher o espaço sem cortar
            )
        }

        // Indicadores do Carrossel de Banners
        HorizontalPagerIndicator(
            pagerState = bannerPagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 1.dp),
            activeColor = Color.Black,
            inactiveColor = Color.Gray,
            indicatorWidth = 8.dp,
            indicatorHeight = 8.dp
        )




        // Exibir loading enquanto as HQs estão sendo carregadas
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                CircularProgressIndicator() // Indicador de progresso
            }
        } else {
            // Lista de HQs em Grid (3 cards por linha)
            Text(
                text = "Revistas disponíveis",
                fontSize = 20.sp,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                items(hqs) { hq ->
                    HQCard(hq = hq, onAddToCart = { adicionarAoCarrinho(hq) }) // Passando a função correta
                }
            }
        }

        // Modal do Carrinho
        if (isCartOpen) {
            CartModal(carrinho = carrinho, onDismiss = { isCartOpen = false }, onCheckout = {
                // Lógica de finalizar a compra
                Toast.makeText(context, "Compra finalizada com sucesso!", Toast.LENGTH_SHORT).show()
                carrinho.clear()  // Limpa o carrinho após a compra
                isCartOpen = false
            })
        }
    }
}




@Composable
fun HQCard(hq: Map<String, Any>, onAddToCart: (hq: Map<String, Any>) -> Unit) {
    val context = LocalContext.current // Contexto para o Toast

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small) // Clip com bordas arredondadas
            .border(
                width = 2.dp,
                color = Color(0xFF313134),
                shape = MaterialTheme.shapes.small // Garantindo que a borda seja arredondada
            )
            .shadow(8.dp, shape = MaterialTheme.shapes.small), // Sombra com borda arredondada
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(10.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Caso tenha uma imagem para a HQ
            val imageUrl = hq["imagem"] as? String
            imageUrl?.let {
                // Se a HQ tem uma imagem, exibe aqui
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Imagem da HQ",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(MaterialTheme.shapes.small)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Título da HQ
            Text(
                text = "Título: ${hq["titulo"]}",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Autor da HQ
            Text(
                text = "Autor: ${hq["autor"]}",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Descrição da HQ
            val descricao = hq["descricao"] as? String
            descricao?.let {
                Text(
                    text = "Descrição: $it",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Preço da HQ
            Text(
                text = "Preço: R$ ${hq["preco"]}",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF416742)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )



            // Botão para adicionar ao carrinho
            Button(
                onClick = {
                    onAddToCart(hq) // Passa o item para a função de adicionar ao carrinho
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Adicionar ao Carrinho", fontSize = 13.sp, color= Color.White)
            }
        }
    }
}

@Composable
fun CartModal(carrinho: List<Map<String, Any>>, onDismiss: () -> Unit, onCheckout: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(MaterialTheme.shapes.large)
                .shadow(8.dp),
            elevation = CardDefaults.elevatedCardElevation(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Carrinho",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF313134),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Se o carrinho estiver vazio, exibe uma mensagem
                if (carrinho.isEmpty()) {
                    Text(
                        text = "Carrinho vazio",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Lista de itens no carrinho
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(carrinho) { hq ->
                            CartItem(hq)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))  // Divisor entre os itens
                        }
                    }

                    // Calculando o total da compra (convertendo de String para Double)
                    val totalCompra = carrinho.sumOf {
                        (it["preco"] as? String)?.toDoubleOrNull() ?: 0.0
                    }

                    // Exibindo o total da compra
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Total: R$ ${"%.2f".format(totalCompra)}", // Formatação do total
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF313134),
                        modifier = Modifier.align(Alignment.End) // Alinhando à direita
                    )

                    // Botão para finalizar a compra
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onCheckout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                    ) {
                        Text("Finalizar Compra", color = Color.White)
                    }
                }

                // Botão para fechar o modal
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Fechar", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

@Composable
fun CartItem(hq: Map<String, Any>) {
    val imageUrl = hq["imagem"] as? String
    val titulo = hq["titulo"] as? String ?: "Sem título"
    val autor = hq["autor"] as? String ?: "Autor desconhecido"
    val preco = hq["preco"] as? String ?: "Preço não disponível"

    Row(modifier = Modifier.fillMaxWidth()) {
        // Imagem da HQ
        imageUrl?.let {
            Image(
                painter = rememberImagePainter(it),
                contentDescription = "Imagem da HQ",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Informações da HQ
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Autor: $autor",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Preço: R$ $preco",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
