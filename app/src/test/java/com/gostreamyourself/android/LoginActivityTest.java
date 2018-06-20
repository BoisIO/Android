package com.gostreamyourself.android;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class LoginActivityTest {
    private Activity activity;
    private Button loginBtn;
    private EditText userTxt;
    private TextView errorTxt;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(LoginActivity.class);
        loginBtn = (Button) activity.findViewById(R.id.login_loginButton);
        userTxt = (EditText) activity.findViewById(R.id.login_user);
        errorTxt = (TextView) activity.findViewById(R.id.login_errorTxt);
    }

    @Test
    (expected = GeneralSecurityException.class)
    public void loadedPrivateKeyIncorrectly() throws Exception {
        LoginActivity.loadPrivateKey("");
        LoginActivity.loadPrivateKey("-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDK5xlFTDtBk9GR\n" +
                "1uc6WpuaAXWkxXy/gosjWLkUt2DLbcmhuABaAYRsxqFUVF0NXsIYDHDubS78Saly\n" +
                "/jRWssK2GdcsLo06pJIskvpPyQgIBUyyEyDzVMLwNkyNPqHy2D5uEVmWMeizDaeo\n" +
                "GDbxpQQ/pyOLEx1V2/Idk40XwV3Uh/fnlqE2vbLTcmc4XVPMYRs/hZd66XI/i4Vn\n" +
                "7X6SkAR7o+V2v8OjVd/kwhxT0cNhOUnFPc8BsZfYVV9MYuM3rRau9qFnjS2ioang\n" +
                "wXiDAtJk74+r7w5gRiOi8CkfVd0TREW5wsxT1yI1LnjkwBKtOZrHYPfart24AiP0\n" +
                "4LJQym/RAgMBAAECggEBAKNmnNlL3OSg0ELsmGZMFcdeqYk6aY4o6Thb+SEP1Om+\n" +
                "zoRoFBBn7GdVRxMMpatgxlprmn7IevA9ujKbOTcg2uGfycMQ8jdp1TYEX4TlVoxv\n" +
                "CCbYdM7BUgP1Fo0hBXb2YsBtRIJKSyX3Ny+N4KGVzOgeB+e2SwecC016SEP7Oz8t\n" +
                "mXliLZxMIkegT3XFQonUO6bi6VwDQGytRCiJaXIaqJxeP0zFBWOEHH3AkEAhH2nV\n" +
                "ZF8XaKhKqs1yHg07+mKBi5DXJ8utPUVZlgEo9UwJ+9IpDYEkpgg0z9mfWvkLMNik\n" +
                "aqwr+BWZCLnx8R2XdljsETS3UaWVHqv3kYwM7fVDwoECgYEA5+kLSaRgSsU+KRCc\n" +
                "pvYIcuYc2NPm4/9FvwX0yA5nvEi2NT4RbX4ujd4ssiNFzYuBL8q2SmPBFjn3uQ2k\n" +
                "YY9CJ6GHHsbag8SDSesY17idBqGGELQ6alnaaaKVuUOCX1vjJmhCVoOy9yHk1UvL\n" +
                "zU2Nlhc0gyGxA76Q8PeugHpYBckCgYEA3/qvW2u1I737yY71TTgFpgj0uC1lJ5Xq\n" +
                "Ipd+fXAV4DB9Rk9/CJoy39D9ekBLZH/VCeHe1osfbzwSGbC2JWOjS9w1iyuXi2xK\n" +
                "4t8DCg+oGmEvY3bbJeXom3/Rl3JTq8wEWtnCVC6G1e88Ro6tkgAJkIZDQuJX4qRO\n" +
                "EID46QBlPckCgYB73okt64OXTGVYT8wsjcuTe3+6/OwdqdLaJldJqBAWjIvuQlnS\n" +
                "rww0sJAVEiwNjs8Q9OiLMV8H/SxpBnVfusIuIuPmN3at3P/3MoCRcMVGrm3KbEmr\n" +
                "VpUHyVTJoNp5Dr50XEnapfuobs0odJTF8v+GRJzC0fMXuej3HIRzkWWEqQKBgDs0\n" +
                "3R3hfuSP1/sT2ywqXvPg8i+tcEviiR3TxhAKjP3SU4s+gGoZuNEJQbgLPuK2F+6g\n" +
                "0nMMG6cfzfkUiVGg4Q2wjEbZoxmr6q/GG9uQ//LZNdmpFY0TFUXlp9XM1ulW6N/L\n" +
                "KF9wXRw4PUq5dJBfiie2FhmcE3dJz4j1Ttboz9phAoGBANKk2oHfupLsj0+4XA10\n" +
                "qfpAjzZ+9yx/mG4O5fslE3cx2Xma7Kd/vNxbNHH6ODJLpU5nZub1wGCz6nIUmmde\n" +
                "GoqV7DYwQWGFXWPvmIkLbp32QiA9aojS1KdYPB9bFVSZRdlBdR+FARN+6zczatBp\n" +
                "WMsLwEenTq2VGVxKDH1eWxWR\n" +
                "-----END PRIVATE KEY-----\n");
        LoginActivity.loadPrivateKey("MIIECjCCAvKgAwIBAgIJAIKW9Au40/h/MA0GCSqGSIb3DQEBCwUAMIGZMQswCQYD\n" +
                "VQQGEwJOTDEWMBQGA1UECAwNTm9vcmQtQnJhYmFudDEOMAwGA1UEBwwFQnJlZGEx\n" +
                "DzANBgNVBAoMBkJvaXNJTzEVMBMGA1UECwwMQW5kcm9pZCBUZWFtMR4wHAYDVQQD\n" +
                "DBVQYXRyaWNrIHZhbiBCYXRlbmJ1cmcxGjAYBgkqhkiG9w0BCQEWC29ra2VAZzRk\n" +
                "Lm5sMB4XDTE4MDYxMTA4MTY0N1oXDTE5MDYxMTA4MTY0N1owgZkxCzAJBgNVBAYT\n" +
                "Ak5MMRYwFAYDVQQIDA1Ob29yZC1CcmFiYW50MQ4wDAYDVQQHDAVCcmVkYTEPMA0G\n" +
                "A1UECgwGQm9pc0lPMRUwEwYDVQQLDAxBbmRyb2lkIFRlYW0xHjAcBgNVBAMMFVBh\n" +
                "dHJpY2sgdmFuIEJhdGVuYnVyZzEaMBgGCSqGSIb3DQEJARYLb2trZUBnNGQubmww\n" +
                "ggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDK5xlFTDtBk9GR1uc6Wpua\n" +
                "AXWkxXy/gosjWLkUt2DLbcmhuABaAYRsxqFUVF0NXsIYDHDubS78Saly/jRWssK2\n" +
                "GdcsLo06pJIskvpPyQgIBUyyEyDzVMLwNkyNPqHy2D5uEVmWMeizDaeoGDbxpQQ/\n" +
                "pyOLEx1V2/Idk40XwV3Uh/fnlqE2vbLTcmc4XVPMYRs/hZd66XI/i4Vn7X6SkAR7\n" +
                "o+V2v8OjVd/kwhxT0cNhOUnFPc8BsZfYVV9MYuM3rRau9qFnjS2ioangwXiDAtJk\n" +
                "74+r7w5gRiOi8CkfVd0TREW5wsxT1yI1LnjkwBKtOZrHYPfart24AiP04LJQym/R\n" +
                "AgMBAAGjUzBRMB0GA1UdDgQWBBT76WvKLHPTRn/Jnvy0VKjnnJaP7zAfBgNVHSME\n" +
                "GDAWgBT76WvKLHPTRn/Jnvy0VKjnnJaP7zAPBgNVHRMBAf8EBTADAQH/MA0GCSqG\n" +
                "SIb3DQEBCwUAA4IBAQBLUeDhA/INu43+OnPEyNaMDcbRzAs1iD1lejGwJbCHIHM9\n" +
                "4fdtv4wNCmlbeoKPR3kWPhmd/SC1R01iL8ncAwFybvvPKQsftZsugyjDnT/DVPsW\n" +
                "TynsK5JHI+XQw22MkYbjOXkDhjLzYVU7X76dsILPJ+P9CL4qxoScuWudKUs4vsYO\n" +
                "O5B0s67nbu1V+/e9CINefSoOd6AjwwCwgWY5pHeFF09uuffu3FqaQM7e39Bni80R\n" +
                "OpUp1oxLoXzK52/eNafSAZtGehzyfhtsq7nWp/JVBjQKkq4AoqPly3DmKY42NfcW\n" +
                "V0lAh8rd9dXjGXVbI11PFeGbTtzdwSRoAJxXbWET");
    }

    @Test
    public void loadedPrivateKeyCorrectly() {
        Exception ex = null;

        try {
            LoginActivity.loadPrivateKey("MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDK5xlFTDtBk9GR\n" +
                    "1uc6WpuaAXWkxXy/gosjWLkUt2DLbcmhuABaAYRsxqFUVF0NXsIYDHDubS78Saly\n" +
                    "/jRWssK2GdcsLo06pJIskvpPyQgIBUyyEyDzVMLwNkyNPqHy2D5uEVmWMeizDaeo\n" +
                    "GDbxpQQ/pyOLEx1V2/Idk40XwV3Uh/fnlqE2vbLTcmc4XVPMYRs/hZd66XI/i4Vn\n" +
                    "7X6SkAR7o+V2v8OjVd/kwhxT0cNhOUnFPc8BsZfYVV9MYuM3rRau9qFnjS2ioang\n" +
                    "wXiDAtJk74+r7w5gRiOi8CkfVd0TREW5wsxT1yI1LnjkwBKtOZrHYPfart24AiP0\n" +
                    "4LJQym/RAgMBAAECggEBAKNmnNlL3OSg0ELsmGZMFcdeqYk6aY4o6Thb+SEP1Om+\n" +
                    "zoRoFBBn7GdVRxMMpatgxlprmn7IevA9ujKbOTcg2uGfycMQ8jdp1TYEX4TlVoxv\n" +
                    "CCbYdM7BUgP1Fo0hBXb2YsBtRIJKSyX3Ny+N4KGVzOgeB+e2SwecC016SEP7Oz8t\n" +
                    "mXliLZxMIkegT3XFQonUO6bi6VwDQGytRCiJaXIaqJxeP0zFBWOEHH3AkEAhH2nV\n" +
                    "ZF8XaKhKqs1yHg07+mKBi5DXJ8utPUVZlgEo9UwJ+9IpDYEkpgg0z9mfWvkLMNik\n" +
                    "aqwr+BWZCLnx8R2XdljsETS3UaWVHqv3kYwM7fVDwoECgYEA5+kLSaRgSsU+KRCc\n" +
                    "pvYIcuYc2NPm4/9FvwX0yA5nvEi2NT4RbX4ujd4ssiNFzYuBL8q2SmPBFjn3uQ2k\n" +
                    "YY9CJ6GHHsbag8SDSesY17idBqGGELQ6alnaaaKVuUOCX1vjJmhCVoOy9yHk1UvL\n" +
                    "zU2Nlhc0gyGxA76Q8PeugHpYBckCgYEA3/qvW2u1I737yY71TTgFpgj0uC1lJ5Xq\n" +
                    "Ipd+fXAV4DB9Rk9/CJoy39D9ekBLZH/VCeHe1osfbzwSGbC2JWOjS9w1iyuXi2xK\n" +
                    "4t8DCg+oGmEvY3bbJeXom3/Rl3JTq8wEWtnCVC6G1e88Ro6tkgAJkIZDQuJX4qRO\n" +
                    "EID46QBlPckCgYB73okt64OXTGVYT8wsjcuTe3+6/OwdqdLaJldJqBAWjIvuQlnS\n" +
                    "rww0sJAVEiwNjs8Q9OiLMV8H/SxpBnVfusIuIuPmN3at3P/3MoCRcMVGrm3KbEmr\n" +
                    "VpUHyVTJoNp5Dr50XEnapfuobs0odJTF8v+GRJzC0fMXuej3HIRzkWWEqQKBgDs0\n" +
                    "3R3hfuSP1/sT2ywqXvPg8i+tcEviiR3TxhAKjP3SU4s+gGoZuNEJQbgLPuK2F+6g\n" +
                    "0nMMG6cfzfkUiVGg4Q2wjEbZoxmr6q/GG9uQ//LZNdmpFY0TFUXlp9XM1ulW6N/L\n" +
                    "KF9wXRw4PUq5dJBfiie2FhmcE3dJz4j1Ttboz9phAoGBANKk2oHfupLsj0+4XA10\n" +
                    "qfpAjzZ+9yx/mG4O5fslE3cx2Xma7Kd/vNxbNHH6ODJLpU5nZub1wGCz6nIUmmde\n" +
                    "GoqV7DYwQWGFXWPvmIkLbp32QiA9aojS1KdYPB9bFVSZRdlBdR+FARN+6zczatBp\n" +
                    "WMsLwEenTq2VGVxKDH1eWxWR");
        } catch (Exception e) {
            ex = e;
        }

        assertEquals(null, ex);
    }

    @Test
    public void clickingButton_shouldLoginUser() throws Exception {
        Method method = LoginActivity.class.getDeclaredMethod("loadPrivateKey", File.class);
        method.setAccessible(true);

        File file = File.createTempFile( "some-prefix", "pem");
        file.deleteOnExit();

        String str = "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDK5xlFTDtBk9GR\n" +
                "1uc6WpuaAXWkxXy/gosjWLkUt2DLbcmhuABaAYRsxqFUVF0NXsIYDHDubS78Saly\n" +
                "/jRWssK2GdcsLo06pJIskvpPyQgIBUyyEyDzVMLwNkyNPqHy2D5uEVmWMeizDaeo\n" +
                "GDbxpQQ/pyOLEx1V2/Idk40XwV3Uh/fnlqE2vbLTcmc4XVPMYRs/hZd66XI/i4Vn\n" +
                "7X6SkAR7o+V2v8OjVd/kwhxT0cNhOUnFPc8BsZfYVV9MYuM3rRau9qFnjS2ioang\n" +
                "wXiDAtJk74+r7w5gRiOi8CkfVd0TREW5wsxT1yI1LnjkwBKtOZrHYPfart24AiP0\n" +
                "4LJQym/RAgMBAAECggEBAKNmnNlL3OSg0ELsmGZMFcdeqYk6aY4o6Thb+SEP1Om+\n" +
                "zoRoFBBn7GdVRxMMpatgxlprmn7IevA9ujKbOTcg2uGfycMQ8jdp1TYEX4TlVoxv\n" +
                "CCbYdM7BUgP1Fo0hBXb2YsBtRIJKSyX3Ny+N4KGVzOgeB+e2SwecC016SEP7Oz8t\n" +
                "mXliLZxMIkegT3XFQonUO6bi6VwDQGytRCiJaXIaqJxeP0zFBWOEHH3AkEAhH2nV\n" +
                "ZF8XaKhKqs1yHg07+mKBi5DXJ8utPUVZlgEo9UwJ+9IpDYEkpgg0z9mfWvkLMNik\n" +
                "aqwr+BWZCLnx8R2XdljsETS3UaWVHqv3kYwM7fVDwoECgYEA5+kLSaRgSsU+KRCc\n" +
                "pvYIcuYc2NPm4/9FvwX0yA5nvEi2NT4RbX4ujd4ssiNFzYuBL8q2SmPBFjn3uQ2k\n" +
                "YY9CJ6GHHsbag8SDSesY17idBqGGELQ6alnaaaKVuUOCX1vjJmhCVoOy9yHk1UvL\n" +
                "zU2Nlhc0gyGxA76Q8PeugHpYBckCgYEA3/qvW2u1I737yY71TTgFpgj0uC1lJ5Xq\n" +
                "Ipd+fXAV4DB9Rk9/CJoy39D9ekBLZH/VCeHe1osfbzwSGbC2JWOjS9w1iyuXi2xK\n" +
                "4t8DCg+oGmEvY3bbJeXom3/Rl3JTq8wEWtnCVC6G1e88Ro6tkgAJkIZDQuJX4qRO\n" +
                "EID46QBlPckCgYB73okt64OXTGVYT8wsjcuTe3+6/OwdqdLaJldJqBAWjIvuQlnS\n" +
                "rww0sJAVEiwNjs8Q9OiLMV8H/SxpBnVfusIuIuPmN3at3P/3MoCRcMVGrm3KbEmr\n" +
                "VpUHyVTJoNp5Dr50XEnapfuobs0odJTF8v+GRJzC0fMXuej3HIRzkWWEqQKBgDs0\n" +
                "3R3hfuSP1/sT2ywqXvPg8i+tcEviiR3TxhAKjP3SU4s+gGoZuNEJQbgLPuK2F+6g\n" +
                "0nMMG6cfzfkUiVGg4Q2wjEbZoxmr6q/GG9uQ//LZNdmpFY0TFUXlp9XM1ulW6N/L\n" +
                "KF9wXRw4PUq5dJBfiie2FhmcE3dJz4j1Ttboz9phAoGBANKk2oHfupLsj0+4XA10\n" +
                "qfpAjzZ+9yx/mG4O5fslE3cx2Xma7Kd/vNxbNHH6ODJLpU5nZub1wGCz6nIUmmde\n" +
                "GoqV7DYwQWGFXWPvmIkLbp32QiA9aojS1KdYPB9bFVSZRdlBdR+FARN+6zczatBp\n" +
                "WMsLwEenTq2VGVxKDH1eWxWR\n" +
                "-----END PRIVATE KEY-----\n";
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(str);
        writer.close();
        userTxt.setText("Patrick van Batenburg");

        method.invoke(File.class, file);
        loginBtn.performClick();

        Field field = LoginActivity.class.getDeclaredField("token");
        field.setAccessible(true);
        Class<?> targetType = field.getType();
        Object objectValue = targetType.newInstance();

        Object value = field.get(objectValue);
        assertThat(value.toString(), is(isEmptyString()));
    }
}