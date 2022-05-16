import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.match
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun main(args: Array<String>) {

    //Selección de preguntas:
    val listaDiccionarios = extraerPreguntas("preguntas_trivial.txt")
    val preguntasSeleccionadas = listaDiccionarios.shuffled().take(5)

    //Conexión a la base de datos:

    Database.connect("jdbc:sqlite:src/main/kotlin/database.db")
    transaction {
        SchemaUtils.create(usuarios)
        SchemaUtils.create(partidas)
    }

    //Login del usuario:

    while (true) {
        println("Nombre de usuario: ")

        //¿Existe el usuario?
        var userInput = readLine().toString()
        var ocurrencias:Long = 0
        transaction {
            ocurrencias = usuarios.slice(usuarios.nombre_usuario_us).select { usuarios.nombre_usuario_us eq userInput }.count()
        }

        if (ocurrencias > 0) {
                //Si el usuario existe:
                println("El usuario existe. Contraseña: ")
                val pass  = transaction {
                    usuarios.select { usuarios.nombre_usuario_us eq userInput }.first()[usuarios.contrasenya_us]
                }
                 println("El pass es $pass")
                if (readLine().toString() == pass) {
                    println("Contraseña correcta.")

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
                        it[nombre_usuario_us] = "$userInput"
                        it[contrasenya_us] = "$pass"
                    }
                }
        }

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

        transaction {
            partidas.insert {
                it[nombre_usuario_pa] = "$userInput"
                it[puntuacion_pa] = puntos
            }
        }

        println("Jugar de nuevo? (Y/n)")
        if ("Y" == readLine()) continue else break
    }

    println("Tabla de puntuaciones: \n ----------------------")
     transaction {
        val datos = partidas.selectAll().orderBy(partidas.puntuacion_pa, SortOrder.DESC).limit(5)
        datos.forEach {
            println(it[partidas.nombre_usuario_pa] + " " + it[partidas.puntuacion_pa])
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