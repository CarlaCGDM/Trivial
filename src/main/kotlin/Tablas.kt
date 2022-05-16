import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object partidas : Table() {
    val nombre_usuario_pa = text("jugador").references(usuarios.nombre_usuario_us)
    val puntuacion_pa = integer("puntuación")
    //val: dia y hora
}

object usuarios: Table() {
    val nombre_usuario_us = text("Nombre de Usuario").uniqueIndex()
    val contrasenya_us = text("Contraseña")
}

/*
data class User (
    var nombre:String,
    var pass:String
) {

    companion object {

        fun fromRow(resultRow: ResultRow) = User(
            nombre= resultRow[usuarios.nombre_usuario_us],
            pass = resultRow[usuarios.contrasenya_us]
        )
    }
}
 */