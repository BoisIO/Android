package com.gostreamyourself.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.login_loginButton) Button loginBtn;
    @BindView(R.id.login_user) EditText userTxt;

    private int PICKFILE_RESULT_CODE = 123;

    private File file;
    private String token;
    private RSAPrivateKey privateKey;
    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                //chooseFile.setType("*/*");
                //chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                //startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);

                DialogProperties properties = new DialogProperties();
                properties.selection_mode = DialogConfigs.SINGLE_MODE;
                properties.selection_type = DialogConfigs.FILE_SELECT;
                properties.root = new File(DialogConfigs.DEFAULT_DIR);
                properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                properties.offset = new File(DialogConfigs.DEFAULT_DIR);
                properties.extensions = null;

                FilePickerDialog dialog = new FilePickerDialog(LoginActivity.this,properties);
                dialog.setTitle("Select a File");

                dialog.setDialogSelectionListener(new DialogSelectionListener() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        for(String path : files){
                            file = new File(path);

                            try {
                                FileInputStream fis = new FileInputStream(file);
                                DataInputStream dis = new DataInputStream(fis);
                                byte[] keyBytes = new byte[(int) file.length()];

                                dis.readFully(keyBytes);
                                dis.close();
                                String temp = new String(keyBytes);

                                String privKeyPEM = temp.replace("-----BEGIN PRIVATE KEY-----", "");
                                privKeyPEM = privKeyPEM.replace("-----END PRIVATE KEY-----", "");

                                privateKey = loadPrivateKey(privKeyPEM);

                                Log.i("test", "onSelectedFilePaths: " + privateKey.toString());

                                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                                url = "http://certifcation.herokuapp.com/login";

                                CustomLoginRequest stringRequest = new CustomLoginRequest(Request.Method.GET, url,
                                        new Response.Listener<CustomLoginRequest.ResponseM>() {
                                            @Override
                                            public void onResponse(CustomLoginRequest.ResponseM result) {

                                                try {

                                                    //From here you will get headers
                                                    token = result.headers.get("Token");
                                                    Log.i("Token", "onResponse: " + token);
                                                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                                                    byte[] hash = digest.digest(token.getBytes("UTF8"));
                                                    byte[] data = token.getBytes("UTF8");

                                                    Signature instance = Signature.getInstance("SHA256withRSA");
                                                    instance.initSign(privateKey);
                                                    instance.update(hash);
                                                    byte[] signatureBytes = instance.sign();
                                                    instance.update(hash);

                                                    final String encryptedToken = android.util.Base64.encodeToString(signatureBytes, android.util.Base64.DEFAULT);

                                                    final String username = userTxt.getText().toString();

                                                    byte[] bytes = encryptedToken.getBytes();

                                                    StringBuilder sb = new StringBuilder();
                                                    for (int i=0; i<bytes.length; i++) {
                                                        sb.append(String.format("%02X ",bytes[i]));
                                                    }

                                                    final String hexStringSpaces = sb.toString();

                                                    final String hexString = hexStringSpaces.replace(" ", "");

                                                    Log.i("After hex", "onResponse: " + hexStringSpaces);
                                                    Log.i("Without spaces", "onResponse: " + hexString);



                                                    StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                                                            new Response.Listener<String>() {
                                                                @Override
                                                                public void onResponse(String response) {
                                                                    // response
                                                                    Log.d("Response", response);
                                                                }
                                                            },
                                                            new Response.ErrorListener() {
                                                                @Override
                                                                public void onErrorResponse(VolleyError error) {
                                                                    // TODO Auto-generated method stub
                                                                    Log.d("ERROR", "error => " + error.toString());
                                                                }
                                                            }
                                                    ) {
                                                        @Override
                                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                                            Map<String, String> params = new HashMap<String, String>();

                                                            //Log.i("EncryptedToken", "getHeaders: " + encryptedToken.toString());
                                                            params.put("token", token);
                                                            params.put("signature", hexString);
                                                            params.put("name", username);

                                                            return params;
                                                        }
                                                    };

                                                    RequestQueue queue1 = Volley.newRequestQueue(LoginActivity.this);
                                                    queue1.add(postRequest);

                                                } catch(Exception e){
                                                    e.printStackTrace();
                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Toast.makeText(LoginActivity.this,error.toString(),Toast.LENGTH_LONG ).show();
                                            }
                                        }) {
                                };

                                queue.add(stringRequest);





                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });

                dialog.show();

            }
        });
    }

    public static RSAPrivateKey loadPrivateKey(String stored) throws GeneralSecurityException, IOException
    {
        byte[] data = Base64.getMimeDecoder().decode((stored.getBytes()));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(data);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(spec);
    }

    private static KeyPair getKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        return kpg.genKeyPair();
    }

}
