package com.example.shipra.mobileapplication;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.example.shipra.mobileapplication.model.CheckUserResponse;
import com.example.shipra.mobileapplication.model.User;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.example.shipra.mobileapplication.Retrofit.mobileAppAPI;
import com.example.shipra.mobileapplication.model.CheckUserResponse;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.shipra.mobileapplication.Utils.Common;
import com.rengwuxian.materialedittext.MaterialEditText;

import static android.util.Patterns.PHONE;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1000 ;
    Button button_continue;

    mobileAppAPI mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //PrintKeyHash();
        mService = Common.getAPI();

       button_continue= (Button)findViewById(R.id.btn_continue);
       button_continue.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               startLoginPage(LoginType.PHONE);

           }
       });

    }

    private void startLoginPage(LoginType loginType) {
        Intent intent=new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder builder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(loginType,AccountKitActivity.ResponseType.TOKEN);

        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,builder.build());

        startActivityForResult(intent,REQUEST_CODE);

        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQUEST_CODE){
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

            if (result.getError()!=null){

                Toast.makeText(this,""+result.getError().getErrorType().getMessage(),Toast.LENGTH_SHORT).show();

            }
            else{
                if(result.wasCancelled()){
                    Toast.makeText(this,"cancel",Toast.LENGTH_SHORT).show();
                }
                else{
                    if (result.getAccessToken()!=null){

                      //AlertDialog alertDialog= new AlertDialog(MainActivity.this);
                       // final android.app.AlertDialog alertDialog =new SpotsDialog(MainActivity.this);
                        final android.app.AlertDialog alertDialog = new SpotsDialog(MainActivity.this);
                        alertDialog.show();
                        alertDialog.setMessage("Please Wait");

                        //get user phone no. and check user exist on server or not

                        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                            @Override
                            public void onSuccess(final Account account) {

                                mService.checkUserExists(account.getPhoneNumber().toString())
                                        .enqueue(new Callback<CheckUserResponse>() {
                                            @Override
                                            public void onResponse(Call<CheckUserResponse> call, Response<CheckUserResponse> response) {
                                                CheckUserResponse userResponse=response.body();
                                                // if user already exits ,just start new Activity
                                                if(userResponse.isExist()) alertDialog.dismiss();
                                                else{
                                                    //else need register
                                                    alertDialog.dismiss();
                                                    showRegisterDialog(account.getPhoneNumber().toString());
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<CheckUserResponse> call, Throwable t) {


                                            }
                                        });

                            }

                            @Override
                            public void onError(AccountKitError accountKitError) {

                                Log.d("ERROR",accountKitError.getErrorType().getMessage());

                            }
                        });


                    }
                }
            }
        }

    }

    private void showRegisterDialog(final String phone) {

        final AlertDialog.Builder alertDialog= new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("REGISTER");
        LayoutInflater inflater= this.getLayoutInflater();

        final View register_layout= inflater.inflate(R.layout.register_layout,null);

        final MaterialEditText edt_name=(MaterialEditText)findViewById(R.id.edt_name);
        final MaterialEditText edit_email=(MaterialEditText)findViewById(R.id.edt_email);

        Button btn_Register=(Button)findViewById(R.id.btn_register);

        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                alertDialog.create().dismiss();
                if(TextUtils.isEmpty(edt_name.getText().toString())){
                    Toast.makeText(MainActivity.this,"Please enter your name",Toast.LENGTH_SHORT);
                    return;
                }
                if(TextUtils.isEmpty(edit_email.getText().toString())){
                    Toast.makeText(MainActivity.this,"Please enter your email",Toast.LENGTH_SHORT);
                    return;
                }

                final android.app.AlertDialog waitingDialog =new SpotsDialog(MainActivity.this);
                waitingDialog.show();
                waitingDialog.setMessage("Please Waiting........");

                mService.registerNewUser(phone,
                        edt_name.getText().toString(),
                        edit_email.getText().toString())
                        .enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {

                                waitingDialog.dismiss();
                                User user= response.body();

                                if(TextUtils.isEmpty(user.getError_msg())){

                                    Toast.makeText(MainActivity.this,"user register successfully",Toast.LENGTH_SHORT);
                                    //start new activity


                                }
                            }

                            @Override
                            public void onFailure(Call<User> call, Throwable t) {

                                waitingDialog.dismiss();

                            }
                        });

                alertDialog.setView(register_layout);
                alertDialog.show();


            }
        });



    }

    private void PrintKeyHash(){
        try {
            PackageInfo info= getPackageManager().getPackageInfo("com.example.shipra.mobileapplication",
                    PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures){

                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KEYHASH", Base64.encodeToString(md.digest(),Base64.DEFAULT));

            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
