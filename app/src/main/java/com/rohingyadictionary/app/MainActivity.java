package com.rohingyadictionary.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends Activity {

    private EditText searchInput;
    private LinearLayout resultsLayout;
    private SharedPreferences preferences;
    private TextToSpeech textToSpeech;

    private JSONArray dictionary = new JSONArray();

    private final int GREEN = Color.rgb(7, 94, 84);
    private final int WHITE = Color.WHITE;
    private final int BLACK = Color.BLACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(
                "RohingyaDictionaryData",
                MODE_PRIVATE
        );

        loadDictionary();

        setContentView(R.layout.activity_main);

        searchInput = findViewById(R.id.searchInput);
        resultsLayout = findViewById(R.id.resultsLayout);

        Button addButton = findViewById(R.id.addButton);
        TextView homeButton = findViewById(R.id.homeButton);
        TextView favoritesButton = findViewById(R.id.favoritesButton);
        TextView historyButton = findViewById(R.id.historyButton);

        searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                searchWords(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchInput.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        hideKeyboard();
                        return true;
                    }
                    return false;
                }
        );

        addButton.setOnClickListener(
                v -> showAddWordDialog()
        );

        homeButton.setOnClickListener(v -> {
            searchInput.setText("");
            hideKeyboard();
        });

        favoritesButton.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Favorites feature will be added next.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        historyButton.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Search history feature will be added next.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        textToSpeech = new TextToSpeech(
                this,
                status -> {
                    if (status == TextToSpeech.SUCCESS
                            && textToSpeech != null) {
                        textToSpeech.setLanguage(Locale.ENGLISH);
                    }
                }
        );

        searchWords("");
    }

    private void searchWords(String query) {

        resultsLayout.removeAllViews();

        String search = query.trim().toLowerCase(Locale.ROOT);
        int found = 0;

        for (int i = 0; i < dictionary.length(); i++) {

            JSONObject word = dictionary.optJSONObject(i);

            if (word == null) {
                continue;
            }

            String english = word.optString("english", "");
            String meaning = word.optString("meaning", "");

            if (search.isEmpty()
                    || english.toLowerCase(Locale.ROOT).contains(search)
                    || meaning.toLowerCase(Locale.ROOT).contains(search)) {

                addWordCard(english, meaning);
                found++;
            }
        }

        if (found == 0) {

            TextView empty = new TextView(this);

            empty.setText(
                    search.isEmpty()
                    ? "Your dictionary is ready.\nTap + Add New Word to begin."
                    : "No matching word found.\nTry another word or add it to your dictionary."
            );

            empty.setTextSize(16);
            empty.setTextColor(Color.DKGRAY);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(
                    dp(12), dp(35), dp(12), dp(35)
            );

            resultsLayout.addView(empty);
        }
    }

    private void addWordCard(String english, String meaning) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackgroundColor(Color.rgb(245, 248, 247));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, dp(12));

        TextView englishText = new TextView(this);
        englishText.setText(english);
        englishText.setTextSize(21);
        englishText.setTypeface(null, Typeface.BOLD);
        englishText.setTextColor(GREEN);

        card.addView(englishText);

        TextView meaningText = new TextView(this);
        meaningText.setText(meaning);
        meaningText.setTextSize(22);
        meaningText.setTextColor(BLACK);
        meaningText.setGravity(Gravity.END);
        meaningText.setTextDirection(View.TEXT_DIRECTION_RTL);
        meaningText.setPadding(0, dp(8), 0, dp(8));

        card.addView(meaningText);

        Button speakButton = new Button(this);
        speakButton.setText("Pronounce English");

        speakButton.setOnClickListener(v -> {
            if (textToSpeech != null) {
                textToSpeech.speak(
                        english,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "word_pronunciation"
                );
            }
        });

        card.addView(speakButton);

        resultsLayout.addView(card, cardParams);
    }

    private void showAddWordDialog() {

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), dp(8));

        EditText englishInput = new EditText(this);
        englishInput.setHint("English word");
        englishInput.setSingleLine(true);

        EditText meaningInput = new EditText(this);
        meaningInput.setHint("Rohingya meaning in Rohingya Script");
        meaningInput.setMinLines(2);
        meaningInput.setGravity(Gravity.TOP);
        meaningInput.setTextDirection(View.TEXT_DIRECTION_RTL);

        form.addView(englishInput);
        form.addView(meaningInput);

        new AlertDialog.Builder(this)
                .setTitle("Add New Word")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {

                    String english = englishInput.getText()
                            .toString().trim();

                    String meaning = meaningInput.getText()
                            .toString().trim();

                    if (english.isEmpty() || meaning.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Enter both the English word and its meaning.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    JSONObject entry = new JSONObject();

                    try {
                        entry.put("english", english);
                        entry.put("meaning", meaning);

                        dictionary.put(entry);
                        saveDictionary();

                        searchInput.setText(english);

                        Toast.makeText(
                                this,
                                "Word saved successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                    } catch (Exception e) {
                        Toast.makeText(
                                this,
                                "Could not save the word.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }

    private void loadDictionary() {

        String saved = preferences.getString(
                "dictionary",
                "[]"
        );

        try {
            dictionary = new JSONArray(saved);
        } catch (Exception e) {
            dictionary = new JSONArray();
        }
    }

    private void saveDictionary() {

        preferences.edit()
                .putString("dictionary", dictionary.toString())
                .apply();
    }

    private void hideKeyboard() {

        try {
            InputMethodManager manager =
                    (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE
                    );

            if (manager != null && searchInput != null) {
                manager.hideSoftInputFromWindow(
                        searchInput.getWindowToken(),
                        0
                );
            }
        } catch (Exception ignored) {
        }
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density
        );
    }

    @Override
    protected void onDestroy() {

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        super.onDestroy();
    }
}
