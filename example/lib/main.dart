import 'dart:convert';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:http/http.dart';
import 'package:payu_flutter/payu_flutter.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  // Creating PayuMoneyPlugin Instance
  PayuFlutter payuMoneyFlutter = PayuFlutter();

  // Payment Details
  String phone = "9632587410";
  String email = "gmail@gmail.com";
  String productName = "My Product Name";
  String firstName = "Maulik";
  String txnID = "223428947";
  String amount = "1.0";

  @override
  void initState() {
    super.initState();
    // Setting up the payment details
    setupPayment();
  }

  // Function for setting up the payment details
  setupPayment() async {
    bool response = await payuMoneyFlutter.setupPaymentKeys(
        merchantKey: "5fxRmkpa",
        merchantID: "6854818",
        isProduction: true,
        activityTitle: "Payumoney",
        disableExitConfirmation: false);

    print("response --> $response");
  }

  // Function for start payment with given merchant id and merchant key
  Future<Map<String, dynamic>> startPayment() async {
    // Generating hash from php server
    Response res =
    await post("http://phpdemo.anques.com/build/payumoney.php", body: {
      "txnid": txnID,
      "phone": phone,
      "mKey": "5fxRmkpa",
      "mSalt": "3C8R06Th5e",
      "email": email,
      "amount": amount,
      "productinfo": productName,
      "firstname": firstName,
    });
    var data = jsonDecode(res.body);
    print("data --> $data");
    String hash = data['params']['hash'];
    print("hash --> $hash");
    Map<dynamic, dynamic> myResponse = await payuMoneyFlutter.startPayment(
        txnid: txnID,
        amount: amount,
        name: firstName,
        email: email,
        phone: phone,
        productName: productName,
        hash: hash);
    payment = myResponse.toString();
    setState(() {});
    print("Message --> ${myResponse}");
  }

  String payment = "";

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(
          backgroundColor: Colors.green,
          title: const Text('Payu Money Flutter'),
        ),
        body: Center(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text("Pay by Payumoney 10"),
                Text("Payment responce :- $payment"),
              ],
            )),
        floatingActionButton: FloatingActionButton.extended(
          onPressed: () async {
            startPayment();
          },
          label: Text("Pay Us"),
          backgroundColor: Colors.green,
        ),
      ),
    );
  }
}
