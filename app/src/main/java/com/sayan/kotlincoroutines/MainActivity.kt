package com.sayan.kotlincoroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

const val TAG: String = "Coroutines => "

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*//1: the problem
        normalDelay()*/

        /*//2: coroutines delay: same result as above normal case
        coroutinesDelay()*/

        /*//Coroutine delay with runBlocking dispacher default i.e. current thread
        coroutinesDelayWithDefaultDispatcher()*/

        /*//Understanding Coroutine delay with runBlocking dispacher default
        understandingCoroutinesDelayWithDefaultDispatcher()*/

        /*//Delay operation using launch global scope
        coroutinesDelayLaunchGlobal()*/

        /*//coroutine Delay local launch
        coroutinesDelayWithContext()*/

        coroutineDummyNetworkRequest()
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
     * Here 'runBlocking' will block & run in the current tread (MainThread)
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
     * but this will still block the main thread
     */
    private fun coroutinesDelayWithDefaultDispatcher() = runBlocking(Dispatchers.Default){
        Log.d(TAG, "start")
        Log.d(TAG, "In thread: ${Thread.currentThread().name}")
        /**
         * (C)
         * long running operation
         */
        coroutinesDelayFor3Sec()
        Log.d(TAG, "end")
    }

    //</editor-fold>

    //<editor-fold desc="Understanding runBlocking with dispatcher">
    /**
     * (D)
     * 'runBlocking' with Dispacher will run the blocking code in the mentioned thread,
     * but this will still block the main thread
     */
    private fun understandingCoroutinesDelayWithDefaultDispatcher(){
        Log.d(TAG, "start In thread: ${Thread.currentThread().name}")

        /**
         * The following block of codes will run on other than main thread but
         * the main thread is still waiting for this block of codes to be completed,
         * thus main thread is blocking
         */
        runBlocking(Dispatchers.Default) {
            Log.d(TAG, "run blocking: before delay In thread: ${Thread.currentThread().name}")

            /**
             * (C)
             * long running operation
             */
            coroutinesDelayFor3Sec()
            Log.d(TAG, "run blocking: after delay In thread: ${Thread.currentThread().name}")
        }
        // Outside of runBlocking to show that it's running in the blocked main thread
        Log.d(TAG, "end In thread: ${Thread.currentThread().name}")
        // It still runs only after the runBlocking is fully executed.
    }

    //</editor-fold>

    //<editor-fold desc="Running operation on another thread asynchronously">
    /**
     * (D)
     * Use GlobalScope launch : the block under launch will run async with the
     * current thread. If the Main thread execution finishes, the
     */
    private fun coroutinesDelayLaunchGlobal() {
        Log.d(TAG, "start In thread: ${Thread.currentThread().name}")
        /**
         * (C)
         * long running operation
         * Using async or launch on the instance of GlobalScope is highly discouraged.
         */
        GlobalScope.launch {
            Log.d(TAG, "GlobalScope.launch start In thread: ${Thread.currentThread().name}")
            coroutinesDelayFor3Sec()
            Log.d(TAG, "GlobalScope.launch end In thread: ${Thread.currentThread().name}")
        }
        Log.d(TAG, "end In thread: ${Thread.currentThread().name}")
    }

    //</editor-fold>

    //<editor-fold desc="local launch async">
    /**
     * (D)
     * Use local launch : the block under launch will run async with the
     * current thread.
     */
    private fun coroutinesDelayWithContext() = runBlocking{
        Log.d(TAG, "start In thread: ${Thread.currentThread().name}")
        /**
         * (C)
         * long running operation
         * Using async or launch on the instance of GlobalScope is highly discouraged.
         */
        launch {
            Log.d(TAG, "GlobalScope.launch start In thread: ${Thread.currentThread().name}")
            coroutinesDelayFor3Sec()
            Log.d(TAG, "GlobalScope.launch end In thread: ${Thread.currentThread().name}")
        }
        Log.d(TAG, "end In thread: ${Thread.currentThread().name}")
    }

    //</editor-fold>



    //<editor-fold desc="Coroutine use case for network call">
    private fun coroutineDummyNetworkRequest() {
        //Start suspend function calling here under CoroutineScope IO-thread
        CoroutineScope(IO).launch{
            startFakeAPIRequest()
        }
    }

    private suspend fun startFakeAPIRequest() {
        println("Coroutines: Loading start...")
        val resultAPI1 = callAPI1()
        println("Coroutines: API1 end")
        //Use this anywhere you want to operate on UI
        withContext(Main){
            Toast.makeText(baseContext, resultAPI1, Toast.LENGTH_LONG).show()
        }
        //call next API with the result of the previous API
        val resultAPI2 = callAPI2(resultAPI1)
        println("Coroutines: API2 end")
        withContext(Main){
            Toast.makeText(baseContext, resultAPI2, Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun callAPI1(): String{
        delay(3000)
        return "Result 1"
    }

    private suspend fun callAPI2(resultAPI1: String): String {
        delay(3000)
        return "Result 2"
    }
    //</editor-fold>
}
