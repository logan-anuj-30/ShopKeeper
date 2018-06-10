package customer.agrawal.anuj.shopkeeper;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;


public class AddProduct extends AppCompatActivity implements View.OnClickListener
{

    EditText et1,et2,et3,et4,et5;
    TextView tv1;
    Button btn1,btn3;
    Toolbar toolbar;
    ImageButton img;
    ProgressDialog pd;
    Uri resultUri,requestUri;
    CheckBox checkBox;
    String key;
    int category;
    String imgLink;
    String downloadUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product);

        Intent i=getIntent();
        key=i.getStringExtra("key");


        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd=new ProgressDialog(AddProduct.this);
        pd.setTitle("Uploading");
        pd.setMessage("Please wait while we are uploading this Product");
        pd.setCanceledOnTouchOutside(false);


        et1 = findViewById(R.id.et1);//brand
        et2 = findViewById(R.id.et2);//price
        et3 = findViewById(R.id.et3);//quantity
        et4 = findViewById(R.id.et4);//description

        tv1=findViewById(R.id.tv1);//category

        img = findViewById(R.id.img1);//Image

        checkBox=findViewById(R.id.checkBox);

        btn1 = findViewById(R.id.btn1);//category
        btn3 = findViewById(R.id.btn3);//upload

        registerForContextMenu(btn1);

        btn1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                openContextMenu(btn1);
            }
        });

        btn3.setOnClickListener(this);
        img.setOnClickListener(this);

        if(key!=null)//means coming from edit
        {
            btn3.setText("Update");
            updateTheDocument();
        }

    }

    public void updateTheDocument()
    {
        DocumentReference ref = FirebaseFirestore.getInstance().collection("ProductInfo").document(key);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    DocumentSnapshot ds = task.getResult();
                    et1.setText(ds.get("brand") + "");
                    et4.setText(ds.get("description") + "");
                    et3.setText(ds.get("quantity") + "");
                    et2.setText(ds.get("price") + "");
                    tv1.setText(ds.get("category") + "");
                    String c = ds.get("available") + "";
                    if (c.equals("1"))
                        checkBox.setChecked(true);

                    imgLink = ds.get("downloadURL") + "";
                    Picasso.get().load(imgLink).into(img);
                    pd.dismiss();
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {

        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        menu.setHeaderTitle("Category");
        inflater.inflate(R.menu.category, menu);
    }


    @Override
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
        AlertDialog.Builder al=new AlertDialog.Builder(AddProduct.this);
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
                Toast.makeText(AddProduct.this,"UpLoad Cancel",Toast.LENGTH_SHORT).show();
            }
        });
        al.show();
    }

    private void uploadToFireStore()
    {
        if(resultUri!=null && imgLink==null)
        {
            char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 25; i++) {
                char c = chars[random.nextInt(chars.length)];
                sb.append(c);
            }
            final String output = sb.toString();//random String name

            FirebaseStorage storage = FirebaseStorage.getInstance();//You're ready to start using Cloud Storage!
            final StorageReference ref = storage.getReference().child("images").child(output);//Folder images, random name(output)
            ref.putFile(requestUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                    {
                        @Override
                        public void onSuccess(Uri uri)
                        {
                            downloadUrl=uri.toString();
                            String brand=et1.getText().toString();
                    String price=et2.getText().toString();
                    String quantity=et3.getText().toString();
                    String description=et4.getText().toString();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String,String> map=new HashMap<>();
                    map.put("brand",brand);
                    map.put("price",price);
                    map.put("quantity",quantity);
                    map.put("description",description);
                    map.put("downloadURL",downloadUrl);

                    map.put("category",Integer.toString(category));
                    boolean b=checkBox.isChecked();
                    if(b)
                        map.put("available","0");
                    else
                        map.put("available","1");


            db.collection("ProductInfo").add(map).addOnSuccessListener
            (new OnSuccessListener<DocumentReference>()
                {
                    @Override
                    public void onSuccess(DocumentReference documentReference)
                    {
                        pd.dismiss();
                        startActivity(new Intent(AddProduct.this, MainActivity.class));
                        finish();
    //                            Toast.makeText(AddProduct.this,"Successfully Uploaded",Toast.LENGTH_SHORT).show();
    //                            changeFragment();//this will move from this to products
    //                            finish();

                    }
                });

                        }
                    });
                }
            });
        }
        else
        {
            uploadToDatabase(imgLink);
        }


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
        map.put("price",price+"");
        map.put("quantity",quantity);
        map.put("description",description);
        map.put("downloadURL",downloadUrl);

        map.put("category",Integer.toString(category));
        boolean b=checkBox.isChecked();
        if(b)
            map.put("available","1");
        else
            map.put("available","0");


        db.collection("ProductInfo").document(key).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid)
            {
                pd.dismiss();
                Toast.makeText(AddProduct.this,"Successfully Uploaded",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddProduct.this, MainActivity.class));
                finish();

            }
        });
    }



    private boolean checkIfNotEmpty()
    {
        //et1.getText().toString().length()==0 almost same as  TextUtils.isempty
        if(TextUtils.isEmpty(et1.getText())||TextUtils.isEmpty(et2.getText())||
                TextUtils.isEmpty(et3.getText())||TextUtils.isEmpty(et4.getText())|| (resultUri==null && imgLink.isEmpty())  || category==0)
        {
            Toast.makeText(AddProduct.this,"Please Fill All Entries",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //request Code:Check which request we're responding to
        //result code: Make sure the request was successful
        //Link for crop: https://github.com/ArthurHub/Android-Image-Cropper
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK)
        {
            requestUri=data.getData();
            CropImage.activity(requestUri)
                    .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(AddProduct.this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                resultUri = result.getUri();
                img.setImageURI(resultUri);
            }

            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
                Toast.makeText(AddProduct.this,"Something Wrong",Toast.LENGTH_SHORT).show();
            }
        }
    }
    public  void changeFragment()
    {
        FragmentManager fm=getSupportFragmentManager();
        FragmentTransaction ft=fm.beginTransaction();
        ft.add(R.id.linear,new ViewProducts(),"Products");
        ft.commit();
    }
}
