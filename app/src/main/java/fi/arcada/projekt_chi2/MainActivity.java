package fi.arcada.projekt_chi2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Deklarera 4 Button-objekt
    Button btn1, btn2, btn3, btn4;
    // Deklarera 4 heltalsvariabler för knapparnas värden
    int val1, val2, val3, val4;

    TextView textOut, textWelcome, textResults, textProcent1, textProcent2, testGrp1, testGrp2, test1, test2, statement;

    int launchCount= 0;
    SharedPreferences sharedPref;
    SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textOut = findViewById(R.id.counter);
        textWelcome = findViewById(R.id.welcome);
        textResults = findViewById(R.id.results);
        textProcent1 = findViewById(R.id.percent1);
        textProcent2 = findViewById(R.id.percent2);
        testGrp1 = findViewById(R.id.testGrp1);
        testGrp2 = findViewById(R.id.testGrp2);
        test1 = findViewById(R.id.test1);
        test2 = findViewById(R.id.test2);
        statement = findViewById(R.id.statement);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Koppla samman Button-objekten med knapparna i layouten
        btn1 = findViewById(R.id.button1);
        btn2 = findViewById(R.id.button2);
        btn3 = findViewById(R.id.button3);
        btn4 = findViewById(R.id.button4);

        prefEditor = sharedPref.edit();
        prefEditor.putInt("launchCount", sharedPref.getInt("launchCount", 0)+1);
        prefEditor.apply();
        launchCount = sharedPref.getInt("launchCount", 0);


        textOut.setText(String.format("Appen startad %d gånger", launchCount));
        textWelcome.setText(String.format("Välkommen tillbaka %s", sharedPref.getString("userName", null)));

        testGrp1.setText(String.format(sharedPref.getString("testGrp1","Anställd")));
        testGrp2.setText(String.format(sharedPref.getString("testGrp2","Arbetslös")));
        test1.setText(String.format(sharedPref.getString("test1","Motionerar regelbundet")));
        test2.setText(String.format(sharedPref.getString("test2","Motionerar inte")));
        statement.setText(String.format(sharedPref.getString("test1","Motionerar regelbundet")));

        // Visa färdigt sparade värden och räkningarna när man öppnar appen
        calculate();
    }

    /**
      Klickhanterare för knapparna
     */
    public void buttonClick(View view) {

        // Skapa ett Button-objekt genom att type-casta (byta datatyp)
        // på det View-objekt som kommer med knapptrycket
        Button btn = (Button) view;

        // Kontrollera vilken knapp som klickats, öka värde på rätt vaiabel
        if (view.getId() == R.id.button1) pref("val1");
        if (view.getId() == R.id.button2) pref("val2");
        if (view.getId() == R.id.button3) pref("val3");
        if (view.getId() == R.id.button4) pref("val4");

        // Börja om knappen
        if (view.getId() == R.id.buttonReset) {
            pref("reset");
        }

        // Slutligen, kör metoden som ska räkna ut allt!
        calculate();
    }

    // Sparar värdena
    public void pref(String val) {
        prefEditor = sharedPref.edit();
        if (val == "reset") {
            prefEditor.putInt("val1",0);
            prefEditor.putInt("val2",0);
            prefEditor.putInt("val3",0);
            prefEditor.putInt("val4",0);
        }
        else {
            prefEditor.putInt(val, sharedPref.getInt(val, 0) + 1);
        }
        prefEditor.apply();
    }


    /**
     * Metod som uppdaterar layouten och räknar ut själva analysen.
     */
    public void calculate() {

        // Hämtar sparade värden
        val1 = sharedPref.getInt("val1", 0);
        val2 = sharedPref.getInt("val2", 0);
        val3 = sharedPref.getInt("val3", 0);
        val4 = sharedPref.getInt("val4", 0);

        // Uppdatera knapparna med de nuvarande värdena
        btn1.setText(String.valueOf(val1));
        btn2.setText(String.valueOf(val2));
        btn3.setText(String.valueOf(val3));
        btn4.setText(String.valueOf(val4));

        // Om alla värden = 0, return, annars crashar appen
        if (val1+val2+val3+val4 == 0) return;

        // Mata in värdena i Chi-2-uträkningen och ta emot resultatet
        // i en Double-variabel
        double chi2 = Significance.chiSquared(val1, val2, val3, val4);

        // Mata in chi2-resultatet i getP() och ta emot p-värdet
        double pValue = Significance.getP(chi2);

        double sign = Double.parseDouble(sharedPref.getString("Signifikansnivå","0.0"));

        double prop = 100 - pValue;

        double procent1 = (val1 / (val1+val3)) * 100;
        double procent2 = (val2 / (val2+val4)) * 100;

        String dep;
        /**
         *  - Visa chi2 och pValue åt användaren på ett bra och tydligt sätt!
         *
         *  - Visa procentuella andelen jakande svar inom de olika grupperna.
         *    T.ex. (val1 / (val1+val3) * 100) och (val2 / (val2+val4) * 100
         *
         *  - Analysera signifikansen genom att jämföra p-värdet
         *    med signifikansnivån, visa reultatet åt användaren
         *
         */

        if (pValue < 0.05)
        {
            dep = "signifikant";
        }
        else
        {
            dep = "insignifikant";
        }

        textProcent1.setText((String.format("%s: %.2f",
                (sharedPref.getString("testGrp1","0")),
                procent1
        )));
        textProcent2.setText((String.format("%s: %.2f",
                (sharedPref.getString("testGrp2","0")),
                procent2
        )));

        textResults.setText(String.format(" Chi-2 resultat: %.2f\n P-värde: %.2f \n Signifikansnivå: %.2f \n Resultatet är med %.2f% sannolikhet inte oberoende och kan betraktas som %s",
                chi2,
                pValue,
                sign,
                prop,
                dep
        ));
    }
    public void openSettings(View view) {

        Intent intent = new Intent (this, SettingsActivity.class);
        startActivity(intent);
    }

}