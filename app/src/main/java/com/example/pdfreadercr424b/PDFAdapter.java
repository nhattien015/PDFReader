package com.example.pdfreadercr424b;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.media.ThumbnailUtils;
import android.os.ParcelFileDescriptor;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
public class PDFAdapter extends ArrayAdapter<File> {
    Context context;
    ViewHolder viewHolder;
    ArrayList<File> pdfList;

    public PDFAdapter(Context context, ArrayList<File> pdfList) {
        super(context, R.layout.adapter_pdf, pdfList);
        this.context = context;
        this.pdfList = pdfList;

    }

    public Bitmap getThumbnailPdfBitmap(File pdfFile){
        try{
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfiumCore pdfiumCore = new PdfiumCore(context);
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pdfiumCore.openPage(pdfDocument, 0);
            int width = pdfiumCore.getPageWidthPoint(pdfDocument, 0);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, 0);
            Bitmap pdfBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            pdfiumCore.renderPageBitmap(pdfDocument, pdfBitmap,0, 0, 0, pdfBitmap.getWidth(), pdfBitmap.getHeight());
            return pdfBitmap;
        }catch(Exception err){
            return BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.book_icon);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        if (pdfList.size() > 0) {
            return pdfList.size();
        } else {
            return 1;
        }
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_pdf, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.filename = (TextView) view.findViewById(R.id.filename);
            viewHolder.image = (ImageView) view.findViewById(R.id.image);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();

        }

        viewHolder.filename.setText(pdfList.get(position).getName());

        viewHolder.image.setImageBitmap(this.getThumbnailPdfBitmap(pdfList.get(position)));
        return view;

    }

    public class ViewHolder {

        TextView filename;
        ImageView image;
    }

}