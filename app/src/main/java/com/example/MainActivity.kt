package com.example

import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.LiveData
import com.yubico.yubikit.YubiKitManager
import com.yubico.yubikit.exceptions.NoPermissionsException
import com.yubico.yubikit.piv.Algorithm
import com.yubico.yubikit.piv.PivApplication
import com.yubico.yubikit.piv.Slot
import com.yubico.yubikit.transport.usb.UsbConfiguration
import com.yubico.yubikit.transport.usb.UsbSession
import com.yubico.yubikit.transport.usb.UsbSessionListener
import com.yubico.yubikit.utils.StringUtils

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    var mgr : YubiKitManager? = null
    var curSession : UsbSession? = null

    val usbListener = object: UsbSessionListener {
        override fun onSessionReceived(session: UsbSession, hasPermission: Boolean) {
            if (hasPermission && null == curSession) {
                curSession = session
                Log.e(TAG, "onSessionReceived: UsbListener=" + System.identityHashCode(this));

                try {
                    val app = PivApplication(session)
                    val cert = app.getCertificate(Slot.SIGNATURE);
                    Log.e(TAG, "Certificate subject DN name: " + cert.subjectDN.name)
                    val input = ByteArray(1024 / 8)
                    input[0] = 1
                    input[1] = 2
                    input[2] = 3
                    app.verify("123456")
                    val output = app.sign(Slot.SIGNATURE, Algorithm.RSA1024, input)
                    Log.e(TAG, "Output: " + StringUtils.bytesToHex(output))
                } catch (e: Exception) {
                    Log.e(TAG, "Error calling Yubikey", e)
                }
            }
        }

        override fun onSessionRemoved(session: UsbSession) {
            if ( null != curSession ) {
                curSession = null
                Log.e(TAG, "onSessionRemoved: UsbListener=" + System.identityHashCode(this));
            }
        }

        override fun onRequestPermissionsResult(session: UsbSession, isGranted: Boolean) {
            onSessionReceived(session, isGranted)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        Log.e(TAG, "onCreate: MainActivity=" + System.identityHashCode(this));
        mgr = YubiKitManager(this)
        mgr?.startUsbDiscovery(UsbConfiguration(), usbListener);
    }

    // https://stackoverflow.com/questions/4299899/android-oncreate-getting-called-multiple-times-and-not-by-me/4300288
    // https://developer.android.com/guide/topics/resources/runtime-changes.html
    override fun onDestroy() {
        if (null != mgr) {
            mgr?.stopUsbDiscovery()
            Log.e(TAG, "onDestroy: MainActivity=" + System.identityHashCode(this));
        }

        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}