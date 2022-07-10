package com.lux.ex090firebaseauth;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.lux.ex090firebaseauth.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    //Firebase Authentication (인증) - 로그인 기능 구현
    //1. email 및 비밀번호 기반 인증 - 이메일 인증확인을 통한 사용자 인증
    //2. Id 공급업체 사용 - Google, Apple, Facebook, Twitter, Github,,,, 계정 로그인 지원

    //Firebase 와 연동
    
    ActivityMainBinding binding;

    //Firebase Auth 객체 참조변수
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       //setContentView(R.layout.activity_main);
        
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        //인증 관리 객체 소환
        firebaseAuth=FirebaseAuth.getInstance();
        
        binding.btnSignup.setOnClickListener(view -> clickSignUp());
        binding.btnSignin.setOnClickListener(view -> clickSignIn());

        binding.btnGoogle.setOnClickListener(view -> clickGoogle());
        binding.btnLogout.setOnClickListener(view -> {
            firebaseAuth.signOut();
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
        });
    }
    void clickGoogle(){
        //Google 계정을 이용한 간편 로그인 기능
        //- 구글 로그인 화면(액티비티)을 실행시키는 Intent를 통한
        //startActivityForResult 로그인 방법
        //단, Google 계정 로그인 sdk를 별도 추가 해야 함. - play_services-auth 라이브러리
        //이 앱에서 다른 앱의 기능을 연동할때 AndroidManifest.xml 에 공개패키지 설정해야 함.

        //구글 로그인을 위한 옵션 객체 생성 = Builder 이용
        GoogleSignInOptions signInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .requestIdToken("543594194548-v501lev3r8og84sianf8qog7jhehr957.apps.googleusercontent.com")
                                            .requestEmail()
                                            .build();
        //구글 로그인 액티비티를 실행하는 Intent 객체 얻어오기
        Intent intent= GoogleSignIn.getClient(this,signInOptions).getSignInIntent();
        resultLauncher.launch(intent);
    }
    ActivityResultLauncher<Intent> resultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            //로그인 결과를 가져온 인텐트 객체 소환
            Intent intent=result.getData();
            //Intent로부터 구글 계정 정보를 가져오는 작업 객체 생성
            Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(intent);
            GoogleSignInAccount account=task.getResult();
            String email=account.getEmail();
            binding.tv2.setText(email);

        }
    });
    void clickSignIn(){
        //이메일과 비번을 이용한 로그인 인증
        String email=binding.etEmail.getText().toString();
        String pw=binding.etPw.getText().toString();

        firebaseAuth.signInWithEmailAndPassword(email,pw).addOnCompleteListener(task -> {
            if (task.isSuccessful()){


                //인증 받은 사용자인지 확인부터
                if (firebaseAuth.getCurrentUser().isEmailVerified()){
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show();

                    //현재 로그인 한 사용자의 정보 가져오기 - password는 가져올 수 없음.
                    String mail=firebaseAuth.getCurrentUser().getEmail();
                    binding.tv.setText("사용자 이메일 : "+mail);

                }else {
                    Toast.makeText(this, "이메일 인증 확인 필요", Toast.LENGTH_SHORT).show();
                }


            }else {
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
    void  clickSignUp(){
        //email 및 비밀번호를 이용한 인증 방식의 회원가입 - 입력된 이메일로 [인증확인] 메일이 보내지고 사용자가 확인했을때 가입이 완료되는 방식
        
        String email=binding.etEmail.getText().toString();
        String pw=binding.etPw.getText().toString();
        
        firebaseAuth.createUserWithEmailAndPassword(email,pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //입력된 이메일과 패스워드가 사용 가능한지를 검사한 결과=>유효성 검사 결과
                //1. 이메일 형식에 맞는가?   ex)xxxxx@xxx.xxx
                //2. 패스워드가 6자리 이상인가?   
                //3. 기존 이메일에 같은 이름이 있는지 확인
                if (task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "입력된 이메일과 비번이 사용 가능합니다.", Toast.LENGTH_SHORT).show();
                    //현재 상태도 firebase 에는 회원등록된 상태임. 다만, 인증이 안되어 있음.
                    
                    //입력된 이메일로 [인증확인] 메일 전송 및 전송성공여부 확인
                    firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "전송된 이메일을 확인하시고 인증 부탁", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(MainActivity.this, "메일 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    
                }else {
                    Toast.makeText(MainActivity.this, "이메일과 비밀번호 형식을 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}