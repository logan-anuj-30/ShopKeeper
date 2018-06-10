package customer.agrawal.anuj.shopkeeper;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProductDetails extends Fragment{
    TextView tv1,tv2,tv3,tv4;
    ImageView img1,img2;
    Button btn1,btn2;
    String key="";
    ProgressDialog pd;
    String imgLink;
    ProgressBar pbar;
    ScrollView sview;


    public ProductDetails()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v=inflater.inflate(R.layout.fragment_product_details, container, false);
        key=getArguments().getCharSequence("key").toString();

        pbar=v.findViewById(R.id.pbar);
        sview=v.findViewById(R.id.scroll);
        sview.setVisibility(View.INVISIBLE);

        pbar.setIndeterminate(true);

        tv1=v.findViewById(R.id.tv1);//brand
        tv2=v.findViewById(R.id.tv2);//description
        tv3=v.findViewById(R.id.tv3);//quantity
        tv4=v.findViewById(R.id.tv4);//price

        btn1=v.findViewById(R.id.btn1);//add to cart
        btn2=v.findViewById(R.id.btn2);//add to cart
        img1=v.findViewById(R.id.img1);//item image

        FirebaseFirestore db= FirebaseFirestore.getInstance();


        //Getting The Data associated with key
        DocumentReference ref=db.collection("ProductInfo").document(key);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    DocumentSnapshot ds=task.getResult();
                    tv1.setText(ds.get("brand")+"");
                    tv2.setText(ds.get("description")+"");
                    tv3.setText(ds.get("quantity")+"");
                    tv4.setText(ds.get("price")+"");
                    imgLink=ds.get("downloadURL")+"";
                    Picasso.get().load(imgLink).into(img1);
                    pbar.setVisibility(View.GONE);
                    sview.setVisibility(View.VISIBLE);
                }
            }
        });

        //Edit
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                    Intent i=new Intent(getContext(), AddProduct.class);
                    i.putExtra("key",key);
                    startActivity(i);
            }
        });

        //Remove
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                AlertDialog.Builder al=new AlertDialog.Builder(getContext());
                al.setTitle("Delete");
                al.setMessage("Are you Sure you want to delete?");
                al.setCancelable(false);

                al.setPositiveButton("Deleta", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        pd.setTitle("Deleting");
                        pd.setMessage("Deleting! Please wait");
                        pd.show();
                        removeItem(key);
                    }
                });

                al.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                });
                al.show();

            }
        });

        return v;
    }

    public void removeItem(String s)
    {
        FirebaseFirestore.getInstance().collection("ProductInfo").document(s).delete().
        addOnSuccessListener(new OnSuccessListener<Void>()
        {
        @Override
        public void onSuccess(Void aVoid)
        {
            pd.dismiss();
            startActivity(new Intent(getActivity(),MainActivity.class));
            Toast.makeText(getContext(),"Successfully Deleted",Toast.LENGTH_SHORT).show();
        }
        });


//        FirebaseStorage.getInstance().getReferenceFromUrl(imgLink).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid)
//            {
//                startActivity(new Intent(getActivity(),MainActivity.class));
//                Toast.makeText(getContext(),"Successfully Deleted",Toast.LENGTH_SHORT).show();
//
//            }
//        });
    }

}
