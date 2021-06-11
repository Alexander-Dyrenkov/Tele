package ru.osp.cnn.core.model

import com.github.michaelbull.result.*

fun <T> T.ok(): Ok<T> = Ok(this)

fun <T: Throwable> T.err(): Err<T> = Err(this)
