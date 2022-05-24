import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun main(args: Array<String>) {

    //Selección de 10 preguntas:
    val listaDiccionarios = extraerPreguntas("preguntas_trivial.txt")
    val preguntasSeleccionadas = listaDiccionarios.shuffled().take(10)

    //Conexión a la base de datos:
    Database.connect("jdbc:sqlite:src/main/resources/database.db")
    transaction {
        SchemaUtils.create(usuarios)
        SchemaUtils.create(partidas)
    }

    //Banner:
    println("""
       ***************************************************************
       *       ████████ ██████  ██ ██    ██ ██  █████  ██            *
       *          ██    ██   ██ ██ ██    ██ ██ ██   ██ ██            *
       *          ██    ██████  ██ ██    ██ ██ ███████ ██            *
       *          ██    ██   ██ ██  ██  ██  ██ ██   ██ ██            *  
       *          ██    ██   ██ ██   ████   ██ ██   ██ ███████       *           
       ***************************************************************                         
                                                                                                                                                                                     
    """.trimIndent())

    //Login del usuario:
    val jugador =  login()
    println("---------------------------------------------------------------")
    println("                    Bienvenid@, $jugador                       ")
    println("---------------------------------------------------------------")

    //Bucle del juego:
    while (true) {

        var puntos = 0

        //Por cada pregunta...
        for (pregunta in preguntasSeleccionadas) {
            println(pregunta["pregunta"])
            println("A)" + pregunta["opcion_a"])
            println("B)" + pregunta["opcion_b"])
            println("C)" + pregunta["opcion_c"])
            println("D)" + pregunta["opcion_d"])
            val respuestaUsusario = readLine().toString().uppercase()
            val respuestaCorrecta = pregunta["respuesta"]

            if (respuestaUsusario == respuestaCorrecta) {
                puntos++
                println("¡Correcto!")
            } else println("Fallaste. La respuesta era $respuestaCorrecta")

        }
        println("Puntuación final: $puntos/10")

        //Guardar resultados en la BBDD:
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        insertarResultados(jugador,puntos,fecha)

        //Terminar o no la partida:
        println("Jugar de nuevo? (Y/n)")
        if ("Y" == readLine()) continue else break
    }

    //Imprimir ranking:
    imprimirRanking()

    //Exportar BBDD a fichero CSV:
    volcarCSV("database.csv")

}

fun insertarResultados(nombre:String,puntos:Int,fecha:String) {
    transaction {
        partidas.insert {
            it[nombre_usuario_pa] = nombre
            it[puntuacion_pa] = puntos
            it[fecha_pa] = fecha
        }
    }
}


fun volcarCSV(fichero: String = "database.csv"){
    //Crear fichero:
    val salida = File("src/main/resources/$fichero")
    salida.createNewFile()

    //Escribir claves en fichero:
    val clave_jugador = transaction { usuarios.nombre_usuario_us.name }
    var claves = "$clave_jugador,puntuación,fecha\n"
    salida.writeText(claves)

    //Escribir valores en fichero:
    transaction {
        partidas.selectAll().forEach {
                salida.appendText(it[partidas.nombre_usuario_pa] + "," + it[partidas.puntuacion_pa] + "," + it[partidas.fecha_pa] +"\n")
        }
    }
}

fun imprimirRanking() {
    //TODO: Tabla bonita
    println("Mejores Puntuaciones: \n ----------------------")
    transaction {
        val datos = partidas.selectAll().orderBy(partidas.puntuacion_pa, SortOrder.DESC).limit(5)
        datos.forEach {
            println(it[partidas.nombre_usuario_pa] + " " + it[partidas.puntuacion_pa] + " " + it[partidas.fecha_pa])
        }
    }
}

fun login():String {
    var userInput = ""
    while (true) {
        //¿Existe el usuario?
        print("Nombre de usuario: ")
        userInput = readLine().toString()
        val ocurrencias = transaction {
            usuarios.select { usuarios.nombre_usuario_us eq userInput }.count()
        }
        if (ocurrencias > 0) {
            //Si el usuario existe:
            print("Contraseña: ")
            val passUsuario = readLine()
            val passCorrecto = transaction {
                usuarios.select { usuarios.nombre_usuario_us eq userInput }.first()[usuarios.contrasenya_us]
            }
            if (passUsuario == passCorrecto) {
                return userInput
            } else {
                println("Contraseña incorrecta")
                continue
            }
        } else {
            //Si el suuario no existe:
            print("Creando nuevo usuario. Contraseña: ")
            val pass = readLine().toString()
            transaction {
                usuarios.insert {
                    it[nombre_usuario_us] = userInput
                    it[contrasenya_us] = pass
                }
            }
            return userInput
        }
    }
}

fun extraerPreguntas(fichero:String): List<Map<String,String>> {
    val preguntas_raw = File("src/main/resources/$fichero").readLines()
    val listaDiccionarios = mutableListOf<Map<String,String>>()

    var i = 0
    val diccionario = mutableMapOf<String,String>()
    for (linea in preguntas_raw) {
        when (i) {
            0 -> diccionario["pregunta"] = linea
            1 -> diccionario["respuesta"] = linea
            2 -> diccionario["opcion_a"] = linea
            3 -> diccionario["opcion_b"] = linea
            4 -> diccionario["opcion_c"] = linea
            5 -> diccionario["opcion_d"] = linea
        }
        if (i == 5) {
            i=0
            listaDiccionarios.add(diccionario.toMap())
            diccionario.clear()
        } else i++
    }

    return listaDiccionarios
}

