# yubi-piv-android
A demo Android app supporting USB Yubikey and use Yubikey PIV for RSA sign/decrypt

This app is a demo app to show how to make a USB connection to Yubikey and then use the inserted Yubikey to sign/decrypt message (signature verification and message encryption can be down by using public key, anyone can do it.).

# About Yubikey

There are multiple *Applications* inside the Yubikey. For example, GPG, FIDO, OTP, PIV etc. PIV is the one that provides pretty low level RSA operation. In fact, we can use GPG as well, but I don't see any GPG documentation or code sample from Yubikey.

Inside in Yubikey PIV, they have the concept of

1. Slots: basically 4 slots (+ some retired slots), each slot has it's own public/private key and/or certificate
2. PIN/PUK: need PIN to use the private key, 3 failure, PIN locked, then can use PUK (admin key) to unlock. There is another management key that can be used to reset the whole key

# Program Flow

1. Create `YubiKitManager` and `startUsbDiscovery`, you need to provide a `UsbSessionListener` which just requires 3 methods

```kotlin
public interface UsbSessionListener {
    void onSessionReceived(@NonNull final UsbSession session, boolean hasPermission) 
    void onSessionRemoved(@NonNull final UsbSession session)
    void onRequestPermissionsResult(@NonNull final UsbSession session, boolean isGranted)
}
```

2. Inside `onSessionReceived()` with `hasPermission=true`, then you can use the passed `UsbSession`

3. Create a `PivApplication` and call the appropriate method

```kotlin
    val app = PivApplication(session)
    val cert = app.getCertificate(Slot.SIGNATURE) // <-- assume we are using Signature slot
    Log.e(TAG, "Certificate subject DN name: " + cert.subjectDN.name)
    val input = ByteArray(1024 / 8)
    app.verify("123456") // <-- this is the default PIN
    val output = app.sign(Slot.SIGNATURE, Algorithm.RSA1024, input)
```

# How to debug

In general, you connect your mobile phone to you development machine via a USB cable. But in that case, then you can't insert your Yubikey into the phone's USB port. Therefore, you *HAVE* to debug via Wifi

```shell script
$ ./adb tcpip 5555
$ ./adb connect 192.168.1.2:555
```

Then you can see your phone in your Android Studio and you can debug using Wifi with a freely available USB port.