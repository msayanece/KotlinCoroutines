package com.sayan.kotlincoroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

const val TAG: String = "Coroutines => "

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*//1: the problem
        normalDelay()*/

        /*//2: coroutines delay: same result as above normal case
        coroutinesDelay()*/

        //Coroutine delay with runBlocking dispacher default i.e. current thread
        coroutinesDelayWithDefaultDispacher()
    }

    //<editor-fold desc="normal delay using thread sleep">
    private fun normalDelay() {
        Log.d(TAG, "start")
        delayFor3Sec()
        Log.d(TAG, "end")
    }

    private fun delayFor3Sec() {
        /**
         * Make the current thread go to sleep for 3 sec
         */
        Thread.sleep(3000)
        /**
         * this log will wait for the above line execution
         */
        Log.d(TAG, "delayed job")
    }
    //</editor-fold>


    //<editor-fold desc="the delay: long running operation">
    /**
     * (B)
     * make this function suspend for calling delay function
     * We will use this same delay for representing long running operation in future
     */
    private suspend fun coroutinesDelayFor3Sec() {
        /**
         * (A)
         * suspend function delay: either could be called from a coroutines scope
         * or another suspend function
         */
        delay(3000)
        Log.d(TAG, "delayed job")
    }
    //</editor-fold>

    //<editor-fold desc="coroutines delay works same as normal delay">
    /**
     * (D)
     * 'runBlocking' will convert this function block into CoroutineScope so that
     * we could use suspend function here.
     * Here 'runBlocking' will block the current tread (MainThread)
     */
    private fun coroutinesDelay() = runBlocking{
        Log.d(TAG, "start")
        /**
         * (C)
         * We may declare this function also as suspend, but that will propagate farther upwards to
         * the onCreate() method. So we need to stop propagating this 'suspend' thing from one
         * function. Therefor, we will declare this coroutinesDelay() function as coroutines scope
         */
        coroutinesDelayFor3Sec()
        Log.d(TAG, "end")
    }
    //</editor-fold>


    //<editor-fold desc="Running on another thread but still blocking the main thread">
    /**
     * (D)
     * 'runBlocking' with Dispacher will run the blocking code in the mentioned thread,
     * by default it is the current thread
     */
    private fun coroutinesDelayWithDefaultDispacher() = runBlocking(Dispatchers.Default){
        Log.d(TAG, "start")
        Log.d(TAG, "In thread: ${Thread.currentThread().name}")
        /**
         * (C)
         * We may declare this function also as suspend, but that will propagate farther upwards to
         * the onCreate() method. So we need to stop propagating this 'suspend' thing from one
         * function. Therefor, we will declare this coroutinesDelay() function as coroutines scope
         */
        coroutinesDelayFor3Sec()
        Log.d(TAG, "end")
    }

    //</editor-fold>
}
