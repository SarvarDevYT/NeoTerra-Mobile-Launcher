package net.kdt.pojavlaunch.value;


import android.graphics.BitmapFactory;
import android.util.Log;

import net.kdt.pojavlaunch.*;
import net.kdt.pojavlaunch.utils.FileUtils;

import java.io.*;
import com.google.gson.*;
import android.graphics.Bitmap;
import android.util.Base64;

import androidx.annotation.Keep;

import org.apache.commons.io.IOUtils;

@SuppressWarnings("IOStreamConstructor")
@Keep
public class MinecraftAccount {
    public String accessToken = "0"; // access token
    public String clientToken = "0"; // clientID: refresh and invalidate
    public String profileId = "00000000-0000-0000-0000-000000000000"; // profile UUID, for obtaining skin
    public String username = "Steve";
    public String selectedVersion = "1.7.10";
    public boolean isMicrosoft = false;
    public String msaRefreshToken = "0";
    public String xuid;
    public long expiresAt;
    public String skinFaceBase64;
    private Bitmap mFaceCache;
    
    void updateSkinFace(String uuid) {
        File skinFile = getSkinFaceFile(username);
        // 1. Try Ely.by skin system
        try {
            File tempSkin = new File(Tools.DIR_CACHE, username + "_rawskin.png");
            Tools.downloadFile("http://skinsystem.ely.by/skins/" + username + ".png", tempSkin.getAbsolutePath());
            if (tempSkin.exists() && tempSkin.length() > 300) {
                Bitmap fullSkin = BitmapFactory.decodeFile(tempSkin.getAbsolutePath());
                if (fullSkin != null && fullSkin.getWidth() >= 64) {
                    Bitmap head = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                    android.graphics.Canvas canvas = new android.graphics.Canvas(head);
                    android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG);
                    paint.setFilterBitmap(false);

                    // Head base [8, 8, 16, 16]
                    android.graphics.Rect srcHead = new android.graphics.Rect(8, 8, 16, 16);
                    android.graphics.Rect dst = new android.graphics.Rect(0, 0, 100, 100);
                    canvas.drawBitmap(fullSkin, srcHead, dst, paint);

                    // Helm layer [40, 8, 48, 16]
                    android.graphics.Rect srcHelm = new android.graphics.Rect(40, 8, 48, 16);
                    canvas.drawBitmap(fullSkin, srcHelm, dst, paint);

                    java.io.FileOutputStream fos = new java.io.FileOutputStream(skinFile);
                    head.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                    tempSkin.delete();
                    mFaceCache = head;
                    Log.i("SkinLoader", "Ely.by skin head rendered for " + username);
                    return;
                }
            }
        } catch (Exception e) {
            Log.d("SkinLoader", "Ely.by check: " + e.getMessage());
        }

        // 2. Fallback to mc-heads.net
        try {
            Tools.downloadFile("https://mc-heads.net/head/" + (uuid != null && !uuid.startsWith("00000000") ? uuid : username) + "/100", skinFile.getAbsolutePath());
            if(skinFile.exists()) {
                mFaceCache = BitmapFactory.decodeFile(skinFile.getAbsolutePath());
            }
            Log.i("SkinLoader", "Update skin face success");
        } catch (IOException e) {
            Log.w("SkinLoader", "Could not update skin face", e);
        }
    }

    public boolean isLocal(){
        return accessToken.equals("0") && !username.startsWith("Demo.");
    }

    public boolean isDemo(){
        return username.startsWith("Demo.");
    }
    
    public void updateSkinFace() {
        updateSkinFace(profileId);
    }
    
    public String save(String outPath) throws IOException {
        Tools.write(outPath, Tools.GLOBAL_GSON.toJson(this));
        return username;
    }
    
    public String save() throws IOException {
        return save(Tools.DIR_ACCOUNT_NEW + "/" + username + ".json");
    }
    
    public static MinecraftAccount parse(String content) throws JsonSyntaxException {
        return Tools.GLOBAL_GSON.fromJson(content, MinecraftAccount.class);
    }

    public static MinecraftAccount load(String name) {
        if(!accountExists(name)) return null;
        try {
            MinecraftAccount acc = parse(Tools.read(Tools.DIR_ACCOUNT_NEW + "/" + name + ".json"));
            if (acc.accessToken == null) {
                acc.accessToken = "0";
            }
            if (acc.clientToken == null) {
                acc.clientToken = "0";
            }
            if (acc.profileId == null) {
                acc.profileId = "00000000-0000-0000-0000-000000000000";
            }
            if (acc.username == null) {
                acc.username = "0";
            }
            if (acc.selectedVersion == null) {
                acc.selectedVersion = "1.7.10";
            }
            if (acc.msaRefreshToken == null) {
                acc.msaRefreshToken = "0";
            }
            return acc;
        } catch(IOException | JsonSyntaxException e) {
            Log.e(MinecraftAccount.class.getName(), "Caught an exception while loading the profile",e);
            return null;
        }
    }

    public Bitmap getSkinFace(){
        File skinFaceFile = getSkinFaceFile(username);
        if (!skinFaceFile.exists()) {
            if(skinFaceBase64 != null) {
                byte[] faceIconBytes = Base64.decode(skinFaceBase64, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(faceIconBytes, 0, faceIconBytes.length);
            }
            return null;
        } else {
            if(mFaceCache == null) {
                mFaceCache = BitmapFactory.decodeFile(skinFaceFile.getAbsolutePath());
            }
        }

        return mFaceCache;
    }

    public static Bitmap getSkinFace(String username) {
        return BitmapFactory.decodeFile(getSkinFaceFile(username).getAbsolutePath());
    }

    private static File getSkinFaceFile(String username) {
        return new File(Tools.DIR_CACHE, username + ".png");
    }

    private static boolean accountExists(String username){
        return new File(Tools.DIR_ACCOUNT_NEW + "/" + username + ".json").exists();
    }
}
