package dao

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

internal fun ResultSet.data(coluna: String): String = getDate(coluna).toLocalDate().toString()
internal fun ResultSet.dataNula(coluna: String): String? = getDate(coluna)?.toLocalDate()?.toString()
internal fun ResultSet.instante(coluna: String): String? = getTimestamp(coluna)?.toLocalDateTime()?.toString()
internal fun PreparedStatement.setTextoNulo(indice: Int, valor: String?) =
    if (valor == null) setNull(indice, Types.VARCHAR) else setString(indice, valor)
internal fun PreparedStatement.setDataNula(indice: Int, valor: String?) =
    if (valor == null) setNull(indice, Types.DATE) else setDate(indice, java.sql.Date.valueOf(valor))
internal fun PreparedStatement.setTimestampNulo(indice: Int, valor: String?) =
    if (valor == null) setNull(indice, Types.TIMESTAMP)
    else setTimestamp(indice, java.sql.Timestamp.valueOf(valor.replace('T', ' ')))
