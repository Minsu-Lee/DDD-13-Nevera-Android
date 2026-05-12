package com.anddd.nevera.core.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber

abstract class NeveraViewModel<
        STATE : NeveraState,
        SIDE_EFFECT : NeveraSideEffect,
        INTENT : NeveraIntent,
        MUTATION : NeveraMutation,
        >(initialState: STATE) : ViewModel(), ContainerHost<STATE, SIDE_EFFECT> {

    override val container = container<STATE, SIDE_EFFECT>(
        initialState = initialState,
        buildSettings = {
            exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                Timber.e(throwable)
            }
        },
    )

    abstract fun onIntent(action: INTENT)

    protected abstract suspend fun Syntax<STATE, SIDE_EFFECT>.onReduce(mutation: MUTATION)
}
