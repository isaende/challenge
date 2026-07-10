package home_page

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.ScrollView
import android.widget.TextView

class PermissionsRationaleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val texto = TextView(this).apply {
            text = "A Ada usa dados do Health Connect apenas para mostrar seu resumo de saude, como passos, sono e batimentos cardiacos. Esses dados nao substituem avaliacao medica e so sao lidos quando voce concede permissao."
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
