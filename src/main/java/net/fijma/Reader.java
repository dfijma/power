package net.fijma;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Reader {

    private final URL homewizardUrl;
    private final URL omnikUrl;

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private Reader(URL homewizardUrl, URL omnikUrl) {
        this.homewizardUrl= homewizardUrl;
        this.omnikUrl = omnikUrl;
    }

    public record HomeWizardP1(double totalPowerImportKwh, double totalPowerExportKwh, double activePowerW) {}
    public record Omnik(int current, double today, double total) {}

    public static Reader create() {
        try {
            return new Reader(
                    new URI("http://10.1.0.153/api/v1/data").toURL(),
                    new URI("http://10.1.0.104/js/status.js").toURL());
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public HomeWizardP1 readP1HomeWizard() throws IOException {
        final URLConnection connection = homewizardUrl.openConnection();
        connection.setReadTimeout(1000);
        connection.setConnectTimeout(1000);
        try (final var stream =  connection.getInputStream()) {
            final InputStreamReader reader = new InputStreamReader(stream);
            return gson.fromJson(reader, HomeWizardP1.class);
        }
    }

    public Omnik readOmnik() throws IOException {
        final URLConnection urlConnection = omnikUrl.openConnection();
        urlConnection.setConnectTimeout(1000);
        urlConnection.setReadTimeout(1000);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
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
