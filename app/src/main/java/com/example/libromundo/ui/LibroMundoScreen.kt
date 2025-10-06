package com.example.libromundo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.libromundo.model.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibroMundoScreen() {
    // Estados para los campos de entrada
    var titulo by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<CategoriaLibro?>(null) }

    // Estado del carrito
    var carrito by remember { mutableStateOf(listOf<Libro>()) }

    // Estados para cálculos
    var subtotalTotal by remember { mutableStateOf(0.0) }
    var descuentoMonto by remember { mutableStateOf(0.0) }
    var porcentajeDescuento by remember { mutableStateOf(0) }
    var totalFinal by remember { mutableStateOf(0.0) }
    var mostrarResumen by remember { mutableStateOf(false) }

    // Estados para AlertDialog
    var mostrarDialog by remember { mutableStateOf(false) }
    var tituloDialog by remember { mutableStateOf("") }
    var mensajeDialog by remember { mutableStateOf("") }
    var accionDialog by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Función para calcular descuento
    fun calcularDescuento(totalLibros: Int): Pair<Int, Color> {
        return when {
            totalLibros >= 11 -> 20 to Color(0xFFFFD700) // Dorado
            totalLibros >= 6 -> 15 to Color(0xFF2196F3) // Azul
            totalLibros >= 3 -> 10 to Color(0xFF4CAF50) // Verde
            else -> 0 to Color.Gray
        }
    }

    // Función para agregar libro
    fun agregarLibro() {
        when {
            titulo.isBlank() -> {
                tituloDialog = "Error"
                mensajeDialog = "Debe ingresar el título del libro"
                accionDialog = null
                mostrarDialog = true
            }
            precio.toDoubleOrNull() == null || precio.toDouble() <= 0 -> {
                tituloDialog = "Error"
                mensajeDialog = "El precio debe ser mayor a 0"
                accionDialog = null
                mostrarDialog = true
            }
            cantidad.toIntOrNull() == null || cantidad.toInt() <= 0 -> {
                tituloDialog = "Error"
                mensajeDialog = "La cantidad debe ser mayor a 0"
                accionDialog = null
                mostrarDialog = true
            }
            categoriaSeleccionada == null -> {
                tituloDialog = "Error"
                mensajeDialog = "Debe seleccionar una categoría"
                accionDialog = null
                mostrarDialog = true
            }
            else -> {
                val nuevoLibro = Libro(
                    titulo = titulo,
                    precio = precio.toDouble(),
                    cantidad = cantidad.toInt(),
                    categoria = categoriaSeleccionada!!
                )
                carrito = carrito + nuevoLibro

                // Limpiar campos pero mantener categoría
                titulo = ""
                precio = ""
                cantidad = ""

                // Mostrar Snackbar de éxito
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Libro agregado al carrito",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // Función para calcular total
    fun calcularTotal() {
        if (carrito.isEmpty()) {
            tituloDialog = "Error"
            mensajeDialog = "Debe haber al menos 1 libro en el carrito"
            accionDialog = null
            mostrarDialog = true
            return
        }

        // Calcular subtotal
        subtotalTotal = carrito.sumOf { it.calcularSubtotal() }

        // Calcular cantidad total de libros
        val totalLibros = carrito.sumOf { it.cantidad }

        // Obtener descuento
        val (porcentaje, color) = calcularDescuento(totalLibros)
        porcentajeDescuento = porcentaje

        // Calcular monto de descuento
        descuentoMonto = subtotalTotal * (porcentaje / 100.0)

        // Calcular total final
        totalFinal = subtotalTotal - descuentoMonto

        // Habilitar resumen
        mostrarResumen = true

        // Mostrar Snackbar según descuento
        val mensaje = when (porcentaje) {
            0 -> "No hay descuento aplicado"
            10 -> "¡Genial! Ahorraste S/. %.2f".format(descuentoMonto)
            15 -> "¡Excelente! Ahorraste S/. %.2f".format(descuentoMonto)
            20 -> "¡Increíble! Ahorraste S/. %.2f".format(descuentoMonto)
            else -> ""
        }

        scope.launch {
            snackbarHostState.showSnackbar(
                message = mensaje,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (mostrarResumen) {
                FloatingActionButton(
                    onClick = { /* Navegar a segunda pantalla */ }
                ) {
                    Icon(Icons.Default.Add, "Mostrar Resumen")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Título de la app
            Text(
                text = "LibroMundo",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Card con los campos de entrada
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Campo título
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título del Libro") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // RadioButtons para categoría
                    Text(
                        "Categoría:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    CategoriaLibro.values().forEach { categoria ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (categoriaSeleccionada == categoria),
                                    onClick = { categoriaSeleccionada = categoria }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (categoriaSeleccionada == categoria),
                                onClick = { categoriaSeleccionada = categoria }
                            )
                            Text(
                                text = categoria.displayName,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campos precio y cantidad en fila
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = precio,
                            onValueChange = { precio = it },
                            label = { Text("Precio (S/.)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = cantidad,
                            onValueChange = { cantidad = it },
                            label = { Text("Cantidad") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { agregarLibro() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ShoppingCart, null)
                    Spacer(Modifier.width(4.dp))
                    Text("AGREGAR")
                }

                Button(
                    onClick = { calcularTotal() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Done, null)
                    Spacer(Modifier.width(4.dp))
                    Text("CALCULAR")
                }

                Button(
                    onClick = {
                        tituloDialog = "Confirmar"
                        mensajeDialog = "¿Está seguro de limpiar el carrito?"
                        accionDialog = {
                            carrito = emptyList()
                            titulo = ""
                            precio = ""
                            cantidad = ""
                            categoriaSeleccionada = null
                            subtotalTotal = 0.0
                            descuentoMonto = 0.0
                            porcentajeDescuento = 0
                            totalFinal = 0.0
                            mostrarResumen = false
                            mostrarDialog = false

                            scope.launch {
                                snackbarHostState.showSnackbar("Carrito limpiado")
                            }
                        }
                        mostrarDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(4.dp))
                    Text("LIMPIAR")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista del carrito
            if (carrito.isNotEmpty()) {
                Text(
                    "Carrito (${carrito.size} libros)",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(carrito) { libro ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        libro.titulo,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "Categoría: ${libro.categoria.displayName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        "Precio: S/. %.2f | Cantidad: %d".format(
                                            libro.precio, libro.cantidad
                                        ),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "Subtotal: S/. %.2f".format(libro.calcularSubtotal()),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        tituloDialog = "Confirmar"
                                        mensajeDialog = "¿Eliminar '${libro.titulo}' del carrito?"
                                        accionDialog = {
                                            carrito = carrito.filter { it.id != libro.id }
                                            mostrarDialog = false

                                            // Recalcular si hay libros
                                            if (carrito.isNotEmpty() && mostrarResumen) {
                                                subtotalTotal = carrito.sumOf { it.calcularSubtotal() }
                                                val totalLibros = carrito.sumOf { it.cantidad }
                                                val (porcentaje, _) = calcularDescuento(totalLibros)
                                                porcentajeDescuento = porcentaje
                                                descuentoMonto = subtotalTotal * (porcentaje / 100.0)
                                                totalFinal = subtotalTotal - descuentoMonto
                                            } else {
                                                mostrarResumen = false
                                                subtotalTotal = 0.0
                                                descuentoMonto = 0.0
                                                porcentajeDescuento = 0
                                                totalFinal = 0.0
                                            }

                                            scope.launch {
                                                snackbarHostState.showSnackbar("Libro eliminado del carrito")
                                            }
                                        }
                                        mostrarDialog = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Resumen de compra
            if (mostrarResumen) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "RESUMEN DE COMPRA",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Subtotal: S/. %.2f".format(subtotalTotal))
                        Text("Descuento (%d%%): - S/. %.2f".format(
                            porcentajeDescuento, descuentoMonto
                        ))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            "TOTAL A PAGAR: S/. %.2f".format(totalFinal),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // AlertDialog
        if (mostrarDialog) {
            AlertDialog(
                onDismissRequest = { mostrarDialog = false },
                title = { Text(tituloDialog) },
                text = { Text(mensajeDialog) },
                confirmButton = {
                    if (accionDialog != null) {
                        TextButton(onClick = { accionDialog?.invoke() }) {
                            Text("Confirmar")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialog = false }) {
                        Text(if (accionDialog != null) "Cancelar" else "Aceptar")
                    }
                }
            )
        }
    }
}
