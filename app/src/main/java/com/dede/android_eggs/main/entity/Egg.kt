package com.dede.android_eggs.main.entity

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.dede.android_eggs.R
import com.dede.android_eggs.ui.adapter.VType
import com.dede.android_eggs.ui.drawables.AlterableAdaptiveIconDrawable
import com.dede.android_eggs.util.append
import com.dede.android_eggs.views.settings.prefs.IconShapePref
import com.dede.basic.provider.EasterEgg
import com.dede.basic.provider.EasterEggGroup
import com.dede.basic.requireDrawable


fun EasterEggGroup.toEggGroup(): EggGroup {
    return EggGroup(eggs.map { it.toEgg() })
}

fun EasterEgg.toEgg(): Egg {
    val versionFormatter: Egg.VersionFormatter
    val versionCommentFormatter: Egg.VersionCommentFormatter
    val versionName1 = EasterEgg.getVersionNameByApiLevel(apiLevel.first)
    if (apiLevel.first == apiLevel.last) {
        versionFormatter = Egg.VersionFormatter(
            nicknameRes,
            versionName1
        )
        versionCommentFormatter = Egg.VersionCommentFormatter(
            apiLevel.first,
            versionName1
        )
    } else {
        val versionName2 = EasterEgg.getVersionNameByApiLevel(apiLevel.last)
        versionFormatter = Egg.VersionFormatter(
            nicknameRes,
            versionName1, versionName2,
        )
        versionCommentFormatter = Egg.VersionCommentFormatter(
            apiLevel.first, apiLevel.last,
            versionName1, versionName2
        )
    }
    return Egg(
        iconRes = iconRes,
        eggNameRes = nameRes,
        versionFormatter = versionFormatter,
        versionCommentFormatter = versionCommentFormatter,
        targetClass = provideEasterEgg(),
        supportAdaptiveIcon = supportAdaptiveIcon,
        id = id
    )
}

data class Egg(
    @DrawableRes val iconRes: Int,
    @StringRes val eggNameRes: Int,
    val versionFormatter: VersionFormatter,
    val versionCommentFormatter: VersionCommentFormatter,
    val targetClass: Class<out Activity>? = null,
    val supportAdaptiveIcon: Boolean = false,
    val id: Int = View.NO_ID,
    private val itemType: Int = VIEW_TYPE_EGG,
) : VType {

    class VersionFormatter(
        @StringRes val nicknameRes: Int,
        vararg versionNames: CharSequence,
    ) {

        private val versionNames: Array<out CharSequence> = versionNames

        fun format(context: Context): CharSequence {
            val enDash = context.getString(R.string.char_en_dash)
            val nickname = context.getString(nicknameRes)
            val sb = StringBuilder()
            versionNames.joinTo(sb, separator = enDash)
            return context.getString(
                R.string.android_version_nickname_format,
                sb.toString(), nickname
            )
        }
    }

    class VersionCommentFormatter(
        private val versionCode1: Int,
        private val versionCode2: Int,
        private val versionName1: CharSequence,
        private val versionName2: CharSequence,
    ) {

        fun getApiLevelRange(): IntRange {
            return versionCode1..versionCode2
        }

        constructor(versionCode: Int, versionName: CharSequence) :
                this(versionCode, versionCode, versionName, versionName)

        fun format(context: Context): CharSequence {
            val span = SpannableStringBuilder()
                .append(context.getString(R.string.android_version_format, versionName1))
            val italic = StyleSpan(Typeface.ITALIC)
            if (versionCode1 == versionCode2) {
                span.append("\n")
                    .append(
                        context.getString(R.string.api_version_format, versionCode1.toString()),
                        italic
                    )
            } else {
                val enDash = context.getString(R.string.char_en_dash)
                span.append(enDash)
                    .append(versionName2)
                    .append("\n")
                    .append(
                        context.getString(R.string.api_version_format, versionCode1.toString()),
                        italic,
                    )
                    .append(enDash, italic)
                    .append(versionCode2.toString(), italic)
            }
            return span
        }
    }

    companion object {
        const val VIEW_TYPE_EGG = 0
        const val VIEW_TYPE_WAVY = -1
        const val VIEW_TYPE_PREVIEW = 1
        const val VIEW_TYPE_EGG_GROUP = 2

        fun Egg.getIcon(context: Context): Drawable {
            if (supportAdaptiveIcon) {
                val pathStr = IconShapePref.getMaskPath(context)
                return AlterableAdaptiveIconDrawable(context, iconRes, pathStr)
            }
            return context.requireDrawable(iconRes)
        }
    }

    override val viewType: Int = itemType
}

class EggGroup(
    val child: List<Egg>,
    var selectedIndex: Int = 0,
) : VType {

    constructor(selectedIndex: Int, vararg child: Egg) : this(child.toList(), selectedIndex)

    val selectedEgg: Egg get() = child[selectedIndex]

    override val viewType: Int
        get() = Egg.VIEW_TYPE_EGG_GROUP
}

class Wavy(val wavyRes: Int, val repeat: Boolean = false) : VType {
    override val viewType: Int = Egg.VIEW_TYPE_WAVY
}
