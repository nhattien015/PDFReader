package com.example.pdfreadercr424b;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PdfActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener {

    PDFView pdfView;
    Integer pageNumber = 0;
    String pdfFileName;
    String TAG="PdfActivity";
    int position=-1;
    private static PdfActivity instance;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        init();
    }

    private void init(){
        pdfView = (PDFView)findViewById(R.id.pdfView);
        position = getIntent().getIntExtra("position",-1);
        String filePath = getIntent().getStringExtra("document_file_path");

        if(this.position >= 0){
            displayFromSdcard();
        }
        else if(filePath != null){
            File documentFile = new File(filePath);
            Log.i("Kei", filePath);
            Log.i("Kei", String.valueOf(documentFile));
            loadFromFile(documentFile);
        }
    }


    private void loadFromFile(File file){
        pdfFileName = file.getName();
        try{
            if(file.exists()){
                pdfView.fromFile(file)
                        .defaultPage(0)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .onPageChange(this)
                        .enableAnnotationRendering(true)
                        .onLoad(this)
                        .scrollHandle(new DefaultScrollHandle(this))
                        .load();
            }
        }catch(Exception err){
            Toast toast = Toast.makeText(PdfActivity.this, err.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    private void displayFromSdcard() {
        pdfFileName = new ArrayList<>(MainActivity.fileList).get(position).getName();
        File pdfFile = new ArrayList<>(MainActivity.fileList).get(position);
        Log.i("Hello World", pdfFile.getPath());
        loadFromFile(pdfFile);
    }
    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }


    static public PdfActivity getInstance(){
        if(PdfActivity.instance == null){
            PdfActivity.instance = new PdfActivity();
        }
        return PdfActivity.instance;
    }
}