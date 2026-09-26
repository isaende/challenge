// Explicação do porque os dados são usados e a importancia de usar eles

package home_page

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.ScrollView
import android.widget.TextView

class Permissão : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val texto = TextView(this).apply {
            text = "A Ada lê passos, sono, frequência cardíaca e pressão arterial do Health Connect com sua permissão para exibir e salvar seu monitoramento no aplicativo. As calorias são estimadas a partir dos passos. Esses dados não substituem avaliação médica."
            textSize = 18f
            setPadding(48, 48, 48, 48)
            gravity = Gravity.START
        }

        setContentView(
            ScrollView(this).apply {
                addView(texto)
            }
        )
    }
}
