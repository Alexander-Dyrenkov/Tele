package ru.osp.cnn.core

class FailedToDisableServiceException : RuntimeException {
    constructor()
    constructor(s: String?) : super(s)
    constructor(s: String?, throwable: Throwable?) : super(s, throwable)
    constructor(throwable: Throwable?) : super(throwable)
}