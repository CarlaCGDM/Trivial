import java.io.File

fun main(args: Array<String>) {
    val preguntas_raw = File("src/main/resources/preguntas_trivial.txt").readLines()
    var preguntas_diccionarios = mutableListOf<Map<String,String>>()

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
            preguntas_diccionarios.add(diccionario.toMap())
            diccionario.clear()
        } else i++
    }

    var preguntas_seleccionadas = preguntas_diccionarios.shuffled().take(10)

    println("Nombre de usuario: ")
    var nombre_usuario = readLine()
    println("Bienvenido, $nombre_usuario.")

    var puntuacion = 0
    for (pregunta in preguntas_seleccionadas) {
        println(pregunta["pregunta"])
    }
}