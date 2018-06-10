package customer.agrawal.anuj.shopkeeper;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

import static android.app.Notification.VISIBILITY_PUBLIC;

//The service can run in the background indefinitely, even if the component that started it
// is destroyed. As such, the service should stop itself when its job is complete by calling stopSelf(),
// or another component can stop it by calling stopService().


//By default a service will be called on the main thread

//A service is started when component (like activity) calls startService() method,
// now it runs in the background indefinitely. It is stopped by stopService() method.
// The service can stop itself by calling the stopSelf() method.

public class MyService extends Service
{
    class ServiceThread implements Runnable
    {
        int serviceId;
        ServiceThread(int serviceId)
        {
            this.serviceId = serviceId;
        }

        @Override
        public void run()
        {
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
           db.collection("PlacedOrders").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot qds,FirebaseFirestoreException e)
                    {
                        if (e != null)
                        {
                            Toast.makeText(getApplicationContext(), "Some Data Retrival Error", Toast.LENGTH_SHORT).show();
                        }

                        else if (qds!=null && !qds.isEmpty() )
                        {
//                            Intent intent = new Intent(getApplicationContext(), Notification.class);
//                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                            final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "Unique_id");
                            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                            builder.setSmallIcon(R.drawable.notification);
                            builder.setContentTitle("E-market Message");

//                            builder.setContentIntent(pendingIntent);
                            builder.setVisibility(VISIBILITY_PUBLIC);//show on Lock Screen
                            builder.setAutoCancel(true);//clear when user tap on It
                            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                            builder.setGroupSummary(true);


                            final ArrayList<String> al=new ArrayList<>();
                            for(DocumentSnapshot ds:qds.getDocuments())
                            {
                                if(ds.getData()!=null && ds.getData().get("received")!=null && ds.getData().get("received").equals("false"))
                                {
                                    al.add(ds.getData().get("userID")+""); //extract userID from placed Orders
                                }

                            }
                 //to get name,email from users
                for(int i=0;i<al.size();i++)
                {
                    final int finalI = i;
                    FirebaseFirestore.getInstance().collection("Users").document(al.get(i))
                    .addSnapshotListener(new EventListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onEvent(DocumentSnapshot ds,FirebaseFirestoreException e)
                        {
                            if(e!=null)
                            {
                                Toast.makeText(getApplicationContext(),"Error in getting Data",Toast.LENGTH_SHORT).show();
                            }
                            else if(ds!=null && ds.getData()!=null )
                            {
                                String name=ds.getData().get("name")+"";
                                String email=ds.getData().get("email")+"";
                                String msg="New order from "+name+"("+email+")";
                                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
                                builder.setGroup("Key");
                                Log.e("New ","Notification");
                                // notificationId is a unique int for each notification that you must define
                                notificationManager.notify(serviceId + (int) (Math.random() * 2000000000), builder.build());
                            }
                        }
                    });
                }


                          FirebaseFirestore.getInstance().collection("PlacedOrders").whereEqualTo("received","false").addSnapshotListener(new EventListener<QuerySnapshot>() {
                              @Override
                              public void onEvent(QuerySnapshot qds,FirebaseFirestoreException e)
                              {
                                  ArrayList<String> id=new ArrayList<>();
                                  if(qds!=null && !qds.getDocuments().isEmpty())
                                  {
                                      for(DocumentSnapshot ds:qds.getDocuments())
                                      {
                                          id.add(ds.getId());
                                      }
                                  }
                                  for(int i=0;i<id.size();i++)
                                  FirebaseFirestore.getInstance().collection("PlacedOrders").document(id.get(i)).update("received",true)
                                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                                      @Override
                                      public void onSuccess(Void aVoid) {

                                      }
                                  });


                              }
                          });
                        }
                    }
                });
            }



    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        //this is called first
        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Thread t = new Thread(new ServiceThread(startId));
        t.start();
        Log.e("Thread","Start");

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

}

