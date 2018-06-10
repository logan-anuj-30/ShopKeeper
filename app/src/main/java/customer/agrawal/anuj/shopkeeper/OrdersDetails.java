package customer.agrawal.anuj.shopkeeper;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class OrdersDetails extends Fragment
{
    ArrayList<FirebaseMap> fireMap;
    ArrayList<String> count;
    String key;
    Map<String,Object> mmap;
    CurrentAdapter adapter;
    RecyclerView rview;
    TextView tv1;
    String userID;
    int total;

    Button btn1;
    public OrdersDetails()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v=inflater.inflate(R.layout.fragment_orders_details, container, false);
        key=getArguments().getCharSequence("key")+"";

        fireMap =new ArrayList<>();
        count=new ArrayList<>();

        tv1=v.findViewById(R.id.tv2);//total
        btn1=v.findViewById(R.id.btn1);//notify
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                notifyOrderReady();
            }
        });


        adapter=new CurrentAdapter();
        rview=v.findViewById(R.id.rview);
        rview.setLayoutManager(new LinearLayoutManager(getContext()));
        rview.setAdapter(adapter);

        getKeysAndValues();
        return v;
    }

    private void getKeysAndValues()
    {
        FirebaseFirestore.getInstance().collection("PlacedOrders").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
            {
                if(documentSnapshot!=null)
                {
                    mmap=new HashMap<>();
                    mmap=documentSnapshot.getData();
                }
                getData(mmap);
            }
        });
    }

    private void getData(final Map<String, Object> mmap)
    {
        if(mmap!=null) {
            Set<String> keys = mmap.keySet();
            fireMap = new ArrayList<>();
            count = new ArrayList<>();

            for (final String s : keys) {
                if (s.equals("userID") || s.equals("time"))
                {
                    userID = mmap.get("userID") + "";//ID of user
                    continue;
                }
                FirebaseFirestore.getInstance().collection("ProductInfo").document(s)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                FirebaseMap m = documentSnapshot.toObject(FirebaseMap.class);
                                if (m != null)
                                {
                                    fireMap.add(m);
                                    count.add(mmap.get(s) + "");
                                    total=0;
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
            }
        }
    }


    class CurrentAdapter extends RecyclerView.Adapter<CurrentViewHolder>
    {

        @NonNull
        @Override
        public CurrentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View v1=LayoutInflater.from(getContext()).inflate(R.layout.placed_layout,parent,false);
            return new CurrentViewHolder(v1);
        }

        @Override
        public void onBindViewHolder(@NonNull CurrentViewHolder holder, int position)
        {
            holder.setBrand(fireMap.get(position).getBrand());
            holder.setDescription(fireMap.get(position).getDescription());
            holder.setQuantity(fireMap.get(position).getQuantity());

            String price=fireMap.get(position).getPrice();
            String num=count.get(position);

            int sum=Integer.parseInt(price)*Integer.parseInt(num);
            total+=sum;
            tv1.setText(total+"");

            holder.setPrice("Rs "+price);
            holder.setItems("items "+num);

            holder.setItemTotal(price,num);

            String imageURL=fireMap.get(position).getDownloadURL();
            Picasso.get().load(imageURL).into(holder.getImage());
        }

        @Override
        public int getItemCount()
        {
            return fireMap.size();
        }
    }

    class CurrentViewHolder extends RecycleViewHolder
    {

        public CurrentViewHolder(View v)
        {
            super(v);
        }
        public void setDescription(String description)
        {
            TextView tv1=v.findViewById(R.id.tv2);
            tv1.setText(description);
        }

        public void setItemTotal(String price ,String num)
        {
            TextView t=v.findViewById(R.id.tv6);
            int sum=Integer.parseInt(price)*Integer.parseInt(num);
            t.setText(sum+"");
        }

        public void setBrand(String brand)
        {
            TextView tv1=v.findViewById(R.id.tv1);
            tv1.setText(brand);
        }
        public void setItems(String items)
        {
            TextView tv1=v.findViewById(R.id.tv4);
            tv1.setText(items);
        }
        public void setPrice(String price)
        {
            TextView tv1=v.findViewById(R.id.tv5);
            tv1.setText(price);
        }
        public void setQuantity(String quantity)
        {
            TextView tv1=v.findViewById(R.id.tv3);
            tv1.setText(quantity);
        }
        public ImageView getImage()
        {
            return (ImageView)v.findViewById(R.id.img1);
        }

    }


    public void notifyOrderReady()
    {
        AlertDialog.Builder ab=new AlertDialog.Builder(getContext());
        ab.setTitle("Order Ready");
        ab.setMessage("Notify user to pick up the product");
        ab.setCancelable(false);
        ab.setPositiveButton("Notify", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                final Map<String,String> noti=new HashMap<String, String>();
                FirebaseFirestore.getInstance().collection("Users").document(userID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                    {
                        String name,email,message;
                        if(documentSnapshot!=null && documentSnapshot.getData()!=null)
                        {
                            name=documentSnapshot.getData().get("name")+"";
                            email=documentSnapshot.getData().get("email")+"";
                            message="Hey "+name+", "+"Your order is ready to Pick Up.";
                            noti.put("message",message);
                            noti.put("received","false");
                            noti.put("time", FieldValue.serverTimestamp().toString());
                        }

                        FirebaseFirestore.getInstance().collection("Notification").document(userID)
                        .collection("navi").add(noti).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference)
                            {
                                Toast.makeText(getContext(),"Notified Successfully",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
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
}
