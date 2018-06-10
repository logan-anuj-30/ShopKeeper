package customer.agrawal.anuj.shopkeeper;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.support.v4.provider.FontsContractCompat.FontRequestCallback.RESULT_OK;

public class EditProduct extends Fragment implements View.OnClickListener {
    EditText et1, et2, et3, et4;
    TextView tv1;
    ImageView img1;
    Button btn1, btn3;
    CheckBox cb1;
    String key = "";
    ProgressDialog pd;
    String imgLink;
    int category;
    Uri resultUri,requestUri;

    public EditProduct() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_product, container, false);
        key = getArguments().getCharSequence("key") + "";
        pd = new ProgressDialog(getContext());
        pd.setTitle("Edit");
        pd.setMessage("Loading");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        img1 = v.findViewById(R.id.img1);
        et1 = v.findViewById(R.id.et1);
        et2 = v.findViewById(R.id.et2);
        et3 = v.findViewById(R.id.et3);
        et4 = v.findViewById(R.id.et4);

        tv1 = v.findViewById(R.id.tv1);
        btn1 = v.findViewById(R.id.btn1);
        btn3 = v.findViewById(R.id.btn3);
        cb1 = v.findViewById(R.id.checkBox);

        registerForContextMenu(btn1);


        DocumentReference ref = FirebaseFirestore.getInstance().collection("ProductInfo").document(key);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot ds = task.getResult();
                    et1.setText(ds.get("brand") + "");
                    et4.setText(ds.get("description") + "");
                    et3.setText(ds.get("quantity") + "");
                    et2.setText(ds.get("price") + "");
                    tv1.setText(ds.get("category") + "");
                    String c = ds.get("available") + "";
                    if (c.equals("1"))
                        cb1.setChecked(true);

                    imgLink = ds.get("downloadURL") + "";
                    Picasso.get().load(imgLink).into(img1);
                    pd.dismiss();
                }
            }
        });

        registerForContextMenu(btn1);


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().openContextMenu(btn1);
            }
        });


        img1.setOnClickListener(this);
        btn3.setOnClickListener(this);


        return v;
    }

    public boolean onContextItemSelected(MenuItem item)
    {

        if(item.getItemId()==R.id.itm1)
        {
            category=1;
            tv1.setText("Dairy");
            return true;
        }
        else if(item.getItemId()==R.id.itm2)
        {
            category=2;
            tv1.setText("Pulses");
            return true;
        }
        else if(item.getItemId()==R.id.itm3)
        {
            category=3;
            tv1.setText("Oil");
            return true;
        }
        else if(item.getItemId()==R.id.itm4)
        {
            category=4;
            tv1.setText("Daily Need");
            return true;
        }
        else if(item.getItemId()==R.id.itm5)
        {
            category=5;
            tv1.setText("Home Cleaning");
            return true;
        }
        return false;
    };


    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {

        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        menu.setHeaderTitle("Category");
        inflater.inflate(R.menu.category, menu);
    }


    //to get image from gallry
    @Override
    public void onClick(View v)
    {
        if(v.getId()==R.id.img1)
        {
            Intent gallaryIntent=new Intent(Intent.ACTION_GET_CONTENT);
            gallaryIntent.setType("image/*");//Show only images not video

            //You can also start another activity and receive a result back.
            // To receive a result, call startActivityForResult()
            //When the user is done with the subsequent activity and returns,
            //   the system calls your activity's onActivityResult() method
            startActivityForResult(gallaryIntent,1);
        }

        //click on upload button
        else if(v.getId()==R.id.btn3)
        {
            //Button Upload
            if(checkIfNotEmpty())//check weather all entries filed or not
            {
                promptDialog(); //Show Dialogue to before Upload
            }

        }
    }

    private void promptDialog()
    {
        AlertDialog.Builder al=new AlertDialog.Builder(getContext());
        al.setTitle("UpLoad");
        al.setMessage("Are you Sure you want to Upload this Product?");
        al.setCancelable(false);
        al.setPositiveButton("Upload", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                uploadToFireStore();//Finnally upload to firebase
                pd.show();
            }
        });
        al.setNegativeButton("Make Changes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                Toast.makeText(getContext(),"UpLoad Cancel",Toast.LENGTH_SHORT).show();
            }
        });
        al.show();
    }

    private void uploadToFireStore()
    {
        if(resultUri!=null) {
            char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 25; i++) {
                char c = chars[random.nextInt(chars.length)];
                sb.append(c);
            }
            final String output = sb.toString();//random String name

            FirebaseStorage storage = FirebaseStorage.getInstance();//You're ready to start using Cloud Storage!
            StorageReference ref = storage.getReference().child("images").child(output);//Folder images, random name(output)
            ref.putFile(requestUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String downloadUrl = taskSnapshot.getUploadSessionUri().toString();
                    uploadToDatabase(downloadUrl);

                }
            });
        }
        uploadToDatabase(imgLink);


    }

   public void uploadToDatabase(String downloadUrl)
    {
        String brand=et1.getText().toString();
        String price=et2.getText().toString();
        String quantity=et3.getText().toString();
        String description=et4.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String,Object> map=new HashMap<>();
        map.put("brand",brand);
        map.put("price",price);
        map.put("quantity",quantity);
        map.put("description",description);
        map.put("downloadURL",downloadUrl);

        map.put("category",Integer.toString(category));
        boolean b=cb1.isChecked();
        if(b)
            map.put("available","0");
        else
            map.put("available","1");


        db.collection("ProductInfo").document(key).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid)
            {
                pd.dismiss();
                Toast.makeText(getContext(),"Successfully Uploaded",Toast.LENGTH_SHORT).show();
                MainActivity m=(MainActivity)getActivity();
                m.editToProduct();

            }
        });
    }



    private boolean checkIfNotEmpty()
    {
        //et1.getText().toString().length()==0 almost same as  TextUtils.isempty
        if(TextUtils.isEmpty(et1.getText())||TextUtils.isEmpty(et2.getText())||
                TextUtils.isEmpty(et3.getText())||TextUtils.isEmpty(et4.getText()) || category==0)
        {
            Toast.makeText(getContext(),"Please Fill All Entries",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //request Code:Check which request we're responding to
        //result code: Make sure the request was successful
        //Link for crop: https://github.com/ArthurHub/Android-Image-Cropper
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK)
        {
            requestUri=data.getData();
            CropImage.activity(requestUri)
                    .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(getActivity());
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                resultUri = result.getUri();
                img1.setImageURI(resultUri);
            }

            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
                Toast.makeText(getContext(),"Something Wrong",Toast.LENGTH_SHORT).show();
            }
        }
    }


}