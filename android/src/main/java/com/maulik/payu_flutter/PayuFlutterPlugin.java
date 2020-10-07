package com.maulik.payu_flutter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.payumoney.core.PayUmoneyConfig;
import com.payumoney.core.PayUmoneySdkInitializer;
import com.payumoney.core.entity.TransactionResponse;
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;

import java.util.HashMap;
import java.util.List;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.app.Activity.RESULT_OK;


import static android.app.Activity.RESULT_OK;

/** PayuFlutterPlugin */
public class PayuFlutterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
  private MethodChannel channel;
  private MethodChannel.Result mainResult;
  private Activity activity;
  private Context context;
  private String merchantKey;
  private String merchantID;
  private static final String TAG = "PayuMoneyPlugin";
  private boolean isProduction = false;
  PayUmoneySdkInitializer.PaymentParam paymentParam = null;
  PayUmoneySdkInitializer.PaymentParam.Builder builder = new PayUmoneySdkInitializer.PaymentParam.Builder();
  PayUmoneyConfig payUmoneyConfig = PayUmoneyConfig.getInstance();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "payu_money_flutter");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    this.mainResult = result;
    if (call.method.equals("setupDetails")) {
      this.merchantKey = call.argument("merchantKey");
      this.merchantID = call.argument("merchantID");
      this.isProduction = call.argument("isProduction");
      payUmoneyConfig.setPayUmoneyActivityTitle((String) call.argument("activityTitle"));
      payUmoneyConfig.disableExitConfirmation((Boolean) call.argument("disableExitConfirmation"));

      if (merchantID != null && merchantKey != null) {
        result.success(true);
        Log.d(TAG, "Payment Setup Completed");
      } else {
        result.error("302", "Pass MerchantID and MerchantKey", null);
        Log.d(TAG, "Payment Setup Failed");
      }
    } else if (call.method.equals("startPayment")) {
      if (merchantID != null && merchantKey != null) {
        startPayment(call);
      } else {
        result.error("302", "Pass MerchantID and MerchantKey using Setup Payment", null);
        Log.d(TAG, "Payment Setup Failed");
      }
    } else {
      result.notImplemented();
    }
  }

  private void startPayment(MethodCall call) {
    builder.setAmount((String) call.argument("amount"))
            .setTxnId((String) call.argument("txnid"))
            .setPhone((String) call.argument("phone"))
            .setProductName((String) call.argument("productName"))
            .setFirstName((String) call.argument("firstname"))
            .setEmail((String) call.argument("email"))
            .setsUrl("https://www.payumoney.com/mobileapp/payumoney/success.php")
            .setfUrl("https://www.payumoney.com/mobileapp/payumoney/failure.php")
            .setUdf1("")
            .setUdf2("")
            .setUdf3("")
            .setUdf4("")
            .setUdf5("")
            .setUdf6("")
            .setUdf7("")
            .setUdf8("")
            .setUdf9("")
            .setUdf10("")
            .setIsDebug(this.isProduction)
            .setKey(this.merchantKey)
            .setMerchantId(this.merchantID);

    try {
      this.paymentParam = builder.build();
      this.paymentParam.setMerchantHash((String) call.argument("hash"));


      PayUmoneyFlowManager.startPayUMoneyFlow(paymentParam, this.activity, R.style.AppTheme_default, true);
      payUmoneyConfig.setColorPrimary("#eb4034");

    } catch (Exception e) {
      mainResult.error("ERROR", e.getMessage(), null);
      Log.d(TAG, "Error : " + e.toString());
    }
  }


  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
    binding.addActivityResultListener(this);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
  }

  @Override
  public void onDetachedFromActivity() {
  }


  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.e(TAG, "request code " + requestCode + " resultcode " + resultCode);
    if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data != null) {
      TransactionResponse transactionResponse = data.getParcelableExtra(PayUmoneyFlowManager.INTENT_EXTRA_TRANSACTION_RESPONSE);

      if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {

        if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.SUCCESSFUL)) {
          String payuResponse = transactionResponse.getPayuResponse();

          HashMap<String, Object> response = new HashMap<String, Object>();
          response.put("status", "success");
          response.put("message", transactionResponse.getMessage());
          response.put("transactionResponse", payuResponse);
          mainResult.success(response);
        } else {
          HashMap<String, Object> response = new HashMap<String, Object>();
          response.put("message", transactionResponse.getMessage());
          response.put("status", "failed");
          response.put("transactionResponse", transactionResponse.toString());
          mainResult.success(response);
        }
      }
    }
    return false;
  }
}
