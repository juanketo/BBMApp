package org.example.appbbmges.ui.productos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.food1
import appbbmges.composeapp.generated.resources.tiara
import appbbmges.composeapp.generated.resources.tutu
import org.example.appbbmges.navigation.SimpleNavController
import org.jetbrains.compose.resources.painterResource


data class Producto(
    val imagen: @Composable () -> Painter,
    val nombre: String,
    val precio: String,
    val descripcion: String
)

@Composable
fun ProductCard(
    producto: Producto,
    modifier: Modifier = Modifier
) {
    var cantidad by remember { mutableStateOf(0) }

    Card(
        modifier = modifier
            .padding(8.dp)
            .width(200.dp), // Ancho fijo para tarjetas compactas
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2E3B4E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen del producto
            Image(
                painter = producto.imagen(),
                contentDescription = "Imagen de ${producto.nombre}",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nombre del producto
            Text(
                text = producto.nombre,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Precio del producto
            Text(
                text = producto.precio,
                color = Color(0xFF00D4FF),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción del producto
            Text(
                text = producto.descripcion,
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Controles de cantidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de disminuir
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF00D4FF), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (cantidad > 0) cantidad--
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "-",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Cantidad
                Text(
                    text = cantidad.toString(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Botón de aumentar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF00D4FF), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            cantidad++
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "+",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComprarButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF00D4FF)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = "Carrito de compras",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "COMPRAR",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Composable principal para la pantalla de boletos
@Composable
fun ProductosScreen(navController: SimpleNavController) {
    val productos = listOf(
        Producto(
            imagen = { painterResource(Res.drawable.food1) },
            nombre = "CAMISETA",
            precio = "$19.99",
            descripcion = "Camiseta de algodón suave y cómoda"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.tiara) },
            nombre = "PANTALÓN",
            precio = "$39.99",
            descripcion = "Pantalón casual para cualquier ocasión"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.tutu) },
            nombre = "SNACK",
            precio = "$2.99",
            descripcion = "Snack crujiente y delicioso"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.food1) },
            nombre = "CHAQUETA",
            precio = "$49.99",
            descripcion = "Chaqueta ligera y resistente"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.tiara) },
            nombre = "SOMBRERO",
            precio = "$15.99",
            descripcion = "Sombrero elegante para toda ocasión"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.food1) },
            nombre = "CAMISETA",
            precio = "$19.99",
            descripcion = "Camiseta de algodón suave y cómoda"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.tiara) },
            nombre = "PANTALÓN",
            precio = "$39.99",
            descripcion = "Pantalón casual para cualquier ocasión"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.tutu) },
            nombre = "SNACK",
            precio = "$2.99",
            descripcion = "Snack crujiente y delicioso"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.food1) },
            nombre = "CHAQUETA",
            precio = "$49.99",
            descripcion = "Chaqueta ligera y resistente"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.tiara) },
            nombre = "SOMBRERO",
            precio = "$15.99",
            descripcion = "Sombrero elegante para toda ocasión"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.food1) },
            nombre = "CAMISETA",
            precio = "$19.99",
            descripcion = "Camiseta de algodón suave y cómoda"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.tiara) },
            nombre = "PANTALÓN",
            precio = "$39.99",
            descripcion = "Pantalón casual para cualquier ocasión"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.tutu) },
            nombre = "SNACK",
            precio = "$2.99",
            descripcion = "Snack crujiente y delicioso"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.food1) },
            nombre = "CHAQUETA",
            precio = "$49.99",
            descripcion = "Chaqueta ligera y resistente"
        ),
        Producto(
            imagen = { painterResource(Res.drawable.tiara) },
            nombre = "SOMBRERO",
            precio = "$15.99",
            descripcion = "Sombrero elegante para toda ocasión"
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Productos Disponibles",
            color = Color.Black,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Cambiado de LazyColumn a LazyVerticalGrid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(productos) { producto ->
                ProductCard(producto = producto)
            }
        }

        // Botón de compra con icono de carrito
        ComprarButton {
            // Acción al presionar el botón de comprar
        }
    }
}