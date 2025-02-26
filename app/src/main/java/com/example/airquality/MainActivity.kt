package com.example.airquality

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.airquality.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    private val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)

    lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAllPermissions()
    }

    private fun checkAllPermissions(){
        if(!isLocationServicesAvailable()){
            showDialogForLocationServiceSetting();
        }else{
            isRunTimePermissionsGranted();
        }
    }

    fun isLocationServicesAvailable(): Boolean{
        val locationManager = getSystemService(LOCATION_SERVICE)as
                LocationManager

        return(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    fun isRunTimePermissionsGranted(){
        val hasFIneLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if(hasFIneLocationPermission != PackageManager.PERMISSION_GRANTED||
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this@MainActivity,
            REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size ==
                REQUIRED_PERMISSIONS.size){
            var checkResult = true

            for (result in grantResults){
                if(result !=PackageManager.PERMISSION_GRANTED){
                    checkResult = false
                    break
                }
            }
            if(checkResult){
                //위칫값을 가져올 수 있음
            }else{
                //퍼미션이 거부되었다면 앱 종료료
               Toast.makeText(
                    this@MainActivity,
                   "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.",
                   Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }
    private fun showDialogForLocationServiceSetting(){
        getGPSPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ result->
        //결괏값을 받았을 때 로직
            if(result.resultCode == Activity.RESULT_OK){
                // 사용자가 GPS를 활성화 시켰는지 확인
                if(isLocationServicesAvailable()){
                    isRunTimePermissionsGranted() //런타임 권한 확인
                }else{
                    //위치 서비스가 허용되지 않아다면 앱 종료
                    Toast.makeText(
                        this@MainActivity,
                        "위치 서비스를 사용할 수 없습니다.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish() // 액티비티 종료
                }
            }
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(
        this@MainActivity) // 사용자에게 의사를 물어보는 AlertDialog 생성성
        builder.setTitle("위치 서비스 비활성화")  //제목설정
        builder.setTitle("위치 서비스가 꺼져 있습니다. 설정해야 앱을 사용할 수 있습니다.")  //내용설정
        builder.setCancelable(true) // 다이얼로그 창 바깥 터치 시 창 닫힘
        builder.setPositiveButton("설정",
            DialogInterface.OnClickListener{
            dialog, id -> //확인버튼 설정
            val callGPSSettingIntent = Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        })
        builder.setNegativeButton("취소", //취소 버튼 설정
            DialogInterface.OnClickListener{
                dialog, id ->
                dialog.cancel()
                Toast.makeText(this@MainActivity,
                "기기에서 위치서비스(GPS) 설정 후 사용해주세요.",
                Toast.LENGTH_SHORT).show()
                finish()
            })
        builder.create().show()  // 다이얼로그 생성 및 보여주기
    }
}