package com.cieep.ejemplo09_permisos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
/*No hay que olvidar que los permisos se realizan en el manifest*/

    private EditText txtNumTel;
    private ImageButton btnLlamar;
    private ImageView imgCamara;
    private Button btnTakeSave;
    private String currentPhotoPath; // URI al archivo creado
    private Button btnOpenGallery;
    private Button btnLocattion;
    private TextView txtDireccion, txtDireccion2;
    private TextView txtCooredenadas;

    // requestCode de los permisos
    private final int CALL_PERMISSION = 100;
    private final int CAMERA_PERMISSION = 101;
    private final int TAKE_SAVE_PERMISION = 102;
    private final int OPEN_GALLERY_PERMISION = 103;
    private final int LOCATTION_PERMISION = 104;

    // request para devolver info
    private final int CAMARA_ACTION = 1;
    private final int TAKE_SAVE_ACTION = 2;
    private final int OPEN_GALLERY_ACTION = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtNumTel = findViewById(R.id.txtNumTelefono);
        btnLlamar = findViewById(R.id.btnLlamar);
        imgCamara = findViewById(R.id.imgCamara);

        btnTakeSave = findViewById(R.id.btnTakeSave);
        btnOpenGallery = findViewById(R.id.btnOpenGalleryAction);
        btnLocattion = findViewById(R.id.btnGetLocattionAction);

        txtDireccion = findViewById(R.id.txtDireccion);
        txtDireccion2 = findViewById(R.id.txtDireccion2);
        txtCooredenadas = findViewById(R.id.txtCoordenadas);

        btnLlamar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtNumTel.getText().toString().isEmpty()){
                    // Comprobar que versión de Android uso
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                   /* esta condicion viene a decir q estoy en una version anterior al andoid 6, y
                   por tanto no se hace necesario realizar los permisos   */
                        llamadaAction();
                    }
                    else{
                        /* Android 6 o posterior, por tanto los permisos hay que solicitarlo a mano */
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) ==
                                PackageManager.PERMISSION_GRANTED){
                            // en el contexto de esta aplicación, si tengo permisos no los vuelvo a pedir
                            llamadaAction();
                        }
                        else {
                            // sino lo solicitamos
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CALL_PHONE},
                                    CALL_PERMISSION);
                        }
                    }
                }
            }
        });

        imgCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    camaraAction();
                }
                else{
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED){
                        camaraAction();
                    }
                    else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},
                                CAMERA_PERMISSION);
                    }
                }
            }
        });

        btnTakeSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    // Compruebo la versión de Android, si la version es anterior al 6
                    takeSaveAction();
                }
                else {
                    // Comprueba si tengo permisos ya concedidos
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED) {
                        takeSaveAction();
                    }
                    else {
                        // Pide los permisos
                        String[] permisos = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(MainActivity.this, permisos, TAKE_SAVE_PERMISION);
                    }
                }
            }
        });

        btnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    // Compruebo la versión de Android
                    openGalleryAction();
                }
                else {
                    // Comprueba si tengo permisos ya concedidos
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED) {
                        openGalleryAction();
                    }
                    else {
                        // Pide los permisos
                        String[] permisos = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(MainActivity.this, permisos, OPEN_GALLERY_PERMISION);
                    }
                }
            }
        });

        btnLocattion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // Compruebo la versión de Android
                    getLocattionAction();
                }
                else {
                    // Comprueba si tengo permisos ya concedidos
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {
                        getLocattionAction();
                    }
                    else { // Pide los permisos
                        String[] permisos = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                        ActivityCompat.requestPermissions(MainActivity.this, permisos, LOCATTION_PERMISION);
                    }
                }
            }
        });
    }


    private void llamadaAction() {
        Log.d("LLAMADA", "LLAMANDO");
        Intent intentTelefono = new Intent(Intent.ACTION_CALL);
        intentTelefono.setData(Uri.parse("tel: "+txtNumTel.getText().toString()));
        startActivity(intentTelefono);
    }

    private void camaraAction() {
        Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intentCamara, CAMARA_ACTION);
    }


    // Ante de crear una imagen, hay que crear un fichero vacio, el cual albergará la imagen
    // y se le asignará un nombre lina 203, ademas de darle un formato
    private File crearFichero() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_"+timeStamp+"_";
        File directoryPictures = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, // Nombre de la Imagen
                ".jpg", // Extensión
                directoryPictures // Carpeta de almacenamiento
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void takeSaveAction(){
        // 1. Crear Fichero Vacio -> para ello nos creamos la funcio crearFichero()
        try {
            File photoFile = crearFichero();
            if (photoFile != null){
                Uri uriPhotoFile = FileProvider.getUriForFile(this, "com.cieep.ejemplo09_permisos", photoFile);
                Intent intentTakeSave = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intentTakeSave.putExtra(MediaStore.EXTRA_OUTPUT, uriPhotoFile);
                startActivityForResult(intentTakeSave, TAKE_SAVE_ACTION);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void openGalleryAction(){
        Intent intentOpenGallery = new Intent(Intent.ACTION_GET_CONTENT);
        intentOpenGallery.setType("image/*");
        startActivityForResult(intentOpenGallery, OPEN_GALLERY_ACTION);
    }

    /*   Método que obtiene la localización del terminal    */
    private void getLocattionAction(){
        // Objeto que tiene acceso a la localización
        LocationManager nLocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        /* @SuppressLint("MissingPermission")-> SED HA REALIZADDO POR QUE EL EDITOR ANDROID STUDIO, VE UN ERROR
        * AL NO DETECTAR LOS PERMISOS EN EL ONREQUESPERMISSIONS, LINA 305,....*/
        @SuppressLint("MissingPermission") Location loc = nLocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(loc != null){
            if(loc.getLatitude() !=0 && loc.getLongitude() != 0){
                txtCooredenadas.setText("LONG:" + loc.getLongitude()+ " LATITUD: " + loc.getLatitude());

                //Geocoder-> Trasnformar una lon y latitud en una direccion tecto
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                // Geocoder devuelve unalista de direcciones
                try {
                    List<Address> direcciones = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                    if( !direcciones.isEmpty()){
                        txtDireccion.setText(direcciones.get(0).getAddressLine(0).toString());
//                        txtDireccion2.setText(direcciones.get(0).getSubLocality().toString());
                    }else{
                        txtDireccion.setText("NO he encontrado dirección");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }else {
            txtCooredenadas.setText("NO tienes localización");
        }
    }

    /**
     * Se ejecuta justo despues de que el usuario conteste a los permisos
     * @param requestCode -> codigo de la petición de los permisos
     * @param permissions -> String[] con los permisos que se han solicitado
     * @param grantResults -> int[] con los resultados de las peticiones de cada permniso
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Para gestion de las llamadas
        if (requestCode == CALL_PERMISSION){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                llamadaAction();
            }
            else {
                Toast.makeText(this, "No puedo llamar sin permisos", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CAMERA_PERMISSION){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                camaraAction();
            }
            else {
                Toast.makeText(this, "No puedo usar la camara sin permisos sin permisos", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == TAKE_SAVE_PERMISION){
            // OJO que ahora tengo que comprobar 2 permisos la camara y la escritura
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takeSaveAction();
            }
            else {
                Toast.makeText(this, "No puedo hacer nada sin permisos", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == OPEN_GALLERY_PERMISION){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGalleryAction();
            }
            else {
                Toast.makeText(this, "No puedo hacer nada sin permisos", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == LOCATTION_PERMISION){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
               getLocattionAction();
            }
            else {
                Toast.makeText(this, "No puedo hacer nada sin permisos", Toast.LENGTH_SHORT).show();
            }
        }
    }

// función para recoger la foto tomada con la cámara
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMARA_ACTION && resultCode == RESULT_OK && data != null){

            Bundle bundle = data.getExtras(); // Ojito, no realizar un new Bundle(); -> ersultado = null
            Bitmap imageBitmap = (Bitmap) bundle.get("data");
            imgCamara.setImageBitmap(imageBitmap);
            Log.d("CAMERA", "CAMARA OK");
        }

        if (requestCode == TAKE_SAVE_ACTION && resultCode == RESULT_OK) {
//            Log.d("salvado:  >", currentPhotoPath);

            imgCamara.setImageURI(Uri.parse(currentPhotoPath));
            // Si quiero Guardar la foto en la biblioteca OJO QUE NO VA

            Intent intentMediaScan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(currentPhotoPath);
            intentMediaScan.setData(Uri.fromFile(f));
            this.sendBroadcast(intentMediaScan);
        }

        if (requestCode == OPEN_GALLERY_ACTION && resultCode == RESULT_OK && data != null){
            Uri uriFile = data.getData();
            Toast.makeText(MainActivity.this, "salvado:  >" +uriFile, Toast.LENGTH_SHORT).show();
            imgCamara.setImageURI(uriFile);
        }
    }
}