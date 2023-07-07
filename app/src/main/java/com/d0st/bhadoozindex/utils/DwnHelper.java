package com.d0st.bhadoozindex.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.d0st.bhadoozindex.R;
import com.google.android.material.snackbar.Snackbar;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2core.Downloader;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;

import java.io.File;
import java.text.DecimalFormat;

public class DwnHelper {

    public static FetchConfiguration getConfiguration (Context context){
        return new FetchConfiguration.Builder(context)
                .setDownloadConcurrentLimit(1)
                .setProgressReportingInterval(3000)
//              .enableHashCheck(true)
                .createDownloadFileOnEnqueue(false)
                .enableLogging(true)
                .enableAutoStart(false)
                .preAllocateFileOnCreation(false)
                .setGlobalNetworkType(NetworkType.ALL)
                .enableFileExistChecks(true)
                .enableRetryOnNetworkGain(true)
//                .setHttpDownloader(new OkHttpDownloader())
                .setNamespace("MvilaaDownload")
                .build();
    }

    @SuppressLint("Range")
//    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void startDownload(Context context, View view, String name, String url,int groupId) {
        Fetch fetch;
        fetch = Fetch.Impl.getInstance(getConfiguration(context));

        File dDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filename = dDirectory + "/" + name;
        com.tonyodev.fetch2.Request request = new com.tonyodev.fetch2.Request(url, filename);
        request.setPriority(Priority.HIGH);
        request.setGroupId(groupId);
        request.setTag(String.valueOf(groupId));
        request.setNetworkType(NetworkType.ALL);

        fetch.enqueue(request, updatedRequest -> {
//            Snackbar snackbar = Snackbar.make(view, "Downloading Started!", Snackbar.LENGTH_INDEFINITE);
//            snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.my));
//            snackbar.setAction("Show", v -> {
////            Intent intent = new Intent(context, DwnActivity.class);
////            context.startActivity(intent);
//        });
//            snackbar.show();

        }, error -> {
            //An error occurred enqueuing the request.
            Snackbar snackbar = Snackbar.make(view, "Downloading Failed | Give us Storage Permission", Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(ContextCompat.getColor(context,R.color.red));
            snackbar.setAction("Close", v -> snackbar.dismiss());
            snackbar.show();
        });
    }

    @NonNull
    public static String getFilePath(@NonNull final String url) {
        final Uri uri = Uri.parse(url);
        return (uri.getLastPathSegment());
    }

    public static int getColorWrapper(Context context, int id){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return context.getColor(id);
        }else {
            return context.getResources().getColor(id);
        }
    }

    @NonNull
    public static String getETAString(@NonNull final Context context, final long etaInMilliSeconds) {
        if (etaInMilliSeconds < 0) {
            return "";
        }
        int seconds = (int) (etaInMilliSeconds / 1000);
        long hours = seconds / 3600;
        seconds -= hours * 3600;
        long minutes = seconds / 60;
        seconds -= minutes * 60;
        if (hours > 0) {
            return context.getString(R.string.download_eta_hrs, hours, minutes, seconds);
        } else if (minutes > 0) {
            return context.getString(R.string.download_eta_min, minutes, seconds);
        } else {
            return context.getString(R.string.download_eta_sec, seconds);
        }
    }

    public static void deleteFileAndContents(@NonNull final File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                final File[] contents = file.listFiles();
                if (contents != null) {
                    for (final File content : contents) {
                        deleteFileAndContents(content);
                    }
                }
            }
            file.delete();
        }
    }

    @NonNull
    public static String getDownloadSpeedString(@NonNull final Context context, final long downloadedBytesPerSecond) {
        if (downloadedBytesPerSecond < 0) {
            return "";
        }
        double kb = (double) downloadedBytesPerSecond / (double) 1000;
        double mb = kb / (double) 1000;
        final DecimalFormat decimalFormat = new DecimalFormat(".##");
        if (mb >= 1) {
            return context.getString(R.string.download_speed_mb, decimalFormat.format(mb));
        } else if (kb >= 1) {
            return context.getString(R.string.download_speed_kb, decimalFormat.format(kb));
        } else {
            return context.getString(R.string.download_speed_bytes, downloadedBytesPerSecond);
        }
    }

}
