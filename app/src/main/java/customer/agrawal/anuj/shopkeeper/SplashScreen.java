package customer.agrawal.anuj.shopkeeper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (Exception e)
                {

                }
                finally
                {
                   startActivity(new Intent(getApplicationContext(),MainActivity.class));
                   finish();
                }
            }
        }.start();

    }
}
