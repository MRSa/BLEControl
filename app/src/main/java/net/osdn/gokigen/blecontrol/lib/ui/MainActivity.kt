package net.osdn.gokigen.blecontrol.lib.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import net.osdn.gokigen.blecontrol.lib.ble.R

class MainActivity : AppCompatActivity()
{
    private lateinit var mAppBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?)
    {
        Log.v(TAG, " ----- onCreate() -----")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try
        {
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            setTitle(R.string.app_name)

            val fab = findViewById<FloatingActionButton>(R.id.wifi)
            fab.setOnClickListener {
                try
                {
                    // Launch WIFI Settings
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            val navigationView = findViewById<NavigationView>(R.id.nav_view)
            mAppBarConfiguration = AppBarConfiguration.Builder(
                R.id.nav_fv100, R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_settings_bluetooth
            ).setDrawerLayout(drawer).build()

            val navController = findNavController(this, R.id.nav_host_fragment)
            setupActionBarWithNavController(this, navController, mAppBarConfiguration)
            setupWithNavController(navigationView, navController)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

        try
        {
            ///////// SET PERMISSIONS /////////
            Log.v(TAG, " ----- SET PERMISSIONS -----")
            if (!allPermissionsGranted())
            {
                val requestPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

                    ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
                    if(!allPermissionsGranted())
                    {
                        // Abort launch application because required permissions was rejected.
                        Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
                        Log.v(TAG, "----- APPLICATION LAUNCH ABORTED -----")
                        finish()
                    }
                }
                requestPermission.launch(REQUIRED_PERMISSIONS)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun allPermissionsGranted() : Boolean
    {
        var result = true
        for (param in REQUIRED_PERMISSIONS)
        {
            if (ContextCompat.checkSelfPermission(
                    baseContext,
                    param
                ) != PackageManager.PERMISSION_GRANTED
            )
            {
                // ----- Permission Denied...
                if ((param == Manifest.permission.ACCESS_MEDIA_LOCATION)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q))
                {
                    //　この場合は権限付与の判断を除外 (デバイスが (10) よりも古く、ACCESS_MEDIA_LOCATION がない場合）
                }
                else if ((param == Manifest.permission.READ_EXTERNAL_STORAGE)&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU))
                {
                    // この場合は、権限付与の判断を除外 (SDK: 33以上はエラーになる...)
                }
                else if ((param == Manifest.permission.WRITE_EXTERNAL_STORAGE)&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU))
                {
                    // この場合は、権限付与の判断を除外 (SDK: 33以上はエラーになる...)
                }
                else if ((param == Manifest.permission.BLUETOOTH_SCAN)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.S))
                {
                    // この場合は、権限付与の判断を除外 (SDK: 31よりも下はエラーになるはず...)
                    Log.v(TAG, "BLUETOOTH_SCAN")
                }
                else if ((param == Manifest.permission.BLUETOOTH_CONNECT)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.S))
                {
                    // この場合は、権限付与の判断を除外 (SDK: 31よりも下はエラーになるはず...)
                    Log.v(TAG, "BLUETOOTH_CONNECT")
                }
                else
                {
                    // ----- 権限が得られなかった場合...
                    Log.v(TAG, " Permission: $param : ${Build.VERSION.SDK_INT}")
                    result = false
                }
            }
        }
        return (result)
    }

    override fun onSupportNavigateUp(): Boolean
    {
        val navController = findNavController(this, R.id.nav_host_fragment)
        return (navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        menuInflater.inflate(R.menu.main, menu)
        return (true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        try
        {
            val id = item.itemId
            if (id == R.id.action_exit)
            {
                exitApplication()
                return (true)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (super.onOptionsItemSelected(item))
    }

    private fun exitApplication()
    {
        finish()
    }

    companion object
    {
        private val TAG = MainActivity::class.java.simpleName

        private const val REQUEST_CODE_PERMISSIONS = 1010

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    }
}