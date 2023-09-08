package com.github.ghmxr.apkextractor.tasks;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.PermissionChecker;
import androidx.documentfile.provider.DocumentFile;

import com.github.ghmxr.apkextractor.Constants;
import com.github.ghmxr.apkextractor.Global;
import com.github.ghmxr.apkextractor.items.AppItem;
import com.github.ghmxr.apkextractor.items.FileItem;
import com.github.ghmxr.apkextractor.items.ImportItem;
import com.github.ghmxr.apkextractor.utils.DocumentFileUtil;
import com.github.ghmxr.apkextractor.utils.EnvironmentUtil;
import com.github.ghmxr.apkextractor.utils.FileUtil;
import com.github.ghmxr.apkextractor.utils.OutputUtil;
import com.github.ghmxr.apkextractor.utils.SPUtil;
import com.github.ghmxr.apkextractor.utils.StorageUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportTask extends Thread {

    private final Context context;
    private final List<AppItem> list;
    private ExportProgressListener listener;
    /**
     * 本次导出任务的目的存储路径是否为外置存储
     */
    private final boolean isExternal;
    private volatile boolean isInterrupted = false;
    private long progress = 0, total = 0;
    private long progress_check_zip = 0;
    private long zipTime = 0;
    private long zipWriteLength_second = 0;

    private FileItem currentWritingFile = null;
    private String currentWritingPath = null;

    private final ArrayList<FileItem> write_paths = new ArrayList<>();
    private final StringBuilder error_message = new StringBuilder();

    Map<AppItem, GetDataObbTask.DataObbSizeInfo> dataObbSizeMap;

    /**
     * 导出任务构造方法
     *
     * @param list     要导出的AppItem集合
     * @param callback 任务进度回调，在主UI线程
     */
    public ExportTask(@NonNull Context context, @NonNull List<AppItem> list, @Nullable ExportProgressListener callback) {
        super();
        this.context = context;
        this.list = list;
        this.listener = callback;
        isExternal = SPUtil.getIsSaved2ExternalStorage(context);
    }

    public void setExportProgressListener(ExportProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            //初始化File导出路径
            if (!isExternal) {
                File export_path = new File(SPUtil.getInternalSavePath(context));
                if (!export_path.exists()) {
                    export_path.mkdirs();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null)
                listener.onExportTaskFinished(new ArrayList<FileItem>(), e.toString());
            return;
        }
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        new GetDataObbTask(list, new GetDataObbTask.DataObbSizeGetCallback() {
            @Override
            public void onDataObbSizeGet(List<AppItem> containsData, List<AppItem> containsObb, Map<AppItem, GetDataObbTask.DataObbSizeInfo> map, GetDataObbTask.DataObbSizeInfo dataObbSizeInfo) {
                dataObbSizeMap = map;
                countDownLatch.countDown();
            }
        }).start();
        try {
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
            if (isInterrupted) return;
        }
        total = getTotalLength();
        long progress_check_apk = 0;
        long bytesPerSecond = 0;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < list.size(); i++) {
            if (isInterrupted) break;
            try {
                final AppItem item = list.get(i);
                final int order_this_loop = i + 1;

                if (!item.exportData && !item.exportObb) {

                    OutputStream outputStream;
                    if (isExternal) {
                        this.currentWritingPath = SPUtil.getDisplayingExportPath(context) + "/" + OutputUtil.getWriteFileNameForAppItem(context, item, "apk", i + 1);
                        DocumentFile documentFile = OutputUtil.getWritingDocumentFileForAppItem(context, item, "apk", i + 1);
                        this.currentWritingFile = FileItem.createFileItemInstance(documentFile);
                        outputStream = OutputUtil.getOutputStreamForDocumentFile(context, documentFile);
                    } else {
                        String writePath = OutputUtil.getAbsoluteWritePath(context, item, "apk", i + 1);
                        this.currentWritingPath = writePath;
                        this.currentWritingFile = FileItem.createFileItemInstance(writePath);
                        outputStream = new FileOutputStream(writePath);
                    }

                    Global.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null)
                                listener.onExportAppItemStarted(order_this_loop, item, list.size(), String.valueOf(currentWritingPath));
                        }
                    });

                    InputStream in = new FileInputStream(String.valueOf(item.getSourcePath())); //读入原文件

                    BufferedOutputStream out = new BufferedOutputStream(outputStream);

                    int byteread;
                    byte[] buffer = new byte[1024 * 10];
                    while ((byteread = in.read(buffer)) != -1 && !this.isInterrupted) {
                        out.write(buffer, 0, byteread);
                        progress += byteread;
                        bytesPerSecond += byteread;
                        long endTime = System.currentTimeMillis();
                        if ((endTime - startTime) > 1000) {
                            startTime = endTime;
                            final long speed = bytesPerSecond;
                            bytesPerSecond = 0;
                            /*Long speed=Long.valueOf(bytesPerSecond);
				            	 bytesPerSecond=0;
				            	 Message msg_speed = new Message();
				            	 msg_speed.what=BaseActivity.MESSAGE_COPYFILE_REFRESH_SPEED;
				            	 msg_speed.obj=speed;
				            	 BaseActivity.sendMessage(msg_speed);*/
                            Global.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (listener != null) listener.onExportSpeedUpdated(speed);
                                }
                            });

                        }

                        if ((progress - progress_check_apk) > 100 * 1024) {   //每写100K发送一次更新进度的Message
                            progress_check_apk = progress;
                            /*Message msg_progress=new Message();
                            Long progressinfo[]  = new Long[]{Long.valueOf(progress),Long.valueOf(total)};
                            msg_progress.what=BaseActivity.MESSAGE_COPYFILE_REFRESH_PROGRESS;
                            msg_progress.obj=progressinfo;
                            BaseActivity.sendMessage(msg_progress);*/
                            Global.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (listener != null)
                                        listener.onExportProgressUpdated(progress, total, String.valueOf(currentWritingPath));
                                }
                            });
                        }
                    }
                    out.flush();
                    in.close();
                    out.close();
                } else {
                    OutputStream outputStream;
                    if (isExternal) {
                        DocumentFile documentFile = OutputUtil.getWritingDocumentFileForAppItem(context, item, SPUtil.getCompressingExtensionName(context), i + 1);
                        this.currentWritingFile = FileItem.createFileItemInstance(documentFile);
                        this.currentWritingPath = SPUtil.getDisplayingExportPath(context) + "/" + documentFile.getName();
                        outputStream = OutputUtil.getOutputStreamForDocumentFile(context, documentFile);
                    } else {
                        String writePath = OutputUtil.getAbsoluteWritePath(context, item, SPUtil.getCompressingExtensionName(context), i + 1);
                        this.currentWritingFile = FileItem.createFileItemInstance(writePath);
                        this.currentWritingPath = writePath;
                        outputStream = new FileOutputStream(writePath);
                    }
                    Global.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null)
                                listener.onExportAppItemStarted(order_this_loop, item, list.size(), currentWritingFile.getPath());
                        }
                    });

                    ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(outputStream));
                    zos.setComment("Packaged by com.github.ghmxr.apkextractor \nhttps://github.com/ghmxr/apkextractor");
                    int zip_level = SPUtil.getGlobalSharedPreferences(context).getInt(Constants.PREFERENCE_ZIP_COMPRESS_LEVEL, Constants.PREFERENCE_ZIP_COMPRESS_LEVEL_DEFAULT);

                    if (zip_level >= 0 && zip_level <= 9) zos.setLevel(zip_level);

                    writeZip(item.getFileItem(), "", zos, zip_level);

                    if (Build.VERSION.SDK_INT < Global.USE_DOCUMENT_FILE_SDK_VERSION && PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
                        if (item.exportData) {
                            writeZip(FileItem.createFileItemInstance(new File(StorageUtil.getMainExternalStoragePath() + "/android/data/" + item.getPackageName())), "Android/data/", zos, zip_level);
                        }
                        if (item.exportObb) {
                            writeZip(FileItem.createFileItemInstance(new File(StorageUtil.getMainExternalStoragePath() + "/android/obb/" + item.getPackageName())), "Android/obb/", zos, zip_level);
                        }
                    } else if (Build.VERSION.SDK_INT < Global.USE_STANDALONE_DOCUMENT_FILE_PERMISSION) {
                        if (item.exportData) {
                            FileItem dataFileItem = null;
                            try {
                                dataFileItem = FileItem.createFileItemInstance(DocumentFileUtil.getDocumentFileBySegments(DocumentFileUtil.getDataDocumentFile(), item.getPackageName(), false));
                            } catch (Exception e) {
                                Log.i(getClass().getSimpleName(), String.valueOf(e));
                            }
                            if (dataFileItem != null) {
                                writeZip(dataFileItem, "Android/data/", zos, zip_level);
                            }
                        }
                        if (item.exportObb) {
                            FileItem obbFileItem = null;
                            try {
                                obbFileItem = FileItem.createFileItemInstance(DocumentFileUtil.getDocumentFileBySegments(DocumentFileUtil.getObbDocumentFile(), item.getPackageName(), false));
                            } catch (Exception e) {
                                Log.i(getClass().getSimpleName(), String.valueOf(e));
                            }
                            if (obbFileItem != null) {
                                writeZip(obbFileItem, "Android/obb/", zos, zip_level);
                            }
                        }
                    } else {
                        if (item.exportData) {
                            FileItem dataFileItem = null;
                            try {
                                dataFileItem = FileItem.createFileItemInstance(DocumentFileUtil.getDataDocumentFileOf(item.getPackageName()));
                            } catch (Exception e) {
                                Log.i(getClass().getSimpleName(), String.valueOf(e));
                            }
                            if (dataFileItem != null) {
                                writeZip(dataFileItem, "Android/data/", zos, zip_level);
                            }
                        }
                        if (item.exportObb) {
                            FileItem obbFileItem = null;
                            try {
                                obbFileItem = FileItem.createFileItemInstance(DocumentFileUtil.getObbDocumentFileOf(item.getPackageName()));
                            } catch (Exception e) {
                                Log.i(getClass().getSimpleName(), String.valueOf(e));
                            }
                            if (obbFileItem != null) {
                                writeZip(obbFileItem, "Android/obb/", zos, zip_level);
                            }
                        }
                    }

                    zos.flush();
                    zos.close();
                }
                write_paths.add(currentWritingFile);
                if (!isInterrupted) currentWritingFile = null;


            } catch (Exception e) {
                e.printStackTrace();
                this.error_message.append(currentWritingPath);
                this.error_message.append(":");
                this.error_message.append(e.toString());
                this.error_message.append("\n\n");
                try {
                    currentWritingFile.delete();//在写入中如果有异常就尝试删除这个文件，有可能是破损的
                } catch (Exception ee) {
                }
            }

        }

        if (isInterrupted) {
            try {
                currentWritingFile.delete();//没有写入完成的文件为破损文件，尝试删除
            } catch (Exception e) {
            }
        }

        if (progress > total) {//data,obb大小发生变化
            GetDataObbTask.clearDataObbSizeCache();
        }

        //更新导出文件到媒体库
        EnvironmentUtil.requestUpdatingMediaDatabase(context);

        //向全局列表添加导出实例
        ArrayList<ImportItem> exported = new ArrayList<>();
        for (FileItem fileItem : write_paths) {
            exported.add(new ImportItem(fileItem));
        }

        //清除对应导出item的缓存(如果存在)
        for (ImportItem importItem : exported) {
            Global.clearZipInfoCacheOfImportItemByPath(importItem.getFileItem().getPath());
            GetPackageInfoViewTask.clearPackageInfoCacheOfPath(importItem.getFileItem().getPath());
            HashTask.clearResultCacheOfPath(importItem.getFileItem().getPath());
            GetApkLibraryTask.clearOutsidePackageCacheOfPath(importItem.getFileItem().getPath());
            GetSignatureInfoTask.clearCacheOfPath(importItem.getFileItem().getPath());
        }

        Global.item_list.addAll(exported);
        Collections.sort(Global.item_list);

        Global.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (listener != null && !isInterrupted)
                    listener.onExportTaskFinished(write_paths, error_message.toString());
