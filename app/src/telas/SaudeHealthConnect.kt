package home_page

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

// ? significa que esses valores podem ser nulos
data class DadosSaude(
    val passos: Long? = null,
    val sonoMinutos: Long? = null,
    val batimentos: Int? = null,
    val pressaoSistolica: Double? = null,
    val pressaoDiastolica: Double? = null
) {
    val caloriasGastas: Double? get() = passos?.times(0.4)

    fun complementarCom(salvos: DadosSaude) = DadosSaude(
        passos = passos ?: salvos.passos,
        sonoMinutos = sonoMinutos ?: salvos.sonoMinutos,
        batimentos = batimentos ?: salvos.batimentos,
        pressaoSistolica = pressaoSistolica ?: salvos.pressaoSistolica,
        pressaoDiastolica = pressaoDiastolica ?: salvos.pressaoDiastolica
    )

    fun temDados() = passos != null || sonoMinutos != null || batimentos != null ||
        pressaoSistolica != null || pressaoDiastolica != null

}

class RepositorioSaudeHealthConnect(
    private val context: Context
) {
    // otimização, so cria o client quando for usar
    private val cliente: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }
    // pergunta ao celular se o pacote do Health Connect está instalado e qual a versão dele.
    fun statusSdk(): Int {
        return HealthConnectClient.getSdkStatus(context)
    }

    // verifica se o healthconnect esta disponivel
    fun healthConnectDisponivel(): Boolean {
        return statusSdk() == HealthConnectClient.SDK_AVAILABLE
    }

    // verifica se tem permissoes
    suspend fun temPermissoes(): Boolean {
        return cliente.permissionController
            .getGrantedPermissions()
            .containsAll(PERMISSOES)
    }

    suspend fun lerDadosRecentes(): LeituraSaude {
        val agora = Instant.now()
        val inicioHoje = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
        val ultimas24Horas = agora.minus(24, ChronoUnit.HOURS)
        val permissoes = cliente.permissionController.getGrantedPermissions()
        val falhas = mutableSetOf<String>()
        val pendentes = PERMISSOES - permissoes

        suspend fun <T> ler(permissao: String, nome: String, bloco: suspend () -> T?): T? {
            if (permissao !in permissoes) return null
            return lerIndicadorSaude(bloco) { erro ->
                falhas.add(nome)
                Log.w("SaudeHealthConnect", "Falha ao ler $nome: ${erro.javaClass.simpleName}")
            }
        }

        val batimentos = ler(HealthPermission.getReadPermission(HeartRateRecord::class), "batimentos") {
            lerUltimoBatimento(inicioHoje, agora)
        }
        val passos = ler(HealthPermission.getReadPermission(StepsRecord::class), "passos") {
            lerPassos(inicioHoje, agora)
        }
        val sono = ler(HealthPermission.getReadPermission(SleepSessionRecord::class), "sono") {
            lerSonoEmMinutos(ultimas24Horas, agora)
        }
        val pressao = ler(HealthPermission.getReadPermission(BloodPressureRecord::class), "pressao") {
            cliente.readRecords(
                ReadRecordsRequest(
                    recordType = BloodPressureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(inicioHoje, agora),
                    ascendingOrder = false,
                    pageSize = 1
                )
            ).records.firstOrNull()
        }
        Log.d("SaudeHealthConnect", "Leitura de hoje: batimentosPresentes=${batimentos != null}, " +
            "pressaoPresente=${pressao != null}, falhas=${falhas.joinToString()}")
        return LeituraSaude(
            dados = DadosSaude(passos, sono, batimentos,
                pressao?.systolic?.inMillimetersOfMercury,
                pressao?.diastolic?.inMillimetersOfMercury),
            falhas = falhas,
            permissoesPendentes = pendentes,
            permissaoBatimentos = HealthPermission.getReadPermission(HeartRateRecord::class) in permissoes,
            permissaoPressao = HealthPermission.getReadPermission(BloodPressureRecord::class) in permissoes
        )
    }

    private suspend fun lerPassos(inicio: Instant, fim: Instant): Long? {
        val resposta = cliente.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(inicio, fim)
            )
        )
        return resposta[StepsRecord.COUNT_TOTAL]
    }

    private suspend fun lerSonoEmMinutos(inicio: Instant, fim: Instant): Long? {
        val resposta = cliente.aggregate(
            AggregateRequest(
                metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(inicio, fim)
            )
        )

        return resposta[SleepSessionRecord.SLEEP_DURATION_TOTAL]?.toMinutes()
    }

    private suspend fun lerUltimoBatimento(inicio: Instant, fim: Instant): Int? {
        var pagina: String? = null
        var ultimaAmostra: HeartRateRecord.Sample? = null
        do {
            val resposta = cliente.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(inicio, fim),
                    pageToken = pagina
                )
            )
            resposta.records.forEach { registro ->
                registro.samples.forEach { amostra ->
                    if (!amostra.time.isBefore(inicio) && amostra.time.isBefore(fim) &&
                        (ultimaAmostra == null || amostra.time.isAfter(ultimaAmostra!!.time))) {
                        ultimaAmostra = amostra
                    }
                }
            }
            pagina = resposta.pageToken
        } while (!pagina.isNullOrEmpty())
        return ultimaAmostra?.beatsPerMinute?.toInt()
    }

    // permissoes necessárias para acessar os dados
    companion object {
        val PERMISSOES = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class)
        )
    }
}

// Uma falha em outro indicador nao deve ocultar as medicoes disponiveis.
internal suspend fun <T> lerIndicadorSaude(
    ler: suspend () -> T?,
    aoFalhar: (Exception) -> Unit
): T? = try {
    ler()
} catch (cancelamento: CancellationException) {
    throw cancelamento
} catch (erro: Exception) {
    aoFalhar(erro)
    null
}

data class LeituraSaude(
    val dados: DadosSaude,
    val falhas: Set<String>,
    val permissoesPendentes: Set<String>,
    val permissaoBatimentos: Boolean,
    val permissaoPressao: Boolean = true
) {
    fun statusBatimentos(): String? = when {
        !permissaoBatimentos -> "Autorize a leitura de batimentos no Health Connect"
        "batimentos" in falhas -> "Nao foi possivel ler os batimentos do Health Connect"
        dados.batimentos == null -> "Sem batimentos registrados hoje no Health Connect"
        else -> null
    }
}

fun LeituraSaude.statusSinaisVitais(): String? {
    val pressao = when {
        !permissaoPressao -> "Autorize a leitura de pressao no Health Connect"
        "pressao" in falhas -> "Nao foi possivel ler a pressao do Health Connect"
        dados.pressaoSistolica == null || dados.pressaoDiastolica == null ->
            "Sem pressao registrada hoje no Health Connect"
        else -> null
    }
    return listOfNotNull(statusBatimentos(), pressao).takeIf { it.isNotEmpty() }?.joinToString(". ")
}
