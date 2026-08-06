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
            text = "A Ada usa dados do Health Connect apenas para mostrar seu resumo de saude, como passos, sono e batimentos cardíacos. Esses dados não substituem avaliação médica e só são lidos quando voce concede permissão."
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
