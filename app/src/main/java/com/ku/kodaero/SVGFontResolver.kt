package com.ku.kodaero

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.caverock.androidsvg.SVGExternalFileResolver

class SVGFontResolver(private val context: Context) : SVGExternalFileResolver() {

    override fun resolveFont(fontFamily: String?, fontWeight: Int, fontStyle: String?): Typeface? {
        return when (fontFamily) {
            "나눔스퀘어 네오" -> ResourcesCompat.getFont(context, R.font.nanum_square_neo)
            "Pretendard" -> ResourcesCompat.getFont(context, R.font.pretendard_semibold)
            else -> super.resolveFont(fontFamily, fontWeight, fontStyle)
        }
    }
}
