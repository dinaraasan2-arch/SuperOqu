package kz.superoqu.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Locale;
import java.util.Set;
import android.speech.tts.Voice;

public class MainActivity extends Activity {
    private WebView view;
    private TextToSpeech tts;
    private boolean ready = false;
    private final Locale kazakh = new Locale("kk", "KZ");

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFF0D1531);
        getWindow().setNavigationBarColor(0xFF0D1531);
        tts = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS && tts != null) {
                int result = tts.setLanguage(kazakh);
                ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
                tts.setPitch(1.03f);
            }
        });
        view = new WebView(this);
        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        view.setWebViewClient(new WebViewClient());
        view.addJavascriptInterface(new SpeechBridge(), "SuperOquAndroid");
        setContentView(view);
        view.loadUrl("file:///android_asset/index.html");
    }

    public class SpeechBridge {
        @JavascriptInterface public boolean hasKazakhVoice() { return ready; }
        @JavascriptInterface public void speak(String text, float speed) {
            if (!ready || tts == null || text == null) return;
            String speech = text.replace("-", "");
            if (speech.length() > 2500) speech = speech.substring(0, 2500);
            final String result = speech;
            final float rate = Math.max(0.5f, Math.min(1.1f, speed));
            runOnUiThread(() -> {
                tts.setSpeechRate(rate);
                tts.speak(result, TextToSpeech.QUEUE_FLUSH, null, "lesson");
            });
        }
        @JavascriptInterface public void stop() {
            runOnUiThread(() -> { if (tts != null) tts.stop(); });
        }
    }

    @Override public void onBackPressed() {
        if (view != null && view.canGoBack()) view.goBack();
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (view != null) view.destroy();
        super.onDestroy();
    }
}
