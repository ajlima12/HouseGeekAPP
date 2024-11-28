package com.example.geek.Screens


import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.accompanist.flowlayout.FlowRow
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun homeADM() {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val colecaoRevistas = "revistas"

    // States
    var titulo by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") } // Novo campo para descrição
    var imagemUrl by remember { mutableStateOf("") }
    var hqs by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var mensagemErro by remember { mutableStateOf("") }
    var hqEditando by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isEditMode by remember { mutableStateOf(false) } // Adicionando o estado para modo de edição

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
            }
            .addOnFailureListener { exception ->
                mensagemErro = "Erro ao buscar HQs: ${exception.message}"
            }
    }

    // Função para preencher os campos com os dados da HQ a ser editada
    fun preencherCamposParaEdicao(hq: Map<String, Any>) {
        titulo = hq["titulo"] as? String ?: ""
        autor = hq["autor"] as? String ?: ""
        preco = hq["preco"] as? String ?: ""
        descricao = hq["descricao"] as? String ?: "" // Preenchendo a descrição
        imagemUrl = hq["imagem"] as? String ?: ""
        hqEditando = hq
        isEditMode = true // Mudar para o modo de edição
    }

    // Buscar HQs ao iniciar a tela
    LaunchedEffect(Unit) {
        buscarHqs()
    }

    // Função para limpar os campos
    fun limparCampos() {
        titulo = ""
        autor = ""
        preco = ""
        descricao = "" // Limpar campo de descrição
        imagemUrl = ""
        hqEditando = null
        isEditMode = false // Resetando o modo de edição
    }

    // Layout da tela
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Título
        item {
            Text(
                text = "Geek House - Adicionar HQs",
                fontSize = 24.sp,
                modifier = Modifier.padding(8.dp)
            )
        }

        // Campos de entrada
        item {
            InputField(label = "Título", value = titulo, onValueChange = { titulo = it })
        }
        item {
            InputField(label = "Autor", value = autor, onValueChange = { autor = it })
        }
        item {
            InputField(
                label = "Preço",
                value = preco,
                onValueChange = { preco = it },
                keyboardType = KeyboardType.Number
            )
        }
        item {
            InputField(
                label = "Descrição",
                value = descricao,
                onValueChange = { descricao = it }
            )
        }
        item {
            InputField(
                label = "URL da Imagem",
                value = imagemUrl,
                onValueChange = { imagemUrl = it }
            )
        }

        // Botões de ação
        item {
            ActionButtons(
                isEditMode = isEditMode,
                onAddClick = {
                    val hq = hashMapOf(
                        "titulo" to titulo,
                        "autor" to autor,
                        "preco" to preco,
                        "descricao" to descricao,
                        "imagem" to imagemUrl
                    )
                    if (hqEditando == null) {
                        addHQToDatabase(db, colecaoRevistas, hq, context, ::buscarHqs)
                    } else {
                        atualizarHQ(db, colecaoRevistas, hq, hqEditando!!, context, ::buscarHqs)
                    }
                    limparCampos()
                },
                onClearClick = { limparCampos() }
            )
        }

        // Exibição de erros
        if (mensagemErro.isNotEmpty()) {
            item {
                Text(
                    text = mensagemErro,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Divisor entre o formulário e a lista de HQs
        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // Exibindo HQs em duas colunas usando FlowRow
        item {
            FlowRow(
                mainAxisSpacing = 8.dp, // Espaço horizontal entre os cards
                crossAxisSpacing = 8.dp, // Espaço vertical entre as linhas
                modifier = Modifier.fillMaxWidth()
            ) {
                hqs.forEach { hq ->
                    // Exibe o card em cada célula da FlowRow
                    HQCard(
                        hq = hq,
                        db = db,
                        colecaoRevistas = colecaoRevistas,
                        context = context,
                        buscarHqs = ::buscarHqs,
                        onEditClick = { preencherCamposParaEdicao(hq) },
                        modifier = Modifier.width(150.dp) // Defina uma largura fixa para os cards
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtons(isEditMode: Boolean, onAddClick: () -> Unit, onClearClick: () -> Unit) {
    Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(if (isEditMode) "Salvar" else "Adicionar")
    }

    Button(onClick = onClearClick, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text("Limpar")
    }
}

@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    )
}

@Composable
fun HQCard(hq: Map<String, Any>, db: FirebaseFirestore, modifier: Modifier ,colecaoRevistas: String, context: Context, buscarHqs: () -> Unit, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(), // Garante que a Column ocupe toda a área disponível
        horizontalAlignment = Alignment.CenterHorizontally // Centraliza o conteúdo horizontalmente
    ) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .width(250.dp) // Largura do card
                .clip(MaterialTheme.shapes.small)
                .border(
                    width = 2.dp,
                    color = Color(0xFF313134),
                    shape = MaterialTheme.shapes.small
                )
                .shadow(8.dp, shape = MaterialTheme.shapes.small),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(10.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth() // Garante que a coluna ocupe toda a largura
            ) {
                val imagemUrl = hq["imagem"] as? String
                imagemUrl?.let {
                    Image(
                        painter = rememberImagePainter(it),
                        contentDescription = "Imagem da HQ",
                        modifier = Modifier
                            .width(200.dp) // Largura da imagem
                            .height(300.dp) // Altura da imagem
                            .clip(MaterialTheme.shapes.medium) // Borda mais curvada
                            .align(Alignment.CenterHorizontally) // Centralizando a imagem horizontalmente
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    "Título: ${hq["titulo"]}",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                Text("Autor: ${hq["autor"]}", style = TextStyle(fontSize = 16.sp))
                Text("Descrição: ${hq["descricao"]}", style = TextStyle(fontSize = 16.sp))
                Text("Preço: ${hq["preco"]}", style = TextStyle(fontSize = 16.sp))

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = {
                        deletarHQ(db, colecaoRevistas, hq["id"].toString(), context, buscarHqs)
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                    }
                }
            }
        }
    }
}

// Funções de banco de dados omitidas, como `addHQToDatabase`, `atualizarHQ`, e `deletarHQ`.


fun deletarHQ(
    db: FirebaseFirestore,
    colecaoRevistas: String,
    hqId: String,
    context: Context,
    buscarHqs: () -> Unit
) {
    // Deletando o documento da HQ no Firestore
    db.collection(colecaoRevistas)
        .document(hqId)
        .delete()
        .addOnSuccessListener {
            // Após a exclusão, buscar novamente as HQs
            buscarHqs()
            // Exibir mensagem de sucesso
            Toast.makeText(context, "HQ excluída com sucesso!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { exception ->
            // Exibir mensagem de erro caso falhe
            Toast.makeText(
                context,
                "Erro ao excluir HQ: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
}


fun addHQToDatabase(db: FirebaseFirestore, colecaoRevistas: String, hq: HashMap<String, String>, context: Context, onSuccess: () -> Unit) {
    db.collection(colecaoRevistas)
        .add(hq)
        .addOnSuccessListener {
            Toast.makeText(context, "HQ adicionada com sucesso!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener { exception ->
            Toast.makeText(context, "Erro ao adicionar HQ: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
}

fun atualizarHQ(
    db: FirebaseFirestore,
    colecaoRevistas: String,
    hq: HashMap<String, String>,
    hqEditando: Map<String, Any>,
    context: Context,
    onSuccess: () -> Unit
) {
    val id = hqEditando["id"] as String
    db.collection(colecaoRevistas).document(id)
        .update(hq as Map<String, Any>)
        .addOnSuccessListener {
            Toast.makeText(context, "HQ atualizada com sucesso!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener { exception ->
            Toast.makeText(context, "Erro ao atualizar HQ: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
}

