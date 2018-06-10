package customer.agrawal.anuj.shopkeeper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

public class ViewOrders extends AppCompatActivity
{
    ArrayList<String> id;//this is userID
    RecyclerView rview;
    OrdersAdapter adp;
    ArrayList<UsersMap> mmap;//this is where details will be stored
    ArrayList<String> keys;//this is key(document) associated with each order

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_orders);

        id=new ArrayList<>();
        keys=new ArrayList<>();
        mmap=new ArrayList<>();

        FirebaseFirestore.getInstance().collection("PlacedOrders").orderBy("time", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
            {
            if(queryDocumentSnapshots!=null)
            {
                for (DocumentChange ds : queryDocumentSnapshots.getDocumentChanges())
                {
                    if(ds.getType()==DocumentChange.Type.ADDED)
                    {
                        id.add(ds.getDocument().get("userID")+"");//userid All
                        keys.add(ds.getDocument().getId());//key all users 1 key/user
                    }
                    else if(ds.getType()==DocumentChange.Type.REMOVED)
                    {
                        id.remove(ds.getDocument().get("userID")+"");
                        keys.remove(ds.getDocument().getId());
                    }
                }
                getNameFromID();//this method will extract email and name from ID
            }
            }
        });

        rview=findViewById(R.id.rview);
        rview.setLayoutManager(new LinearLayoutManager(this));
        adp=new OrdersAdapter();
        rview.setAdapter(adp);
    }

    public void getNameFromID()
    {
        mmap=new ArrayList<>();
        for(String s:id)
        {
            FirebaseFirestore.getInstance().collection("Users").
                    document(s).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null)
                    {
                        UsersMap m = documentSnapshot.toObject(UsersMap.class);
                        mmap.add(m);
                        adp.notifyDataSetChanged();
                    }
                }
            });
        }
    }



    class OrdersAdapter extends RecyclerView.Adapter<OrdersViewHolder>
    {

        @Override
        public OrdersViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View v= LayoutInflater.from(ViewOrders.this).inflate(R.layout.userslayout,parent,false);
            return new OrdersViewHolder(v);
        }

        @Override
        public void onBindViewHolder(OrdersViewHolder holder, int position)
        {
            holder.setName(mmap.get(position).getName()+"");
            holder.setEmail(mmap.get(position).getEmail()+"");
            holder.setOnClick(position);
            holder.ready(position);
        }

        @Override
        public int getItemCount()
        {
            return mmap.size();
        }
    }

    class OrdersViewHolder extends RecyclerView.ViewHolder
    {
        View v;
        public OrdersViewHolder(View v)
        {
            super(v);
            this.v=v;
        }

        public void setName(String name)
        {
            TextView tv1=v.findViewById(R.id.tv1);
            tv1.setText(name); 
        }
        public void setEmail(String email)
        {
            TextView tv1=v.findViewById(R.id.tv2);
            tv1.setText(email);
        }
        
        public void ready(final int position)
        {
            Button btn1=v.findViewById(R.id.btn1);
            btn1.setText("Ready");
            btn1.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    removeItemDialog(position);
                }
            });
        }

        public void setOnClick(final int position)
        {
            v.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    showOrderDetails(position);
                }
            });
        }

    }

    private void removeItemDialog(final int position)
    {
        AlertDialog.Builder ab=new AlertDialog.Builder(ViewOrders.this);
        ab.setTitle("Order Completed");
        ab.setMessage("Remove this order from List");
        ab.setCancelable(false);
        ab.setPositiveButton("Complete", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                removeOrder(position);
            }
        });

        ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {

            }
        });
        ab.show();
    }

    private void showOrderDetails(int pos)
    {
        FragmentManager fm=getSupportFragmentManager();
        FragmentTransaction ft=fm.beginTransaction();
        OrdersDetails od=new OrdersDetails();
        Bundle b=new Bundle();
        b.putCharSequence("key",keys.get(pos));//setting the key associated with click
        od.setArguments(b);
        ft.replace(R.id.frame,od,"orderDetails").addToBackStack("orderDetails");
        ft.commit();
    }

    public void removeOrder(final int position)
    {
        final ProgressDialog pd=new ProgressDialog(ViewOrders.this);
        pd.setTitle("Removing");
        pd.setMessage("Moving to placed Orders");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        FirebaseFirestore.getInstance().collection("PlacedOrders").whereEqualTo("userID",id.get(position))
            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot qds)
            {
                Map<String,Object> snap=new HashMap<>();
                if(qds!=null && !qds.getDocuments().isEmpty())
                {
                    for(DocumentSnapshot ds:qds.getDocuments())
                    {
                        snap=ds.getData();//copy data to hashmap
                    }
                }
                snap.put("time", FieldValue.serverTimestamp());

                FirebaseFirestore.getInstance().collection("ProcessedOrders").add(snap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                {
                    @Override
                    public void onSuccess(DocumentReference documentReference)
                    {
                        pd.dismiss();
                        String ss=keys.get(position);
                        keys.remove(position);
                        mmap.remove(position);
                        id.remove(position);

                        adp.notifyDataSetChanged();

                        FirebaseFirestore.getInstance().collection("PlacedOrders").document(ss).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>()
                        {
                            @Override
                            public void onSuccess(Void aVoid)
                            {

                            }
                        });
                    }
                });
            }
        });
        }

}
