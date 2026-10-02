package com.example.meuapproteiro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument


data class Cliente(val id: Int, val nome: String, val email: String, val telefone: String)
data class Produto(val id: Int, val titulo: String, val preco: String, val categoria: String)

class AppViewModel : ViewModel() {
    var listaClientes = mutableStateListOf<Cliente>(
        Cliente(1, "Lucas Silva", "lucas@email.com", "34 9999-1111"),
        Cliente(2, "Mariana Souza", "mari@email.com", "34 9888-2222")
    )
    private var nextClienteId = 3

    var listaProdutos = mutableStateListOf<Produto>(
        Produto(1, "Teclado Mecanico", "250.00", "Perifericos"),
        Produto(2, "Mouse Sem Fio", "120.00", "Acessorios")
    )
    private var nextProdutoId = 3


    fun salvarCliente(id: Int?, nome: String, email: String, tel: String) {
        if (id == null || id == 0) {
            listaClientes.add(Cliente(nextClienteId++, nome, email, tel))
        } else {
            val index = listaClientes.indexOfFirst { it.id == id }
            if (index != -1) {
                listaClientes[index] = Cliente(id, nome, email, tel)
            }
        }
    }

    fun salvarProduto(id: Int?, titulo: String, preco: String, cat: String) {
        if (id == null || id == 0) {
            listaProdutos.add(Produto(nextProdutoId++, titulo, preco, cat))
        } else {
            val index = listaProdutos.indexOfFirst { it.id == id }
            if (index != -1) {
                listaProdutos[index] = Produto(id, titulo, preco, cat)
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavegacao()
            }
        }
    }
}


@Composable
fun AppNavegacao(vm: AppViewModel = remember { AppViewModel() }) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (rotaAtual == "clientes" || rotaAtual == "produtos") {
                NavigationBar {
                    NavigationBarItem(
                        selected = rotaAtual == "clientes",
                        onClick = { navController.navigate("clientes") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Clinetes") }
                    )
                    NavigationBarItem(
                        selected = rotaAtual == "produtos",
                        onClick = { navController.navigate("produtos") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Produdos") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "clientes",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("clientes") {
                TelaListaClientes(
                    clientes = vm.listaClientes,
                    onAdd = { navController.navigate("form_cliente/0") },
                    onEdit = { id -> navController.navigate("form_cliente/$id") }
                )
            }

            composable("produtos") {
                TelaListaProdutos(
                    produtos = vm.listaProdutos,
                    onAdd = { navController.navigate("form_produto/0") },
                    onEdit = { id -> navController.navigate("form_produto/$id") }
                )
            }


            composable(
                route = "form_cliente/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStack ->
                val id = backStack.arguments?.getInt("id") ?: 0
                val cliente = vm.listaClientes.find { it.id == id }
                TelaFormCliente(
                    cliente = cliente,
                    onSalvar = { n, e, t ->
                        vm.salvarCliente(if (id == 0) null else id, n, e, t)
                        navController.popBackStack()
                    },
                    onCancelar = { navController.popBackStack() }
                )
            }

            composable(
                route = "form_produto/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStack ->
                val id = backStack.arguments?.getInt("id") ?: 0
                val produto = vm.listaProdutos.find { it.id == id }
                TelaFormProduto(
                    produto = produto,
                    onSalvar = { t, p, c ->
                        vm.salvarProduto(if (id == 0) null else id, t, p, c)
                        navController.popBackStack()
                    },
                    onCancelar = { navController.popBackStack() }
                )
            }
        }
    }
}


@Composable
fun TelaListaClientes(
    clientes: List<Cliente>,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Novo")
            }
        }
    ) { p ->
        Column(modifier = Modifier.padding(p).fillMaxSize().padding(16.dp)) {
            Text("Listage de Clientes", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn {
                items(clientes) { c ->
                    CardClientePersonalizado(cliente = c, onClick = { onEdit(c.id) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TelaListaProdutos(
    produtos: List<Produto>,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Novo")
            }
        }
    ) { p ->
        Column(modifier = Modifier.padding(p).fillMaxSize().padding(16.dp)) {
            Text("Listage de Produtos", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn {
                items(produtos) { prod ->
                    CardProdutoPersonalizado(produto = prod, onClick = { onEdit(prod.id) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


@Composable
fun CardClientePersonalizado(cliente: Cliente, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "Nome: ${cliente.nome}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Email: ${cliente.email}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Telefone: ${cliente.telefone}", style = MaterialTheme.typography.bodySmall)
        }
    }
}


@Composable
fun CardProdutoPersonalizado(produto: Produto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = produto.titulo, style = MaterialTheme.typography.titleMedium)
            Text(text = "Preco: R$ ${produto.preco}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Categoria: ${produto.categoria}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun TelaFormCliente(
    cliente: Cliente?,
    onSalvar: (String, String, String) -> Unit,
    onCancelar: () -> Unit
) {
    var nome by remember { mutableStateOf(cliente?.nome ?: "") }
    var email by remember { mutableStateOf(cliente?.email ?: "") }
    var telefone by remember { mutableStateOf(cliente?.telefone ?: "") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = if (cliente == null) "Cadastra Novo Cliente" else "Edita Cliente",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome do clinte") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Emal") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = telefone,
            onValueChange = { telefone = it },
            label = { Text("Numero de telefone") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onCancelar) {
                Text("Cancela")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = {
                    if (nome.isNotEmpty()) {
                        onSalvar(nome, email, telefone)
                    }
                }
            ) {
                Text("Salva")
            }
        }
    }
}


@Composable
fun TelaFormProduto(
    produto: Produto?,
    onSalvar: (String, String, String) -> Unit,
    onCancelar: () -> Unit
) {
    var titulo by remember { mutableStateOf(produto?.titulo ?: "") }
    var preco by remember { mutableStateOf(produto?.preco ?: "") }
    var categoria by remember { mutableStateOf(produto?.categoria ?: "") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = if (produto == null) "Cadastra Novo Produdo" else "Edita Produdo",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Nome do produdo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = preco,
            onValueChange = { preco = it },
            label = { Text("Valor") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = categoria,
            onValueChange = { categoria = it },
            label = { Text("Categora") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onCancelar) {
                Text("Cancela")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = {
                    if (titulo.isNotEmpty()) {
                        onSalvar(titulo, preco, categoria)
                    }
                }
            ) {
                Text("Salva")
            }
        }
    }
}