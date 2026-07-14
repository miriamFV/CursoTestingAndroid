package com.example.cursotestingandroid.core.data.util

import com.example.cursotestingandroid.core.domain.util.Clock
import java.time.Instant
import javax.inject.Inject

class SystemClock @Inject constructor(): Clock {
    override fun now(): Instant = Instant.now()
}