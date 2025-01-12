package net.fijma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            loop(Reader.create());
        } catch (IllegalArgumentException e) {
            logger.error("{}", e.getMessage());
            System.exit(1);
        }
        logger.info("terminated");
    }

    private static void loop(Reader reader) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final var p1HomeWizard = reader.readP1HomeWizard();
                final var omnik = reader.readOmnik();

                final var activeImport = (int) p1HomeWizard.activePowerW();
                final var activeProduce = omnik.current();
                final var totalUsage = activeImport + activeProduce;

                if (activeImport > 0) {
                    logger.info("using {} (imported {}, generated {})", totalUsage, activeImport, activeProduce);
                } else {
                    logger.info("using {} (exported {}, generated {})", totalUsage, -activeImport, activeProduce);
                }
            } catch (IOException e) {
                logger.error("{}", e.getMessage());
            }

            try {
                Thread.sleep(120 * 1_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}