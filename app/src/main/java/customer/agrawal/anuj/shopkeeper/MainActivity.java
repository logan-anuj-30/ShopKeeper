package customer.agrawal.anuj.shopkeeper;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ServerTimestamp;

import java.net.InetAddress;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn1,btn2,btn3,btn4,btn5,btn6;
    Toolbar toolbar;
    @ServerTimestamp Date time;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar=findViewById(R.id.toolbar);
        toolbar.setTitle("E-market");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);

        Intent i=new Intent(MainActivity.this,MyService.class);
        startService(i);

        FirebaseFirestore.getInstance().collection("PlacedOrders").document("pWaqW7WCwQYaTaN5bosu").
                addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot,FirebaseFirestoreException e)
                    {

                    }
                });

        btn1=findViewById(R.id.btn1);
        btn2=findViewById(R.id.btn2);
        btn3=findViewById(R.id.btn3);
        btn4=findViewById(R.id.btn4);
        btn5=findViewById(R.id.btn5);
        btn6=findViewById(R.id.btn6);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        for(int z=0;z<getSupportFragmentManager().getBackStackEntryCount();z++)
        {
            getSupportFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId()==R.id.btn1)
        {
            //This will take from MainActivity to AddProduct activity
            startActivity(new Intent(MainActivity.this,AddProduct.class));
        }

       else if(v.getId()==R.id.btn2)
        {
            startActivity(new Intent(MainActivity.this,RegisterUser.class));

        }

        else if(v.getId()==R.id.btn3)
        {
            viewProduct();
        }

        else if(v.getId()==R.id.btn4)
        {
            viewUsers();
        }

        else if(v.getId()==R.id.btn5)
        {
            startActivity(new Intent(MainActivity.this,ViewOrders.class));
        }

        else if(v.getId()==R.id.btn6)
        {
            FragmentManager fm=getSupportFragmentManager();//view products
            FragmentTransaction ft=fm.beginTransaction();
            ProcessedOrders pd=new ProcessedOrders();
            ft.replace(R.id.linear,pd,"details").addToBackStack("details");
            ft.commit();
        }
    }

    public void viewProduct()
    {
        FragmentManager fm=getSupportFragmentManager();//view products
        FragmentTransaction ft=fm.beginTransaction();
        ft.replace(R.id.linear,new ViewProducts(),"Products").addToBackStack("products");
        ft.commit();
    }

    public void productDetails(String s)
    {
        FragmentManager fm=getSupportFragmentManager();//view products
        FragmentTransaction ft=fm.beginTransaction();
        Bundle b=new Bundle();
        b.putCharSequence("key",s);
        ProductDetails pd=new ProductDetails();
        pd.setArguments(b);
        ft.replace(R.id.linear,pd,"details").addToBackStack("details");
        ft.commit();
    }

    public void editProduct(String s)
    {
        FragmentManager fm=getSupportFragmentManager();//view products
        FragmentTransaction ft=fm.beginTransaction();
        Bundle b=new Bundle();
        b.putCharSequence("key",s);
        EditProduct e=new EditProduct();
        e.setArguments(b);
        ft.add(R.id.linear,e,"details");
        ft.commit();
    }

    public void editToProduct()
    {
        FragmentManager fm=getSupportFragmentManager();//view products
        FragmentTransaction ft=fm.beginTransaction();
        ft.add(R.id.linear,new ViewProducts(),"Products");
        ft.commit();
    }

    public void viewUsers()
    {
        FragmentManager fm=getSupportFragmentManager();//view products
        FragmentTransaction ft=fm.beginTransaction();
        ViewRegisteredUsers view=new ViewRegisteredUsers();
        ft.replace(R.id.linear,view,"details").addToBackStack("details");
        ft.commit();
    }

    public void showOrderDetails(String s)
    {
        FragmentManager fm=getSupportFragmentManager();
        FragmentTransaction ft=fm.beginTransaction();
        ProcessedOrderDetails od=new ProcessedOrderDetails();
        Bundle b=new Bundle();
        b.putCharSequence("key",s);//setting the key associated with click
        od.setArguments(b);
        ft.replace(R.id.linear,od,"Pdetails").addToBackStack("Pdetails");
        ft.commit();
    }

}
