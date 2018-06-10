package customer.agrawal.anuj.shopkeeper;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewProducts extends Fragment
{

    ArrayList<FirebaseMap> al;
    RecyclerView rView;
    MyAdapter adapter;
    ArrayList<String> as;//hold keys
    GridLayoutManager gm;
    public ViewProducts()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        al=new ArrayList<FirebaseMap>();
        as=new ArrayList<String>();


        View v=inflater.inflate(R.layout.fragment_view_products, container, false);
        getData();
        rView=v.findViewById(R.id.rView);
        gm=new GridLayoutManager(getContext(),2);
        rView.setLayoutManager(gm);
        adapter=new MyAdapter(al);
        rView.setAdapter(adapter);

        return v;
}

public void getData()
{
    al=new ArrayList<FirebaseMap>();
    as=new ArrayList<String>();
    FirebaseFirestore.getInstance().collection("ProductInfo").addSnapshotListener(new EventListener<QuerySnapshot>()
    {
        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
        {
            if(e!=null)
            {
                Toast.makeText(getContext(),"Error in Getting Data",Toast.LENGTH_SHORT).show();
            }
            else if(queryDocumentSnapshots!=null)
            {
                for(DocumentChange d:queryDocumentSnapshots.getDocumentChanges())
                {
                    if(d.getType()==DocumentChange.Type.ADDED)
                    {
                        FirebaseMap fm=d.getDocument().toObject(FirebaseMap.class);
                        al.add(fm);
                        as.add(d.getDocument().getId());
                        adapter.notifyDataSetChanged();

                    }
                }
            }
        }
    });
}


    class MyAdapter extends RecyclerView.Adapter<RecycleViewHolder> implements View.OnClickListener
    {
        ArrayList<FirebaseMap> al;
        MyAdapter(ArrayList<FirebaseMap> al)
        {
            this.al=al;
        }
        @NonNull
        @Override
        public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View v=LayoutInflater.from(getContext()).inflate(R.layout.itemrecycle,parent,false);
            v.setOnClickListener(this);
            return new RecycleViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecycleViewHolder holder, int position)
        {
            holder.setBrand(al.get(position).getBrand());
            holder.setDescription(al.get(position).getDescription());
            holder.setQuantity(al.get(position).getQuantity());
            holder.setPrice(al.get(position).getPrice());
            String imageURL=al.get(position).getDownloadURL();
            Picasso.get().load(imageURL).into(holder.getImage());

            if(al.get(position).getAvailable().equals("1"))
            {
                holder.setVisibility(0);
            }
            else
            {
                holder.setVisibility(1);
            }

        }

        @Override
        public int getItemCount()
        {
            return al.size();
        }

        @Override
        public void onClick(View view)
        {
            int pos=rView.getChildLayoutPosition(view);//give the position which is clicked
            MainActivity m=(MainActivity)getActivity();
            m.productDetails(as.get(pos));

        }

    }

}
class RecycleViewHolder extends RecyclerView.ViewHolder
{
    View v;
    RelativeLayout rl;
    public RecycleViewHolder(View v)
    {
        super(v);
        this.v=v;
    }

    public void setDownloadURL(String downloadURL)
    {
        TextView tv1=v.findViewById(R.id.tv1);
        tv1.setText(downloadURL);
    }

    public void setVisibility(int i)
    {
        rl=v.findViewById(R.id.avail);

        if(i==1)
        {
            rl.setVisibility(View.INVISIBLE);
        }
        else
        {
            rl.setVisibility(View.VISIBLE);
        }
    }

    public void setDescription(String description)
    {
        TextView tv1=v.findViewById(R.id.tv2);
        tv1.setText(description);
    }
    public void setBrand(String brand)
    {
        TextView tv1=v.findViewById(R.id.tv1);
        tv1.setText(brand);
    }
    public void setDiscount(String discount)
    {
        TextView tv1=v.findViewById(R.id.tv1);
        tv1.setText(discount);
    }
    public void setPrice(String price)
    {
        TextView tv1=v.findViewById(R.id.tv4);
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


