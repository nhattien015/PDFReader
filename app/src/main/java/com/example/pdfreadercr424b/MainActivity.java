package com.example.pdfreadercr424b;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.zip.Inflater;


import com.example.pdfreadercr424b.R;
import com.example.pdfreadercr424b.Utils.FileUtils;

public class MainActivity extends AppCompatActivity {
    final int DOCUMENT_FILE_SELECTOR_CODE = 1;
    ListView pdfListView;
    public static Set<File> fileList = new HashSet<File>();
    PDFAdapter pdfAdapter;
    public static int REQUEST_PERMISSIONS = 1;
    boolean boolean_permission;
    File dir;
    private Toolbar toolbar;


    ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.i("Hello", result.toString());
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        Log.i("Hello", uri.toString());

                        Intent pdfActivityIntent = new Intent(MainActivity.this, PdfActivity.class);
                        String filePath = FileUtils.getRealPath(getApplicationContext(), uri);

                        pdfActivityIntent.putExtra("document_file_path", filePath);
                        startActivity(pdfActivityIntent);

                    }
                }
            }
    );
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


                ArrayList<File> filterdFileList = new ArrayList<>(fileList.stream().filter(filterSearch).collect(Collectors.toList()));
                pdfAdapter = new PDFAdapter(getApplicationContext(), filterdFileList);
                pdfListView.setAdapter(pdfAdapter);
                return true;
            }
        });
        return true;
    }



    public void showFileChooser(){
        try{
            Intent documentExplorer = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            documentExplorer.setType("*/*");
            documentExplorer.addCategory(Intent.CATEGORY_OPENABLE);
            mGetContent.launch(Intent.createChooser(documentExplorer, "Choose chooser"));

        }catch(Exception err){

        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.open_specific_file_option: {
               showFileChooser();
            };
            break;
            case R.id.sort_by_name_option: {
                File[] sortedFiles = fileList.toArray(new File[0]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Arrays.sort(sortedFiles, Comparator.comparing(File::getName));
                    pdfAdapter.pdfList = (ArrayList<File>) Arrays.stream(sortedFiles).collect(Collectors.toCollection(ArrayList::new));
                    pdfListView.setAdapter(pdfAdapter);
                    item.setChecked(true);
                }
            };
            break;
            case R.id.menu_sort_by_file_size_option:{
                File[] sortedFiles = fileList.toArray(new File[0]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Arrays.sort(sortedFiles, Comparator.comparing(File::length));
                    pdfAdapter.pdfList = (ArrayList<File>) Arrays.stream(sortedFiles).collect(Collectors.toCollection(ArrayList::new));
                    pdfListView.setAdapter(pdfAdapter);
                    item.setChecked(true);
                }
            };
            break;
            case R.id.menu_sort_by_modified_time_option: {
                File[] sortedFiles = fileList.toArray(new File[0]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Arrays.sort(sortedFiles, Comparator.comparing(File::lastModified));
                    pdfAdapter.pdfList = (ArrayList<File>) Arrays.stream(sortedFiles).collect(Collectors.toCollection(ArrayList::new));
                    pdfListView.setAdapter(pdfAdapter);
                    item.setChecked(true);
                }
            };
            break;
            default: break;
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

        initPermissions();

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
        return new ArrayList<>(fileList);
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
            pdfAdapter = new PDFAdapter(getApplicationContext(), new ArrayList<File>(fileList));
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

                   pdfAdapter = new PDFAdapter(getApplicationContext(), new ArrayList<File>(fileList));
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