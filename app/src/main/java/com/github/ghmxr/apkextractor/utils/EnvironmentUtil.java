package com.github.ghmxr.apkextractor.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.ghmxr.apkextractor.Constants;
import com.github.ghmxr.apkextractor.MyApplication;
import com.github.ghmxr.apkextractor.R;
import com.github.ghmxr.apkextractor.tasks.GetApkLibraryTask;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class EnvironmentUtil {

    public static void showInputMethod(@NonNull View view) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            view.requestFocus();
            inputMethodManager.showSoftInput(view, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideInputMethod(@NonNull final Activity activity) {
        try {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static @NonNull
    String getAppNameByPackageName(@NonNull Context context, @NonNull String package_name) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            return String.valueOf(packageManager.getApplicationLabel(packageManager.getApplicationInfo(package_name, 0)));
        } catch (PackageManager.NameNotFoundException ne) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 获取apk包签名基本信息
     *
     * @return string[0]证书发行者, string[1]证书所有者, string[2]序列号
     * string[3]证书起始时间 string[4]证书结束时间
     */
    public static String[] getAPKSignInfo(String filePath) {
        String subjectDN = "";
        String issuerDN = "";
        String serial = "";
        String notBefore = "";
        String notAfter = "";
        try {
            JarFile JarFile = new JarFile(filePath);
            JarEntry JarEntry = JarFile.getJarEntry("AndroidManifest.xml");
            if (JarEntry != null) {
                byte[] readBuffer = new byte[8192];
                InputStream is = new BufferedInputStream(JarFile.getInputStream(JarEntry));
                while (is.read(readBuffer, 0, readBuffer.length) != -1) {

                }
                Certificate[] certs = JarEntry.getCertificates();
                if (certs != null && certs.length > 0) {
                    //获取证书
                    X509Certificate x509cert = (X509Certificate) certs[0];
                    //获取证书发行者
                    issuerDN = x509cert.getIssuerDN().toString();
                    //获取证书所有者
                    subjectDN = x509cert.getSubjectDN().toString();
                    //证书序列号
                    serial = x509cert.getSerialNumber().toString();
                    //证书起始有效期
                    notBefore = x509cert.getNotBefore().toString();
                    //证书结束有效期
                    notAfter = x509cert.getNotAfter().toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[]{subjectDN, issuerDN, serial, notBefore, notAfter};
    }

    public static String hashMD5Value(@NonNull InputStream inputStream) {
        return getHashValue(inputStream, "MD5");
    }

    public static String hashSHA256Value(@NonNull InputStream inputStream) {
        return getHashValue(inputStream, "SHA256");
    }

    public static String hashSHA1Value(@NonNull InputStream inputStream) {
        return getHashValue(inputStream, "SHA1");
    }

    public static String getSignatureMD5StringOfPackageInfo(@NonNull PackageInfo info) {
        return getSignatureStringOfPackageInfo(info, "MD5");
    }

    public static String getSignatureSHA1OfPackageInfo(@NonNull PackageInfo info) {
        return getSignatureStringOfPackageInfo(info, "SHA1");
    }

    public static String getSignatureSHA256OfPackageInfo(@NonNull PackageInfo info) {
        return getSignatureStringOfPackageInfo(info, "SHA256");
    }

    private static String getSignatureStringOfPackageInfo(@NonNull PackageInfo packageInfo, @NonNull String type) {
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance(type);
            localMessageDigest.update(packageInfo.signatures[0].toByteArray());
            return getHexString(localMessageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getHashValue(@NonNull InputStream inputStream, @NonNull String type) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(type);
            int length;
            byte[] buffer = new byte[1024];
            while ((length = inputStream.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, length);
            }
            inputStream.close();
            return getHexString(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getHexString(byte[] paramArrayOfByte) {
        if (paramArrayOfByte == null) {
            return "";
        }
        StringBuilder localStringBuilder = new StringBuilder(2 * paramArrayOfByte.length);
        for (int i = 0; ; i++) {
            if (i >= paramArrayOfByte.length) {
                return localStringBuilder.toString();
            }
            String str = Integer.toString(0xFF & paramArrayOfByte[i], 16);
            if (str.length() == 1) {
                str = "0" + str;
            }
            localStringBuilder.append(str);
        }
    }

    public static HashMap<String, List<String>> getStaticRegisteredReceiversForPackageName(@NonNull Context context, @NonNull String package_name) {
        HashMap<String, List<String>> map = new HashMap<>();
        PackageManager packageManager = context.getPackageManager();
        String[] static_filters = context.getResources().getStringArray(R.array.static_receiver_filters);
        for (String s : static_filters) {
            List<ResolveInfo> list = packageManager.queryBroadcastReceivers(new Intent(s), 0);
            if (list == null) continue;
            for (ResolveInfo info : list) {
                String pn = info.activityInfo.packageName;
                if (pn == null) continue;
                List<String> filters_class = map.get(info.activityInfo.name);
                if (filters_class == null) {
                    filters_class = new ArrayList<>();
                    filters_class.add(s);
                    if (pn.equals(package_name)) map.put(info.activityInfo.name, filters_class);
                } else {
                    if (!filters_class.contains(s)) filters_class.add(s);
                }

            }
        }
        return map;

    }

    public static Bundle getStaticRegisteredReceiversOfBundleTypeForPackageName(@NonNull Context context, @NonNull String package_name) {
        Bundle bundle = new Bundle();
        if (!SPUtil.getGlobalSharedPreferences(context)
                .getBoolean(Constants.PREFERENCE_LOAD_STATIC_LOADERS, Constants.PREFERENCE_LOAD_STATIC_LOADERS_DEFAULT)) {
            return bundle;
        }
        PackageManager packageManager = context.getPackageManager();
        String[] static_filters = context.getResources().getStringArray(R.array.static_receiver_filters);

        for (String s : static_filters) {
            List<ResolveInfo> list = packageManager.queryBroadcastReceivers(new Intent(s), 0);
            if (list == null) continue;
            for (ResolveInfo info : list) {
                String pn = info.activityInfo.packageName;
                if (pn == null) continue;
                ArrayList<String> filters_class = bundle.getStringArrayList(info.activityInfo.name);
                if (filters_class == null) {
                    filters_class = new ArrayList<>();
                    filters_class.add(s);
                    if (pn.equals(package_name))
                        bundle.putStringArrayList(info.activityInfo.name, filters_class);
                } else {
                    if (!filters_class.contains(s)) filters_class.add(s);
                }

            }
        }
        return bundle;
    }


    /**
     * 通过keyword高亮content中的指定内容，支持汉字首字母、全拼匹配
     *
     * @param content 要匹配的内容
     * @param keyword 匹配字符
     * @param color   高亮颜色
     * @return 生成的Spannable
     */
    public static SpannableStringBuilder getSpannableString(@NonNull String content, @Nullable String keyword, @ColorInt int color) {
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        if (keyword == null || "".equals(keyword)) return builder;

        int index = content.toLowerCase().indexOf(keyword.toLowerCase());
        if (index >= 0) {
            builder.setSpan(new ForegroundColorSpan(color), index, index + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return builder;
        }
        keyword = keyword.toLowerCase();
        final ArrayList<String> singleCharFullSpell = new ArrayList<>();
        final StringBuilder fullSpell = new StringBuilder();
        final StringBuilder singleSpell = new StringBuilder();
        final char[] chars_content = content.toCharArray();
        for (int i = 0; i < chars_content.length; i++) {
            if (PinyinUtil.isChineseChar(chars_content[i])) {
                fullSpell.append(PinyinUtil.getFullSpell(String.valueOf(chars_content[i])).toLowerCase());
                singleSpell.append(PinyinUtil.getFirstSpell(String.valueOf(chars_content[i])).toLowerCase());
                singleCharFullSpell.add(PinyinUtil.getFullSpell(String.valueOf(chars_content[i])).toLowerCase());
            } else {
                fullSpell.append(String.valueOf(chars_content[i]).toLowerCase());
                singleSpell.append(String.valueOf(chars_content[i]).toLowerCase());
                singleCharFullSpell.add(String.valueOf(chars_content[i]).toLowerCase());
            }
        }

        int span_index_begin = -1, span_index_end = -1;
        final int index_first_spell = singleSpell.indexOf(keyword);
        if (index_first_spell >= 0) {
            span_index_begin = index_first_spell;
            span_index_end = index_first_spell + keyword.length();
        } else {
            int fullSpellCheck = 0;
            String keywordFullSpellCheck = keyword;
            boolean flag_matched = false;
            boolean flag_matched_end = true;
            for (int i = 0; i < singleCharFullSpell.size(); i++) {
                if (keywordFullSpellCheck.trim().length() == 0) break;
                final String sp = singleCharFullSpell.get(i);
                if (sp.contains(keyword) && !flag_matched) {
                    span_index_begin = i;
                    span_index_end = span_index_begin + 1;
                    break;
                }

                final int index_2 = keywordFullSpellCheck.indexOf(sp);
                if (index_2 >= 0 && PinyinUtil.isChineseChar(chars_content[i])) {
                    flag_matched = true;
                    if (span_index_begin == -1) span_index_begin = i;
                    keywordFullSpellCheck = keywordFullSpellCheck.substring(index_2 + sp.length());
                    fullSpellCheck++;
                    continue;
                }

                final int index_1 = sp.indexOf(keywordFullSpellCheck);
                if (flag_matched) {
                    if (index_1 >= 0) {
                        fullSpellCheck++;
                    } else {
                        flag_matched_end = false;
                    }
                    break;
                }
            }
            if (fullSpellCheck > 0) span_index_end = span_index_begin + fullSpellCheck;
            if (!flag_matched_end) {
                span_index_begin = span_index_end = -1;
            }
        }

        if (span_index_begin >= 0 && span_index_end >= 0) {
            builder.setSpan(new ForegroundColorSpan(color), span_index_begin, span_index_end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }


    @Nullable
    public static GetApkLibraryTask.LibraryType getShowingLibraryType(@NonNull GetApkLibraryTask.LibraryInfo libraryInfo) {
        if (Build.VERSION.SDK_INT >= 21) {
            final String[] supported64BitAbis = Build.SUPPORTED_64_BIT_ABIS;
            if (supported64BitAbis != null) {
                for (String s : supported64BitAbis) {
                    for (GetApkLibraryTask.LibraryType type : libraryInfo.libraries.keySet()) {
                        try {
                            if (type.getName().equalsIgnoreCase(s)) return type;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
            final String[] supportedAbis = Build.SUPPORTED_ABIS;
            if (supportedAbis != null) {
                for (String s : supportedAbis) {
                    for (GetApkLibraryTask.LibraryType type : libraryInfo.libraries.keySet()) {
                        try {
                            if (type.getName().equalsIgnoreCase(s)) return type;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            final String[] supportedAbis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
            for (String s : supportedAbis) {
                for (GetApkLibraryTask.LibraryType type : libraryInfo.libraries.keySet()) {
                    try {
                        if (type.getName().equalsIgnoreCase(s)) return type;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }

    public static int getTargetSdkVersion() {
        final Context context = MyApplication.getApplication();
        final ApplicationInfo applicationInfo = context.getApplicationInfo();
        return applicationInfo != null ? applicationInfo.targetSdkVersion : 23;//此项目发布时targetSdkVersion是23
    }
}
