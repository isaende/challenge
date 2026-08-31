package dao

import factory.ConnectionFactory
import model.AdaExames

class AdaExamesDAO {
    fun inserir(item:AdaExames):AdaExames=ConnectionFactory.transaction { con ->
        val id=if(item.id>0)item.id else ConnectionFactory.proximoId(con,"ADA_EXAMES")
        con.prepareStatement("INSERT INTO ADA_EXAMES (id,nm_exame,dt_exame,tipoMime,nm_arquivo,armazenamento,checksum,enviadoEm,ADA_PERFIL_id) VALUES (?,?,?,?,?,?,?,SYSTIMESTAMP,?)").use { ps -> ps.setLong(1,id);ps.setString(2,item.nomeExame);ps.setDate(3,java.sql.Date.valueOf(item.dataExame));ps.setString(4,item.tipoMime);ps.setString(5,item.nomeArquivo);ps.setString(6,item.armazenamento);ps.setString(7,item.checksum);ps.setLong(8,item.perfilId);ps.executeUpdate() };item.copy(id=id)
    }
    fun buscarPorId(id:Long):AdaExames?=ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_EXAMES WHERE id=?").use { ps -> ps.setLong(1,id);ps.executeQuery().use { rs -> if(rs.next())mapear(rs) else null } } }
    fun listarPorPerfil(perfilId:Long): List<AdaExames> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_EXAMES WHERE ADA_PERFIL_id=? ORDER BY enviadoEm DESC").use { ps -> ps.setLong(1,perfilId);ps.executeQuery().use { rs -> buildList { while(rs.next())add(mapear(rs)) } } } }
    private fun mapear(rs:java.sql.ResultSet)=AdaExames(rs.getLong("id"),rs.getString("nm_exame"),rs.data("dt_exame"),rs.getString("tipoMime"),rs.getString("nm_arquivo"),rs.getString("armazenamento"),rs.getString("checksum"),rs.instante("enviadoEm"),rs.getLong("ADA_PERFIL_id"))
}
