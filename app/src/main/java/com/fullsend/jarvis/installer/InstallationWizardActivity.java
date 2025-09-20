package com.fullsend.jarvis.installer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fullsend.jarvis.R;

public class InstallationWizardActivity extends AppCompatActivity {

    private TextView statusText;
    private Button installButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installation_wizard);

        statusText = findViewById(R.id.statusText);
        installButton = findViewById(R.id.installButton);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setMax(100);
        progressBar.setProgress(0);

        installButton.setOnClickListener(v -> startInstallation());
    }

    private void startInstallation() {
        installButton.setEnabled(false);
        statusText.setText("Starting installation...");
        new InstallationTask().execute();
    }

    private class InstallationTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setProgress(0);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Simulated steps with delays
                String[] steps = {
                        "Checking system requirements...",
                        "Copying files...",
                        "Applying settings...",
                        "Finalizing installation..."
                };

                for (int i = 0; i < steps.length; i++) {
                    Thread.sleep(1000); // Simulate work
                    publishProgress((i + 1) * 25); // Update progress
                }
            } catch (InterruptedException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = values[0];
            progressBar.setProgress(progress);
            statusText.setText("Installation " + progress + "% complete...");
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                statusText.setText("Installation Complete!");
                Toast.makeText(InstallationWizardActivity.this, "Full Send Installed!", Toast.LENGTH_LONG).show();
            } else {
                statusText.setText("Installation Failed.");
                Toast.makeText(InstallationWizardActivity.this, "Installation failed. Try again.", Toast.LENGTH_LONG).show();
            }
            installButton.setEnabled(true);
            installButton.setText("Finish");
            installButton.setOnClickListener(v -> finish());
        }
    }
}