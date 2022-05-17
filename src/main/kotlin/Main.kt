import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun main(args: Array<String>) {

    //Selección de preguntas:
    val listaDiccionarios = extraerPreguntas("preguntas_trivial.txt")
    val preguntasSeleccionadas = listaDiccionarios.shuffled().take(10)

    //Conexión a la base de datos:
    Database.connect("jdbc:sqlite:src/main/kotlin/database.db")
    transaction {
        SchemaUtils.create(usuarios)
        SchemaUtils.create(partidas)
    }

    val userName =  login()

    //Login del usuario:
    while (true) {

        //Partida:
        var puntos = 0
        for (pregunta in preguntasSeleccionadas) {
            println(pregunta["pregunta"])
            println("A)" + pregunta["opcion_1"])
            println("B)" + pregunta["opcion_2"])
            println("C)" + pregunta["opcion_3"])
            println("D)" + pregunta["opcion_4"])

            if (pregunta["respuesta"] == readLine().toString().uppercase()) {
                puntos++
                println("Respuesta correcta.")
            } else {
                println("Respuesta incorrecta. La respuesta era ${pregunta["respuesta"]}")
            }

        }
        println("Puntuacion: $puntos/10")

        //Guardar resultados:

        transaction {
            partidas.insert {
                it[nombre_usuario_pa] = "$userName"
                it[puntuacion_pa] = puntos
            }
        }

        println("Jugar de nuevo? (Y/n)")
        if ("Y" == readLine()) continue else break
    }

    //Imprimir ranking:

    println("Mejores Puntuaciones: \n ----------------------")
     transaction {
        val datos = partidas.selectAll().orderBy(partidas.puntuacion_pa, SortOrder.DESC).limit(5)
        datos.forEach {
            println(it[partidas.nombre_usuario_pa] + " " + it[partidas.puntuacion_pa])
        }
    }

    //Guardar en csv:


}

fun login():String {
    var userInput = ""
    while (true) {
        //¿Existe el usuario?
        println("Nombre de usuario: ")
        userInput = readLine().toString()
        var ocurrencias: Long = 0
        transaction {
            ocurrencias =
                usuarios.slice(usuarios.nombre_usuario_us).select { usuarios.nombre_usuario_us eq userInput }.count()
        }

        if (ocurrencias > 0) {
            //Si el usuario existe:
            println("El usuario existe. Contraseña: ")
            val pass = transaction {
                usuarios.select { usuarios.nombre_usuario_us eq userInput }.first()[usuarios.contrasenya_us]
            }
            println("El pass es $pass")
            if (readLine().toString() == pass) {
                println("Contraseña correcta.")
                return userInput
            } else {
                println("Contraseña incorrecta")
                continue
            }
        } else {
            //Si el suuario no existe:
            println("El usuario no existe. Creando usuario. Nueva contraseña: ")
            val pass = readLine().toString()
            transaction {
                usuarios.insert {
                    it[nombre_usuario_us] = userInput.padStart(30)
                    it[contrasenya_us] = "$pass"
                }
            }
            return userInput
        }
    }
}

fun extraerPreguntas(fichero:String): List<Map<String,String>> {
    val preguntas_raw = File("src/main/resources/$fichero").readLines()
    var listaDiccionarios = mutableListOf<Map<String,String>>()

    var i = 0
    var diccionario = mutableMapOf<String,String>()
    for (linea in preguntas_raw) {
        when (i) {
            0 -> diccionario.put("pregunta",linea)
            1 -> diccionario.put("respuesta",linea)
            2 -> diccionario.put("opcion_1",linea)
            3 -> diccionario.put("opcion_2",linea)
            4 -> diccionario.put("opcion_3",linea)
            5 -> diccionario.put("opcion_4",linea)
        }
        if (i == 5) {
            i=0
            listaDiccionarios.add(diccionario.toMap())
            diccionario.clear()
        } else i++
    }

    return listaDiccionarios
}

