package customer.agrawal.anuj.shopkeeper;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;


public class ProcessedOrders extends Fragment
{

    ArrayList<String> id;//this is userID
    RecyclerView rview;
    ProcessedAdapter adp;
    ArrayList<UsersMap> mmap;//this is where details will be stored
    ArrayList<String> keys;//this is key(document) associated with each order
    int limit,skip;

    public ProcessedOrders()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle s)
    {
        View v=inflater.inflate(R.layout.fragment_processed_orders, container, false);

        id=new ArrayList<>();
        keys=new ArrayList<>();
        mmap=new ArrayList<>();

        getItems();

        rview=v.findViewById(R.id.rview);
        rview.setLayoutManager(new LinearLayoutManager(getContext()));
        adp=new ProcessedAdapter();
        rview.setAdapter(adp);

        return v;
    }


    public void  getItems() {

        FirebaseFirestore.getInstance().collection("ProcessedOrders").orderBy("time", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    for (DocumentChange ds : queryDocumentSnapshots.getDocumentChanges()) {
                        if (ds.getType() == DocumentChange.Type.ADDED) {
                            id.add(ds.getDocument().get("userID") + "");//userid All
                            keys.add(ds.getDocument().getId());//key all users 1 key/user
                        } else if (ds.getType() == DocumentChange.Type.REMOVED)
                        {
                            id.remove(ds.getDocument().get("userID") + "");
                            keys.remove(ds.getDocument().getId());
                        }
                    }
                    getNameFromID();//this method will extract email and name from ID
                }
            }
        });

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


    class ProcessedAdapter extends RecyclerView.Adapter<ProcessedViewHolder>
    {

        @Override
        public ProcessedViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View v= LayoutInflater.from(getContext()).inflate(R.layout.userslayout,parent,false);
            return new ProcessedViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ProcessedViewHolder holder, int position)
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

    class ProcessedViewHolder extends RecyclerView.ViewHolder
    {
        View v;
        public ProcessedViewHolder(View v)
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
            btn1.setVisibility(View.INVISIBLE);

        }

        public void setOnClick(final int position)
        {
            v.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    MainActivity v=(MainActivity)getActivity();
                    v.showOrderDetails(keys.get(position));
                }
            });
        }

    }

}
