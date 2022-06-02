package com.example.pdfreadercr424b;

import android.Manifest;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.Inflater;

import kotlin.text.Regex;

import com.example.pdfreadercr424b.R;

public class MainActivity extends AppCompatActivity {
    ListView pdfListView;
    public static ArrayList<File> fileList = new ArrayList<File>();
    PDFAdapter pdfAdapter;
    public static int REQUEST_PERMISSIONS = 1;
    boolean boolean_permission;
    File dir;
    private Toolbar toolbar;

    public String removeAccents(String inputStr){
        return Normalizer.normalize(inputStr, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }



            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextChange(String newText) {

                Log.i("Kei", removeAccents(newText));
                Predicate<File> filterSearch = file -> (removeAccents(file.getName().toLowerCase())).contains(removeAccents(newText.toLowerCase()));


                pdfAdapter = new PDFAdapter(getApplicationContext(), (ArrayList<File>)fileList.stream().filter(filterSearch).collect(Collectors.toList()));
                pdfListView.setAdapter(pdfAdapter);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.open_specific_:
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();


    }

    private void init() {

        pdfListView = (ListView) findViewById(R.id.pdfList);
        dir = new File(Environment.getExternalStorageDirectory().toString());
        //  dir = new File(String.valueOf(Environment.getExternalStorageDirectory()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                initPermissions();
            }
        }).start();

        findViewById(R.id.scan_btn).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                initPermissions();
            }
        });
        pdfListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), PdfActivity.class);
                intent.putExtra("position", i);
                startActivity(intent);

            }
        });

    }

    public ArrayList<File> getfile(File dir) {
        Set<File> allFiles = new HashSet<File>();
        allFiles.addAll(Arrays.asList(dir.listFiles()));
        File[] files = allFiles.toArray(new File[0]);
        if (allFiles != null && allFiles.size() > 0) {
            for (int i = 0; i < allFiles.size(); i++) {

                if (files[i].isDirectory()) {
                    getfile(files[i]);

                } else {

                    if (files[i].getName().endsWith(".pdf") || files[i].getName().endsWith(".epub") && !fileList.contains(files[i])) {
                        fileList.add(files[i]);
                    }
                }
            }
        }
        return fileList;
    }
    private void initPermissions() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;

            getfile(dir);
            pdfAdapter = new PDFAdapter(getApplicationContext(), fileList);
            pdfListView.setAdapter(pdfAdapter);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == REQUEST_PERMISSIONS) {

               if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    boolean_permission = true;
                    getfile(dir);

                   pdfAdapter = new PDFAdapter(getApplicationContext(), fileList);
                    if(pdfAdapter.pdfList.size() < 0){
                        findViewById(R.id.emptyView).setVisibility(View.VISIBLE);
                    }
                    else{
                        findViewById(R.id.emptyView).setVisibility(View.INVISIBLE);
                    }
                    pdfListView.setAdapter(pdfAdapter);

                } else {
                    Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();

                }
            }

    }

}