## Regtest Setup

A regtest setup will save you a lot of time as it allows you to test Zap in a simulated network.


### Create a Network

The easiest way to create and manage simulated regtest networks is by using Polar.

1. Download [Polar][polar]
2. Setup a network in Polar with at least one LND node.


### Remote control your simulated LND nodes with Zap Android

To connect Zap with a simulated LND node:
1. Click on a LND node in Polar and navigate to the connect tab.
2. Select "LND Connect" in the sub tab.
3. Copy the connect string using the copy icon.
4. Open a textfile and paste the string there. It should now look like this: </br>
`lndconnect://127.0.0.1:10001?cert=MIICBzCC...`
5. Now it depends if you want to connect from the Android Studio emulator or from a physical phone.
For the emulator you have to replace the IP address with 10.0.2.2, for a phone you have to replace it with the local network IP address of the computer running polar. The result should look something like this:<p>
Emulator: `lndconnect://10.0.2.2:10001?cert=MIICBzCC...`</br>
Phone: `lndconnect://192.168.0.62:10001?cert=MIICBzCC...`</p>
6. Copy this edited connect string and paste it in Zap on the connect remote node screen or on the general scan screen.
7. Have fun testing!

Please note that connecting to regtest nodes will only work when building with debug build variant.

[polar]: https://lightningpolar.com/