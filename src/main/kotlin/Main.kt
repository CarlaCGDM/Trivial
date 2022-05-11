import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object partidas : Table() {
    val jugador = text("jugador")
    val puntuacion = integer("puntuación")
}

fun main(args: Array<String>) {
    val preguntas_raw = File("src/main/resources/preguntas_trivial.txt").readLines()
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

    Database.connect("jdbc:sqlite:src/main/kotlin/database.db")
    transaction {
        SchemaUtils.create(partidas)
    }

    while (true) {
        var preguntas_seleccionadas = listaDiccionarios.shuffled().take(10)

        println("Nombre de usuario: ")
        var nombre_usuario = readLine()
        println("Bienvenido, $nombre_usuario.")

        var puntos = 0
        for (pregunta in preguntas_seleccionadas) {
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
                it[jugador] = "$nombre_usuario"
                it[puntuacion] = puntos
            }
        }

        println("Jugar de nuevo? (Y/n)")
        if ("Y" == readLine()) continue else break
    }

    println("Tabla de puntuaciones: ")
    transaction {
        val datos = partidas.selectAll().orderBy(partidas.puntuacion).limit(5)
        datos.forEach {
            println(it[partidas.jugador] + " " + it[partidas.puntuacion])
        }
    }

}