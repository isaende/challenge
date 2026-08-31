package api

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SegurancaSenha {
    private const val ITERACOES = 120_000
    private const val TAMANHO_CHAVE = 256

    fun gerarHash(senha: String): String {
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val hash = derivar(senha, salt, ITERACOES)
        return "pbkdf2$$ITERACOES$${Base64.getEncoder().encodeToString(salt)}$${Base64.getEncoder().encodeToString(hash)}"
    }

    fun verificar(senha: String, armazenado: String): Boolean = runCatching {
        val partes = armazenado.split('$')
        require(partes.size == 4 && partes[0] == "pbkdf2")
        val esperado = Base64.getDecoder().decode(partes[3])
        java.security.MessageDigest.isEqual(
            esperado,
            derivar(senha, Base64.getDecoder().decode(partes[2]), partes[1].toInt())
        )
    }.getOrDefault(false)

    private fun derivar(senha: String, salt: ByteArray, iteracoes: Int): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(senha.toCharArray(), salt, iteracoes, TAMANHO_CHAVE))
            .encoded
}
