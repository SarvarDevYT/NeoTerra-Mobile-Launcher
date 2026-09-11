package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

import org.json.JSONObject;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NeoTerraLoginFragment extends Fragment {
    public static final String TAG = "NEOTERRA_LOGIN_FRAGMENT";
    private static final String LOGIN_API = "https://site.neoterra.uz/api/launcher/auth/login";

    private EditText mIdentifierEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private Button mRegisterButton;

    public NeoTerraLoginFragment() {
        super(R.layout.fragment_neoterra_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mIdentifierEditText = view.findViewById(R.id.neoterra_edit_identifier);
        mPasswordEditText = view.findViewById(R.id.neoterra_edit_password);
        mLoginButton = view.findViewById(R.id.neoterra_login_button);
        mRegisterButton = view.findViewById(R.id.neoterra_register_button);

        mRegisterButton.setOnClickListener(v -> Tools.openURL(requireActivity(), "https://site.neoterra.uz"));
        mLoginButton.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String identifier = mIdentifierEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();

        if (identifier.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), R.string.neoterra_login_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        mLoginButton.setEnabled(false);
        mLoginButton.setText("Kutilmoqda...");

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(LOGIN_API);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject req = new JSONObject();
                req.put("identifier", identifier);
                req.put("password", password);

                byte[] out = req.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(out);
                }

                int code = conn.getResponseCode();
                String resp = code >= 200 && code < 300
                        ? Tools.read(conn.getInputStream())
                        : (conn.getErrorStream() != null ? Tools.read(conn.getErrorStream()) : "{}");

                JSONObject resJson = new JSONObject(resp);
                boolean success = resJson.optBoolean("success", false);

                if (success) {
                    JSONObject user = resJson.optJSONObject("user");
                    String nickname = user != null ? user.optString("nickname", "") : "";
                    if (nickname.isEmpty()) {
                        nickname = identifier.contains("@") ? identifier.split("@")[0] : identifier;
                    }

                    // Pre-download skin if user has custom skin URL
                    String skinUrl = user != null ? user.optString("skinUrl", null) : null;
                    if (skinUrl != null && !skinUrl.isEmpty() && !skinUrl.equals("null")) {
                        try {
                            File tempSkin = new File(Tools.DIR_CACHE, nickname + "_rawskin.png");
                            Tools.downloadFile(skinUrl, tempSkin.getAbsolutePath());
                        } catch (Exception e) {
                            Log.w("NeoTerraLogin", "Failed to pre-download skin: " + e.getMessage());
                        }
                    }

                    final String finalNickname = nickname;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            ExtraCore.setValue(ExtraConstants.MOJANG_LOGIN_TODO, new String[]{ finalNickname, "" });
                            Tools.swapFragment(requireActivity(), MainMenuFragment.class, MainMenuFragment.TAG, null);
                        });
                    }
                } else {
                    String msg = resJson.optString("message", getString(R.string.neoterra_login_failed));
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            mLoginButton.setEnabled(true);
                            mLoginButton.setText(R.string.neoterra_login_button);
                            Context ctx = getContext();
                            if (ctx != null) {
                                Tools.dialog(ctx, getString(R.string.global_error), msg);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("NeoTerraLogin", "Login exception: " + e.getMessage(), e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        mLoginButton.setEnabled(true);
                        mLoginButton.setText(R.string.neoterra_login_button);
                        Context ctx = getContext();
                        if (ctx != null) {
                            Tools.dialog(ctx, getString(R.string.global_error), "Ulanishda xatolik: " + e.getLocalizedMessage());
                        }
                    });
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }
}
