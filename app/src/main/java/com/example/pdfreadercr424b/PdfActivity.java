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
import java.util.List;

public class PdfActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener {

    PDFView pdfView;
    Integer pageNumber = 0;
    String pdfFileName;
    String TAG="PdfActivity";
    int position=-1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        init();
    }

    private void init(){
        pdfView = (PDFView)findViewById(R.id.pdfView);
        position = getIntent().getIntExtra("position",-1);
        displayFromSdcard();
    }

    private void displayFromSdcard() {
        pdfFileName = MainActivity.fileList.get(position).getName();
        File pdfFile = MainActivity.fileList.get(position);
        try{
            if(pdfFile.exists()){
                pdfView.fromFile(pdfFile)
                        .defaultPage(pageNumber)
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
}