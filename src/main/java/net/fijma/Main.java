package net.fijma;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    public static void main(String[] args) {
        try {
            loop(Reader.create());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("loop terminated");
    }

    private static void loop(Reader reader) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final var p1HomeWizard = reader.readP1HomeWizard();
                final var omnik = reader.readOmnik();

                final var activeImport = (int) p1HomeWizard.activePowerW();
                final var activeProduce = omnik.current();
                final var totalUsage = activeImport + activeProduce;

                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

                if (activeImport > 0) {
                    System.out.println("%s: using %d (import %d, generated %d)".formatted(formatter.format(LocalDateTime.now()), totalUsage, activeImport, activeProduce));
                } else {
                    System.out.println("%s: using %d (export %d, generated %d)".formatted(formatter.format(LocalDateTime.now()), totalUsage, -activeImport, activeProduce));

                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }
    }


}