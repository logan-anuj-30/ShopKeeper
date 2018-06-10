package customer.agrawal.anuj.shopkeeper;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;

import javax.annotation.Nullable;

public class ViewRegisteredUsers extends Fragment
{

    RecyclerView r;
    ArrayList<UsersMap> mmap;
    UsersAdapter ad;
    public ViewRegisteredUsers()
    {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mmap=new ArrayList<>();
        View v=inflater.inflate(R.layout.fragment_view_registered_users, container, false);
        r=v.findViewById(R.id.rView);
        r.setLayoutManager(new LinearLayoutManager(getContext()));
        ad=new UsersAdapter();
        r.setAdapter(ad);

        FirebaseFirestore.getInstance().collection("Users").
                addSnapshotListener(new EventListener<QuerySnapshot>()
        {
    @Override
    public void onEvent(QuerySnapshot queryDocumentSnapshots,FirebaseFirestoreException e)
    {
        if(!queryDocumentSnapshots.isEmpty())
        {
            for(DocumentChange ds:queryDocumentSnapshots.getDocumentChanges())
            {
                if(ds.getType()==DocumentChange.Type.ADDED)
                {
                    UsersMap mm = ds.getDocument().toObject(UsersMap.class);
                    mmap.add(mm);
                    ad.notifyDataSetChanged();
                }
                else if(ds.getType()==DocumentChange.Type.REMOVED)
                {
                    mmap.remove(ds.getDocument().toObject(UsersMap.class));
                    ad.notifyDataSetChanged();
                }
            }
        }
    }
        });
        return v;
    }

    class UsersAdapter extends RecyclerView.Adapter<UsersViewHolder>
    {
        @Override
        public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View v=LayoutInflater.from(getContext()).inflate(R.layout.userslayout,parent,false);
            return new UsersViewHolder(v);
        }

        @Override
        public void onBindViewHolder(UsersViewHolder holder, int position)
        {
            holder.setName(mmap.get(position).getName());
            holder.setEmail(mmap.get(position).getEmail());
            holder.listener(position);
        }

        @Override
        public int getItemCount()
        {
            return mmap.size();
        }
    }

    class UsersViewHolder extends RecyclerView.ViewHolder
    {
        View v;
        public UsersViewHolder(View v)
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

        public void listener(final int p)
        {
            Button btn1=v.findViewById(R.id.btn1);
            btn1.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    AlertDialog.Builder al=new AlertDialog.Builder(getContext());
                    al.setMessage("Delete This User?");
                    al.setTitle("Delete");
                    al.setCancelable(false);
                    al.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                               removeUser(mmap.get(p).getEmail(),mmap.get(p).getPassword());
                        }
                    });
                    al.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    al.show();
                }
            });
        }
    }

    public void removeUser(String email,String pwd)
    {
        final ProgressDialog pd=new ProgressDialog(getContext());
        pd.setMessage("Please Wait");
        pd.setTitle("Deleting");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        final FirebaseAuth mAuth=FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email,
                pwd).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult)
            {
                FirebaseFirestore.getInstance().collection("Users").document(mAuth.getCurrentUser().getUid()).delete();
                mAuth.getCurrentUser().delete();
                ad.notifyDataSetChanged();
                pd.dismiss();
                MainActivity m=(MainActivity)getActivity();
                m.viewUsers();
            }
        });
    }
}
