package customer.agrawal.anuj.shopkeeper;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProcessedOrderDetails extends Fragment {

    ArrayList<FirebaseMap> fireMap;
    ArrayList<String> count;
    String key;
    Map<String,Object> mmap;
    ProcessedAdapter adapter;
    RecyclerView rview;
    TextView tv1;
    String userID;
    Button btn1;
    int total;



    public ProcessedOrderDetails()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        key=getArguments().getCharSequence("key")+"";
        View v=inflater.inflate(R.layout.fragment_processed_order_details, container, false);
        fireMap =new ArrayList<>();
        count=new ArrayList<>();

        tv1=v.findViewById(R.id.tv2);//total



        adapter=new ProcessedAdapter();
        rview=v.findViewById(R.id.rview);
        rview.setLayoutManager(new LinearLayoutManager(getContext()));
        rview.setAdapter(adapter);


        getKeysAndValues();
        return v;

    }

    private void getKeysAndValues()
    {
        FirebaseFirestore.getInstance().collection("ProcessedOrders").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
        if(mmap!=null)
        {
            Set<String> keys = mmap.keySet();
            fireMap = new ArrayList<>();
            count = new ArrayList<>();

            for (final String s : keys) {
                if (s.equals("userID") || s.equals("time"))
                {
                    userID = mmap.get("userID") + "";//ID of user
                    continue;
                }
                total=0;
                FirebaseFirestore.getInstance().collection("ProductInfo").document(s)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                FirebaseMap m = documentSnapshot.toObject(FirebaseMap.class);
                                if (m != null) {
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
    class ProcessedAdapter extends RecyclerView.Adapter<ProcessedViewHolder>
    {

        @NonNull
        @Override
        public ProcessedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View v1=LayoutInflater.from(getContext()).inflate(R.layout.placed_layout,parent,false);
            return new ProcessedViewHolder(v1);
        }

        @Override
        public void onBindViewHolder(@NonNull ProcessedViewHolder holder, int position)
        {
            holder.setBrand(fireMap.get(position).getBrand());
            holder.setDescription(fireMap.get(position).getDescription());
            holder.setQuantity(fireMap.get(position).getQuantity());

            String price=fireMap.get(position).getPrice();
            String num=count.get(position);

            holder.setPrice("Rs "+price);
            holder.setItems("items "+num);

           int p=Integer.parseInt(price)*Integer.parseInt(num);
           total=total+p;
            tv1.setText(total+"");

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

    class ProcessedViewHolder extends RecycleViewHolder
    {

        public ProcessedViewHolder(View v)
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


}
