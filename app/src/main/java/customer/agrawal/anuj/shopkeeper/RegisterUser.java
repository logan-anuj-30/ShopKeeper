package customer.agrawal.anuj.shopkeeper;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterUser extends AppCompatActivity
{
    EditText et1,et2,et3;
    Button btn1;
    ProgressDialog pd;
    android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_user);
        et1=findViewById(R.id.et1);//name
        et2=findViewById(R.id.et2);//email
        et3=findViewById(R.id.et3);//pwd
        btn1=findViewById(R.id.btn1);

        toolbar=findViewById(R.id.toolbar);
        toolbar.setTitle("Register User");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd=new ProgressDialog(RegisterUser.this);
        pd.setTitle("Register user");
        pd.setMessage("Please wait while we Register This user");
        btn1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(checkIfNotEmpty())
                {

                    promptDialog();
                }
            }
        });

    }

    private boolean checkIfNotEmpty()
    {
        //et1.getText().toString().length()==0 almost same as  TextUtils.isempty
        if(TextUtils.isEmpty(et1.getText())||TextUtils.isEmpty(et2.getText())||
                TextUtils.isEmpty(et3.getText())|| et3.getText().toString().length()<8)
        {
            Toast.makeText(RegisterUser.this,"Please Fill All Entries",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void promptDialog()
    {
        AlertDialog.Builder al=new AlertDialog.Builder(RegisterUser.this);
        al.setTitle("Register User");
        al.setMessage("Are you Sure you want to Register this User?");
        al.setCancelable(false);
        al.setPositiveButton("Register", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                uploadToFireStore();//Finnally upload to firebase
                pd.show();
            }
        });
        al.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                Toast.makeText(RegisterUser.this,"UpLoad Cancel",Toast.LENGTH_SHORT).show();
            }
        });
        al.show();
    }

    private void uploadToFireStore()
    {
        final FirebaseAuth mAuth=FirebaseAuth.getInstance();
        final String email=et2.getText().toString();
        final String pwd=et3.getText().toString();
        mAuth.createUserWithEmailAndPassword(email,pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(task.isSuccessful())
                {
                    String userID=task.getResult().getUser().getUid();
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("name", et1.getText().toString());
                    map.put("email", email);
                    map.put("password", pwd);

                    FirebaseFirestore.getInstance().collection("Users").document(userID).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            pd.dismiss();
                            Toast.makeText(RegisterUser.this,"Register Successfully",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterUser.this,MainActivity.class));
                            finish();
                        }
                    });
                }
                else
                {
                    Toast.makeText(RegisterUser.this,"Users Already Exist",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
