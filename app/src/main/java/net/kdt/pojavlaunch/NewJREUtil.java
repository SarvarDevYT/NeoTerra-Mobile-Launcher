package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.Architecture.archAsString;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.Runtime;
import net.kdt.pojavlaunch.utils.MathUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class NewJREUtil {
    private static final String TAG = "NewJREUtil";

    private static boolean checkInternalRuntime(Activity activity, InternalRuntime internalRuntime) {
        // 1. Check if the runtime is already installed and functional
        Runtime installedRuntime = MultiRTUtils.read(internalRuntime.name);
        if (installedRuntime != null && installedRuntime.javaVersion >= internalRuntime.majorVersion) {
            File javaBin = new File(Tools.MULTIRT_HOME, internalRuntime.name + "/bin/java");
            if (javaBin.exists()) {
                Log.i(TAG, "Runtime " + internalRuntime.name + " is already installed and functional (Java " + installedRuntime.javaVersion + ")");
                return true;
            }
        }

        AssetManager assetManager = activity.getAssets();
        String nativeLibDir = activity.getApplicationInfo().nativeLibraryDir;
        if (nativeLibDir == null) {
            nativeLibDir = Tools.NATIVE_LIB_DIR;
        }

        // 2. Check if runtime archive is bundled in APK assets
        if (tryInstallFromAssets(assetManager, internalRuntime, nativeLibDir)) {
            MultiRTUtils.forceReread(internalRuntime.name);
            writeVersionFile(internalRuntime.name, String.valueOf(internalRuntime.majorVersion));
            return true;
        }

        // 3. Not found in assets -> auto-download from official repository
        Log.i(TAG, "Runtime not found locally or in assets. Auto-downloading " + internalRuntime.name);
        if (downloadAndInstallRuntime(internalRuntime, nativeLibDir)) {
            MultiRTUtils.forceReread(internalRuntime.name);
            writeVersionFile(internalRuntime.name, String.valueOf(internalRuntime.majorVersion));
            return true;
        }

        return false;
    }

    private static boolean tryInstallFromAssets(AssetManager assetManager, InternalRuntime internalRuntime, String nativeLibDir) {
        String arch = archAsString(Tools.DEVICE_ARCHITECTURE);

        // A. Check single complete archive: e.g. components/jre-new/jre-arm64.tar.xz or components/jre-new/jre.tar.xz
        String[] possibleArchives = {
                internalRuntime.path + "/jre-" + arch + ".tar.xz",
                internalRuntime.path + "/jre.tar.xz"
        };

        for (String path : possibleArchives) {
            try (InputStream is = assetManager.open(path)) {
                Log.i(TAG, "Unpacking bundled runtime from asset: " + path);
                ProgressLayout.setProgress(ProgressLayout.DOWNLOAD_MINECRAFT, 0, "Java 17 o'rnatilmoqda...");
                MultiRTUtils.installRuntimeNamed(nativeLibDir, is, internalRuntime.name);
                MultiRTUtils.postPrepare(internalRuntime.name);
                return true;
            } catch (IOException ignored) {
                // Not found under this asset path
            }
        }

        // B. Check split binpack archive: universal.tar.xz + bin-<arch>.tar.xz
        try {
            InputStream universalIs = assetManager.open(internalRuntime.path + "/universal.tar.xz");
            InputStream binIs = assetManager.open(internalRuntime.path + "/bin-" + arch + ".tar.xz");
            String version = String.valueOf(internalRuntime.majorVersion);
            try {
                version = Tools.read(assetManager.open(internalRuntime.path + "/version"));
            } catch (Exception ignored) {}

            ProgressLayout.setProgress(ProgressLayout.DOWNLOAD_MINECRAFT, 0, "Java 17 o'rnatilmoqda...");
            MultiRTUtils.installRuntimeNamedBinpack(universalIs, binIs, internalRuntime.name, version);
            MultiRTUtils.postPrepare(internalRuntime.name);
            return true;
        } catch (IOException ignored) {
            // Binpack not found
        }

        return false;
    }

    private static String getDownloadUrl(InternalRuntime internalRuntime, int arch) {
        if (internalRuntime == InternalRuntime.JRE_17) {
            switch (arch) {
                case Architecture.ARCH_ARM64:
                    return "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-ec28559/jre17-arm64-20210825-release.tar.xz";
                case Architecture.ARCH_ARM:
                    return "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-ec28559/jre17-arm-20210914-release.tar.xz";
                case Architecture.ARCH_X86_64:
                    return "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-ec28559/jre17-x86_64-20210825-release.tar.xz";
                case Architecture.ARCH_X86:
                    return "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-ec28559/jre17-x86-20220225-release.tar.xz";
                default:
                    return null;
            }
        }
        return null;
    }

    private static boolean downloadAndInstallRuntime(InternalRuntime internalRuntime, String nativeLibDir) {
        String urlString = getDownloadUrl(internalRuntime, Tools.DEVICE_ARCHITECTURE);
        if (urlString == null) {
            Log.e(TAG, "No download URL available for arch " + Tools.DEVICE_ARCHITECTURE);
            return false;
        }

        File cacheDir = Tools.DIR_CACHE;
        if (cacheDir != null && !cacheDir.exists()) cacheDir.mkdirs();
        File tempFile = new File(cacheDir, "jre_" + internalRuntime.name + "_" + archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz");
        File partFile = new File(tempFile.getAbsolutePath() + ".part");

        try {
            downloadFileWithProgress(urlString, partFile);
            if (!partFile.exists() || partFile.length() < 1000000) {
                Log.e(TAG, "Downloaded file is invalid or too small");
                partFile.delete();
                return false;
            }

            if (tempFile.exists()) tempFile.delete();
            if (!partFile.renameTo(tempFile)) {
                FileUtils.copyFile(partFile, tempFile);
                partFile.delete();
            }

            ProgressLayout.setProgress(ProgressLayout.DOWNLOAD_MINECRAFT, 100, "Java 17 o'rnatilmoqda...");
            try (FileInputStream fis = new FileInputStream(tempFile)) {
                MultiRTUtils.installRuntimeNamed(nativeLibDir, fis, internalRuntime.name);
                MultiRTUtils.postPrepare(internalRuntime.name);
            }
            tempFile.delete();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to download and install runtime", e);
            if (partFile.exists()) partFile.delete();
            if (tempFile.exists()) tempFile.delete();
            return false;
        }
    }

    private static void downloadFileWithProgress(String initialUrl, File destination) throws IOException {
        String currentUrl = initialUrl;
        HttpURLConnection conn = null;
        int redirectCount = 0;

        while (redirectCount < 8) {
            URL url = new URL(currentUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent", "NeoTerra-Launcher");
            conn.setInstanceFollowRedirects(true);
            int code = conn.getResponseCode();

            if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP
                    || code == HttpURLConnection.HTTP_SEE_OTHER || code == 307 || code == 308) {
                String location = conn.getHeaderField("Location");
                conn.disconnect();
                if (location == null) throw new IOException("Redirect response without Location header");
                currentUrl = location;
                redirectCount++;
            } else if (code == HttpURLConnection.HTTP_OK) {
                break;
            } else {
                conn.disconnect();
                throw new IOException("HTTP error response: " + code);
            }
        }

        if (conn == null) throw new IOException("Connection failed");

        long totalBytes = conn.getContentLengthLong();
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[16384];
            long downloadedBytes = 0;
            int read;
            long lastProgressUpdate = 0;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                downloadedBytes += read;

                long now = System.currentTimeMillis();
                if (now - lastProgressUpdate > 150) {
                    lastProgressUpdate = now;
                    int percent = totalBytes > 0 ? (int) ((downloadedBytes * 100) / totalBytes) : -1;
                    String msg = percent >= 0
                            ? "Java 17 yuklab olinmoqda... (" + percent + "%)"
                            : "Java 17 yuklab olinmoqda...";
                    ProgressLayout.setProgress(ProgressLayout.DOWNLOAD_MINECRAFT, percent >= 0 ? percent : 50, msg);
                }
            }
            out.flush();
        } finally {
            conn.disconnect();
        }
    }

    private static void writeVersionFile(String runtimeName, String version) {
        try {
            File verFile = new File(Tools.MULTIRT_HOME, runtimeName + "/pojav_version");
            try (FileOutputStream fos = new FileOutputStream(verFile)) {
                fos.write(version.getBytes());
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not write pojav_version file", e);
        }
    }

    private static InternalRuntime getInternalRuntime(Runtime runtime) {
        for(InternalRuntime internalRuntime : InternalRuntime.values()) {
            if(internalRuntime.name.equals(runtime.name)) return internalRuntime;
        }
        return null;
    }

    private static MathUtils.RankedValue<Runtime> getNearestInstalledRuntime(int targetVersion) {
        List<Runtime> runtimes = MultiRTUtils.getRuntimes();
        return MathUtils.findNearestPositive(targetVersion, runtimes, (runtime)->runtime.javaVersion);
    }

    private static MathUtils.RankedValue<InternalRuntime> getNearestInternalRuntime(int targetVersion) {
        List<InternalRuntime> runtimeList = Arrays.asList(InternalRuntime.values());
        return MathUtils.findNearestPositive(targetVersion, runtimeList, (runtime)->runtime.majorVersion);
    }

    /** @return true if everything is good, false otherwise.  */
    public static boolean installNewJreIfNeeded(Activity activity, JMinecraftVersionList.Version versionInfo) {
        if (versionInfo.javaVersion == null || versionInfo.javaVersion.component.equalsIgnoreCase("jre-legacy"))
            return true;

        int gameRequiredVersion = versionInfo.javaVersion.majorVersion;

        LauncherProfiles.load();
        MinecraftProfile minecraftProfile = LauncherProfiles.getCurrentProfile();
        String profileRuntime = Tools.getSelectedRuntime(minecraftProfile);
        Runtime runtime = MultiRTUtils.read(profileRuntime);

        // Check if selected runtime satisfies requirement
        if (runtime.javaVersion >= gameRequiredVersion) {
            InternalRuntime internalRuntime = getInternalRuntime(runtime);
            if(internalRuntime != null) {
                return checkInternalRuntime(activity, internalRuntime);
            }
            return true;
        }

        // Automatically pick from installed or internal runtimes
        MathUtils.RankedValue<?> nearestInstalledRuntime = getNearestInstalledRuntime(gameRequiredVersion);
        MathUtils.RankedValue<?> nearestInternalRuntime = getNearestInternalRuntime(gameRequiredVersion);

        MathUtils.RankedValue<?> selectedRankedRuntime = MathUtils.objectMin(
                nearestInternalRuntime, nearestInstalledRuntime, (value)->value.rank
        );

        if(selectedRankedRuntime == null) {
            showRuntimeFail(activity, versionInfo);
            return false;
        }

        Object selected = selectedRankedRuntime.value;
        String appropriateRuntime;
        InternalRuntime internalRuntime;

        if(selected instanceof Runtime) {
            Runtime selectedRuntime = (Runtime) selected;
            appropriateRuntime = selectedRuntime.name;
            internalRuntime = getInternalRuntime(selectedRuntime);
        } else if (selected instanceof InternalRuntime) {
            internalRuntime = (InternalRuntime) selected;
            appropriateRuntime = internalRuntime.name;
        } else {
            throw new RuntimeException("Unexpected type of selected: " + selected.getClass().getName());
        }

        if(internalRuntime != null && !checkInternalRuntime(activity, internalRuntime)) {
            return false;
        }

        minecraftProfile.javaDir = Tools.LAUNCHERPROFILES_RTPREFIX + appropriateRuntime;
        LauncherProfiles.write();
        return true;
    }

    private static void showRuntimeFail(Activity activity, JMinecraftVersionList.Version verInfo) {
        Tools.dialogOnUiThread(activity, activity.getString(R.string.global_error),
                activity.getString(R.string.multirt_nocompatiblert, verInfo.javaVersion.majorVersion));
    }

    private enum InternalRuntime {
        JRE_17(17, "Internal-17", "components/jre-new"),
        JRE_21(21, "Internal-21", "components/jre-21");
        public final int majorVersion;
        public final String name;
        public final String path;
        InternalRuntime(int majorVersion, String name, String path) {
            this.majorVersion = majorVersion;
            this.name = name;
            this.path = path;
        }
    }
}