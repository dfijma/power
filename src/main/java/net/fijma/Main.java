package net.fijma;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    record HomeWizardP1(double totalPowerImportKwh, double totalPowerExportKwh, double activePowerW) {}
    record Omnik(int current, double today, double total) {}

    static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public static void main(String[] args) {

        try {
            final var homewizardUri = new URI("http://10.1.0.153/api/v1/data");
            final var omnikUri = new URI("http://10.1.0.104/js/status.js");

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    final var p1HomeWizard = readP1HomeWizard(homewizardUri);
                    final var omnik = readOmnik(omnikUri);

                    final var activeImport = (int) p1HomeWizard.activePowerW;
                    final var activeProduce = omnik.current;
                    final var totalUsage = activeImport + activeProduce;

                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

                    if (activeImport > 0) {
                        System.out.println("%s: using %d (import %d, generated %d)".formatted(formatter.format(LocalDateTime.now()), totalUsage, activeImport, activeProduce));
                    } else {
                        System.out.println("%s: using %d (export %d, generated %d)".formatted(formatter.format(LocalDateTime.now()), totalUsage, -activeImport, activeProduce));

                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        System.out.println("loop terminated");
    }


    static HomeWizardP1 readP1HomeWizard(URI uri) throws Exception {
        final InputStreamReader reader = new InputStreamReader(uri.toURL().openStream());
        return gson.fromJson(reader, HomeWizardP1.class);
    }

    static Omnik readOmnik(URI uri) throws Exception {
        URLConnection conn = uri.toURL().openConnection();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null; ) {
                if (line.contains("myDeviceArray[0]=")) {
                    final var ss = line.split(",");
                    final int current = Integer.parseInt(ss[5]); // W
                    final double today = Integer.parseInt(ss[6]) / 100.0; // KWh, original reading is in 1/100's of kWh
                    final double total = Integer.parseInt(ss[7]) / 10.0; // KWh, original reading is in 1/10's of kWh
                    return new Omnik(current, today, total);
                }
            }
        }
        throw new RuntimeException("Unable to find Omnik data in response");
    }
}