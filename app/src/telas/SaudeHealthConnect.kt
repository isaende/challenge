package home_page

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

// ? significa que esses valores podem ser nulos
data class DadosSaude(
    val passos: Long? = null,
    val sonoMinutos: Long? = null,
    val batimentos: Int? = null
)

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

    suspend fun lerDadosRecentes(): DadosSaude {
        val agora = Instant.now()
        val inicioHoje = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
        val ultimas24Horas = agora.minus(24, ChronoUnit.HOURS)

        return DadosSaude(
            passos = lerPassos(inicioHoje, agora),
            sonoMinutos = lerSonoEmMinutos(ultimas24Horas, agora),
            batimentos = lerUltimoBatimento(inicioHoje, agora)
        )
    }

    private suspend fun lerPassos(inicio: Instant, fim: Instant): Long {
        val resposta = cliente.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(inicio, fim)
            )
        )
        return resposta[StepsRecord.COUNT_TOTAL] ?: 0L
    }

    private suspend fun lerSonoEmMinutos(inicio: Instant, fim: Instant): Long {
        val resposta = cliente.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(inicio, fim)
            )
        )

        return resposta.records.sumOf { sessao ->
            val inicioSessao = maxOf(sessao.startTime, inicio)
            val fimSessao = minOf(sessao.endTime, fim)
            Duration.between(inicioSessao, fimSessao).toMinutes().coerceAtLeast(0)
        }
    }

    private suspend fun lerUltimoBatimento(inicio: Instant, fim: Instant): Int? {
        val resposta = cliente.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(inicio, fim)
            )
        )

        return resposta.records
            .flatMap { it.samples }
            .maxByOrNull { it.time }
            ?.beatsPerMinute
            ?.toInt()
    }

    // permissoes necessárias para acessar os dados
    companion object {
        val PERMISSOES = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class)
        )
    }
}