//                context.sendBroadcast(new Intent(Constants.ACTION_REFRESH_IMPORT_ITEMS_LIST));
                context.sendBroadcast(new Intent(Constants.ACTION_REFRESH_AVAILIBLE_STORAGE));
                context.sendBroadcast(new Intent(Constants.ACTION_REFILL_IMPORT_LIST));
            }
        });

    }


    /**
     * 获取本次导出的总计长度
     *
     * @return 总长度，字节
     */
    private long getTotalLength() {
        long total = 0;
        for (final AppItem item : list) {
            total += item.getSize();
            if (dataObbSizeMap == null) continue;
            GetDataObbTask.DataObbSizeInfo dataObbSizeInfo = dataObbSizeMap.get(item);
            if (dataObbSizeInfo != null) {
                if (item.exportData) total += dataObbSizeInfo.data;
                if (item.exportObb) total += dataObbSizeInfo.obb;
            }
        }
        return total;
    }

    /**
     * 将本Runnable停止，删除当前正在导出而未完成的文件，使线程返回
     */
    public void setInterrupted() {
        this.isInterrupted = true;
        interrupt();
    }

    private void writeZip(final FileItem fileItem, String parent, ZipOutputStream zos, final int zip_level) {
        if (fileItem == null || parent == null || zos == null) return;
        if (isInterrupted) return;
        if (!fileItem.exists()) return;
        if (fileItem.isDirectory()) {
            parent += fileItem.getName() + File.separator;
            List<FileItem> fileItemList = fileItem.listFileItems();
            if (fileItemList.size() > 0) {
                for (FileItem f : fileItemList) {
                    writeZip(f, parent, zos, zip_level);
                }
            } else {
                try {
                    zos.putNextEntry(new ZipEntry(parent));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (fileItem.isFile()) {
            try {
                InputStream in = fileItem.getInputStream();
                ZipEntry zipentry = new ZipEntry(parent + fileItem.getName());

                if (zip_level == Constants.ZIP_LEVEL_STORED) {
                    zipentry.setMethod(ZipOutputStream.STORED);
                    zipentry.setCompressedSize(fileItem.length());
                    zipentry.setSize(fileItem.length());
                    zipentry.setCrc(FileUtil.getCRC32FromInputStream(fileItem.getInputStream()).getValue());
                }

                zos.putNextEntry(zipentry);
                byte[] buffer = new byte[1024];
                int length;

                    /*Message msg_currentfile = new Message();
                    msg_currentfile.what=BaseActivity.MESSAGE_COPYFILE_CURRENTFILE;
                    String currentPath=file.getAbsolutePath();
                    if(currentPath.length()>90) currentPath="..."+currentPath.substring(currentPath.length()-90,currentPath.length());
                    msg_currentfile.obj=context.getResources().getString(R.string.copytask_zip_current)+currentPath;
                    BaseActivity.sendMessage(msg_currentfile);*/
                final String display_path;
                if (fileItem.isDocumentFile()) {
                    display_path = DocumentFileUtil.getDisplayPathForDataObbDocumentFile(fileItem.getDocumentFile());
                } else {
                    display_path = fileItem.getPath();
                }
                Global.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onExportProgressUpdated(progress, total, display_path);
                        }

                    }
                });

                while ((length = in.read(buffer)) != -1 && !isInterrupted) {
                    zos.write(buffer, 0, length);
                    this.progress += length;
                    this.zipWriteLength_second += length;
                    long endTime = System.currentTimeMillis();
                    if (endTime - this.zipTime > 1000) {
                        this.zipTime = endTime;
                            /*Message msg_speed=new Message();
                            msg_speed.what=BaseActivity.MESSAGE_COPYFILE_REFRESH_SPEED;
                            msg_speed.obj=this.zipWriteLength_second;
                            BaseActivity.sendMessage(msg_speed);*/
                        final long zip_speed = zipWriteLength_second;
                        Global.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) listener.onExportSpeedUpdated(zip_speed);
                            }
                        });
                        this.zipWriteLength_second = 0;
                    }
                    if (this.progress - progress_check_zip > 100 * 1024) {
                        progress_check_zip = this.progress;
                            /*Message msg=new Message();
                            msg.what=Main.MESSAGE_COPYFILE_REFRESH_PROGRESS;
                            msg.obj=new Long[]{this.progress,this.total};
                            BaseActivity.sendMessage(msg);*/
                        Global.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null)
                                    listener.onExportProgressUpdated(progress, total, display_path);
                            }
                        });
                    }

                }
                zos.flush();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public interface ExportProgressListener {
        void onExportAppItemStarted(int order, AppItem item, int total, String write_path);

        void onExportProgressUpdated(long current, long total, String write_path);

        void onExportSpeedUpdated(long speed);

        //void onAppItemFinished(int order,AppItem item,int total);
        void onExportTaskFinished(List<FileItem> write_paths, String error_message);
    }
}
