package ru.osp.cnn.core.dao

class DaoException : Exception {
    /**
     * Constructor.
     */
    constructor() {}

    /**
     * Constructor.
     * @param message exception's message
     */
    constructor(message: String?) : super(message) {}

    /**
     * Constructor.
     * @param cause cause exception
     */
    constructor(cause: Throwable?) : super(cause) {}

    /**
     * Constructor.
     * @param message exception's message
     * @param cause cause exception
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
}