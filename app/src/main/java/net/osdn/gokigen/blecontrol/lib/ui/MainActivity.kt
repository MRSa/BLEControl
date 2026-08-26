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
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import net.osdn.gokigen.blecontrol.lib.ble.R
import kotlin.text.toInt

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, " ----- onCreate() -----")

        // Edge-to-Edge の有効化
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ActivityResult のランチャー登録（CREATED 状態より前に実行）
        setupPermissionLauncher()

        // UI 初期化と WindowInsets（システムバー余白）の適用
        initUi()

        // パーミッションの確認と要求
        checkAndRequestPermissions()
    }

    private fun setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val isAllGranted = permissions.entries.all { it.value }
            if (isAllGranted) {
                Log.v(TAG, "All required permissions granted.")
            } else {
                Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
                Log.v(TAG, "----- APPLICATION LAUNCH ABORTED -----")
                finish()
            }
        }
    }

    private fun initUi() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setTitle(R.string.app_name)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val appBarLayout = findViewById<com.google.android.material.appbar.AppBarLayout>(R.id.app_bar_layout) // または AppBar の ID
        val fab = findViewById<FloatingActionButton>(R.id.wifi)

        // WindowInsetsListener の設定
        ViewCompat.setOnApplyWindowInsetsListener(drawer) { _, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            // 1. AppBar に上部の余白（ステータスバー分）を追加
            appBarLayout?.updatePadding(top = insets.top)

            // 2. FAB（またはメインコンテンツ）に下部のマージン/余白を追加
            fab.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom + 16.dpToPx() // 元々の margin (16dpなど) に加算
            }

            // 3. ナビゲーションドロワー（引き出しメニュー）の余白調整
            navigationView.updatePadding(
                top = insets.top,
                bottom = insets.bottom
            )

            windowInsets
        }

        fab.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch WiFi settings", e)
            }
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_fv100, R.id.nav_home, R.id.nav_gallery,
                R.id.nav_slideshow, R.id.nav_tools, R.id.nav_settings_bluetooth
            ),
            drawer
        )

        val navController = findNavController(this, R.id.nav_host_fragment)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = getRequiredPermissions()
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            Log.v(TAG, "Requesting permissions: $missingPermissions")
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.Q..<Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        return permissions
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment)
        return navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_exit) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    // dp を px に変換する拡張関数
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}



/*
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?)
    {
        Log.v(TAG, " ----- onCreate() -----")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ActivityResult のランチャーは onCreate の最初期に登録する
        setupPermissionLauncher()

        // UI 初期化
        initUi()

        // パーミッションの確認と要求
        checkAndRequestPermissions()
    }

    private fun setupPermissionLauncher()
    {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // 要求した権限のうち、いずれかが拒否されているか確認
            val isAllGranted = permissions.entries.all { it.value }
            if (isAllGranted) {
                Log.v(TAG, "All required permissions granted.")
            } else {
                Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
                Log.v(TAG, "----- APPLICATION LAUNCH ABORTED -----")
                finish()
            }
        }
    }

    private fun initUi()
    {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setTitle(R.string.app_name)

        val fab = findViewById<FloatingActionButton>(R.id.wifi)
        fab.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch WiFi settings", e)
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_fv100, R.id.nav_home, R.id.nav_gallery,
                R.id.nav_slideshow, R.id.nav_tools, R.id.nav_settings_bluetooth
            ),
            drawer
        )

        val navController = findNavController(this, R.id.nav_host_fragment)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)
    }

    private fun checkAndRequestPermissions()
    {
        val requiredPermissions = getRequiredPermissions()
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty())
        {
            Log.v(TAG, "Requesting permissions: $missingPermissions")
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    // 実行OSのバージョンに応じて必要な危険権限（Dangerous Permissions）の一覧を取得する
    private fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()

        // 位置情報
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // ストレージ（OSバージョン別に分岐）
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        // メディア位置情報（Android 10以上）
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)&&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)) {
            permissions.add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }

        // Bluetooth（Android 12以上）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        // Wi-Fi Nearby（Android 13以上）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        return permissions
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment)
        return navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_exit) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
*/