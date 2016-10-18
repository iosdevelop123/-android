package com.example.secretwang.myapplication;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.Transport;
import java.net.URL;
import java.util.Date;

/**
 * Created by Secret Wang on 2015/12/15.
 */
public class request {
    public static SoapObject getResult(String method, String parpm) {
        String nameSpace = "http://tempuri.org/";
        String methodName = method;
        String requestUrl = "http://139.196.207.149:10013/WebService.asmx";
        String soapAction = nameSpace + methodName;

        HttpTransportSE httpTransportSE = new HttpTransportSE(requestUrl);
        httpTransportSE.debug = true;

        SoapObject soapObject = new SoapObject(nameSpace,methodName);

        soapObject.addProperty("str_json",parpm);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut = soapObject;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(soapObject);
        SoapObject soapObject1 = new SoapObject();
        try {
            httpTransportSE.call(soapAction,envelope);
            soapObject1 = (SoapObject)envelope.bodyIn;
        }catch (Exception e){
            e.printStackTrace();
        }
        String string = soapObject1.toString();
        if (string.equals("{}"))
        {
            soapObject1.addProperty("ErrMessage","连接超时");
            return soapObject1;
        }else {
        return soapObject1;
        }
    }
}
